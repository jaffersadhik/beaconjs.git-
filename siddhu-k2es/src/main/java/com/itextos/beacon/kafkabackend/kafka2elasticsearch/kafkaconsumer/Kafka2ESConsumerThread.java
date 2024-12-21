package com.itextos.beacon.kafkabackend.kafka2elasticsearch.kafkaconsumer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.messageprocessor.data.KafkaDataLoader;
import com.itextos.beacon.commonlib.messageprocessor.data.db.KafkaClusterComponentMap;
import com.itextos.beacon.commonlib.messageprocessor.data.db.KafkaClusterInfo;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.errorlog.K2ESDataLog;
import com.itextos.beacon.errorlog.K2ESLog;
import com.itextos.beacon.platform.elasticsearchutil.EsProcess;

public class Kafka2ESConsumerThread
        extends
        Thread
        implements
        ConsumerRebalanceListener
{

    private static final K2ESLog                              log                     = K2ESLog.getInstance();
    private static final K2ESDataLog                              logdata                     = K2ESDataLog.getInstance();
    
    private static final String AppProcID = CommonUtility.getJvmProcessId();

    private final Component COMPONENT;
    
    private static final String PROPERTY_KAFKA_TOPIC = "kafka.topic.name";
    private static final String PROPERTY_CLIENT_ID   = "client.id";
    private static final String PROPERTY_GROUP_ID    = "group.id";
    
    private final AtomicBoolean             stopped         = new AtomicBoolean(false);

    private KafkaConsumer<String, IMessage> TopicConsumer   = null;
    private final String                    KafkaTopicName;
    private final String                    KafkaConsumerGroupId;
    

    public String                           ConsumerThreadName;
    private final String                    ConsumerClientID;
    private final AppConfiguration          AppConfig;
    private final String                    ESIndexName;
    private final String                    ESIndexUniqueColumn;
    public final String                     ESFmsgIndexName;
    public final String                     ESFmsgIndexUniqueColumn;
    public ArrayList<ESIndexColMapValue>           ListESColMap            = null;

    private final int                       ESRetryConflictCount;

    private RestHighLevelClient             ESClient        = null;
    private BulkRequest                     bulkRequest     = null;
    private BulkRequest                     fmsgBulkRequest = null;

    private final int                       IdleFlushTime;
    private final int                       FlushLimit;
    private final int                       LogProcLimit;

    private long                            IdleTimeMS      = 0L;
    private int                             BatchCount      = 0;
    private int                             ProcCount       = 0;
    private int                             LogProcCount    = 0;

    public Kafka2ESConsumerThread(
            String pThreadName,String topicname,String groupname,AppConfiguration appConfig,Component component,ArrayList<ESIndexColMapValue> listESColMap)
    {
        this.setName(pThreadName);
        ConsumerThreadName           = pThreadName;
        this.ListESColMap 			 = listESColMap;
        this.AppConfig               = appConfig;
        this.ESIndexName             = appConfig.getString("es.index.name");
        this.ESIndexUniqueColumn     = appConfig.getString("es.index.uidcolumn");

        this.ESFmsgIndexName         = appConfig.getString("es.fmsg.index.name");

        this.ESFmsgIndexUniqueColumn =  appConfig.getString("es.fmsg.index.uidcolumn");
        this.KafkaTopicName          = topicname;
        this.KafkaConsumerGroupId    = groupname;
        this.ESRetryConflictCount    = appConfig.getInt("es.update.retry.count");
        this.FlushLimit              = appConfig.getInt("es.index.flush.limit");
        this.IdleFlushTime           = appConfig.getInt("consumer.idle.flushtime.ms");
        this.LogProcLimit            = appConfig.getInt("consumer.log.proc.limit");
        
        this.COMPONENT =component;
       String  HostIPAddr = CommonUtility.getApplicationServerIp();


        ConsumerClientID             = HostIPAddr + ":" + AppProcID + ":"+ pThreadName;
    }

    RestHighLevelClient esConnect()
    {
    	/*
        final String         ESHosts         = this.AppConfig.getString("es.servers");
        final String[]       ESHSplit        = ESHosts.split("[,]");
        final int            ESConnecTimeOut = this.AppConfig.getInt("es.connection.timeout");
        final int            ESSocketTimeout = this.AppConfig.getInt("es.socket.timeout");

        final List<HttpHost> httpHosts       = Arrays.stream(ESHSplit)
                .map(s -> s.split("[:]"))
                .map(strings -> new HttpHost(strings[0], Integer.valueOf(strings[1])))
                .collect(Collectors.toList());

        log.info("Connecting to ElasticSearch Nodes: " + httpHosts.toString());

        final RestClientBuilder   builder    = RestClient.builder(
                httpHosts.toArray(new HttpHost[httpHosts.size()]))
                .setRequestConfigCallback(
                        requestConfigBuilder -> requestConfigBuilder
                                .setConnectTimeout(ESConnecTimeOut)
                                .setSocketTimeout(ESSocketTimeout));
                                */
      //  final RestHighLevelClient restClient = new RestHighLevelClient(builder);
        
        final RestHighLevelClient restClient = EsProcess.getInstance().getEsConnection();

        return restClient;
    }

    KafkaConsumer<String, IMessage> kafkaConnect()
    {

    	
        final KafkaClusterComponentMap lKafkaClusterComponentMap = KafkaDataLoader.getInstance().getKafkaClusterComponentMap(COMPONENT, ClusterType.COMMON);

        final String lKafkaConsumerClusterName = lKafkaClusterComponentMap.getKafkaConsumerClusterName();

        final KafkaClusterInfo lKafkaClusterInfo = KafkaDataLoader.getInstance().getKafkaClusterInfo(lKafkaConsumerClusterName);

        final Properties              consumerProps           = PropertyLoader.getInstance().getPropertiesByFileName( lKafkaClusterInfo.getKafkaConsumerProperties());

        consumerProps.setProperty(PROPERTY_KAFKA_TOPIC, KafkaTopicName);
        consumerProps.setProperty(PROPERTY_GROUP_ID, lKafkaClusterComponentMap.getKafkaConsumerGroupName()+"-k2e-"+System.getenv("topicgroupid"));
        consumerProps.setProperty(PROPERTY_CLIENT_ID, ConsumerClientID);
  /*      
        final Properties ConsumerProps = new Properties();
        final String     KafkaServers  = this.AppConfig.getString("kafka.bootstrap.servers");
        ConsumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaServers);
        ConsumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, this.KafkaConsumerGroupId);
        ConsumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, this.AppConfig.getString("kafka.key.deserializer"));
        ConsumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, this.AppConfig.getString("kafka.value.deserializer"));
        ConsumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, this.AppConfig.getString("kafka.enable.auto.commit"));
        ConsumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, this.AppConfig.getString("kafka.auto.offset.reset"));
        ConsumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, this.AppConfig.getString("kafka.max.poll.records"));
        ConsumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, this.AppConfig.getString("kafka.session.timeout.ms"));
        ConsumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, this.ConsumerClientID);
*/
        final KafkaConsumer<String, IMessage> consumer = new KafkaConsumer<>(consumerProps);
        return consumer;
    }

    private void processData(
            ConsumerRecords<String, IMessage> pDataList)
            throws Exception
    {

        for (final ConsumerRecord<String, IMessage> data : pDataList)
        {
            final IMessage iMsg     = data.value();
            JSONObject     dataJSON = null;

            try
            {
                if (COMPONENT==Component.T2DB_SUBMISSION){
                    dataJSON = Kafka2ESJSONUtil.buildSubJSON(iMsg,
                             ESIndexUniqueColumn,
                             Kafka2ESConstants.subUpdTmColumn,
                             ListESColMap);
                }
                else if(COMPONENT==Component.T2DB_DELIVERIES) {
                	
                    dataJSON = Kafka2ESJSONUtil.buildDelJSON(iMsg,
                            ESIndexUniqueColumn,
                            Kafka2ESConstants.delUpdTmColumn,
                            ListESColMap);

                }
                   
            }
            catch (final Exception ex)
            {
                log.error("Error while processing Message Object", ex);
                ex.printStackTrace(System.err);
                if (log.isDebugEnabled())
                    log.debug(iMsg.toString());
            }

            if (dataJSON == null)
            {
                log.error("Unable to build JSON Object from Message Object");

                if (log.isDebugEnabled())
                    log.debug(iMsg.toString());

                continue;
            }

            final String        msgId         = CommonUtility.nullCheck(dataJSON.get(this.ESIndexUniqueColumn), true);
            final UpdateRequest updateRequest = new UpdateRequest(this.ESIndexName, msgId)
                    .doc(dataJSON.toJSONString(), XContentType.JSON)
                    .docAsUpsert(true);
            updateRequest.retryOnConflict(ESRetryConflictCount);
            bulkRequest.add(updateRequest);

            final String baseMsgId = CommonUtility.nullCheck(dataJSON.get(this.ESFmsgIndexUniqueColumn), true);

            if (!"".equals(baseMsgId))
            {
                JSONObject fmsgJSON = null;
                if (COMPONENT==Component.T2DB_SUBMISSION){
                    fmsgJSON = Kafka2ESJSONUtil.buildSubFMSGJSON(dataJSON, baseMsgId,ESFmsgIndexUniqueColumn);
                } else if(COMPONENT==Component.T2DB_DELIVERIES) {
                        fmsgJSON = Kafka2ESJSONUtil.buildDelFMSGJSON(dataJSON, baseMsgId,ESFmsgIndexUniqueColumn);
                }
                if (fmsgJSON != null)
                {
                    final UpdateRequest fmsgupdateRequest = new UpdateRequest(this.ESFmsgIndexName, baseMsgId)
                            .doc(fmsgJSON.toJSONString(), XContentType.JSON)
                            .docAsUpsert(true);
                    fmsgupdateRequest.retryOnConflict(ESRetryConflictCount);
                    fmsgBulkRequest.add(fmsgupdateRequest);
                }
            }

            BatchCount++;

            if (BatchCount == FlushLimit)
            {
                if (log.isDebugEnabled())
                    log.debug("Batch Count: " + BatchCount + ", Flusing Data...");

                writeData();
            }
        }
    }

    private void writeData()
            throws Exception
    {

        if (BatchCount == 0)
        {
            if (log.isDebugEnabled())
                log.debug("BulkRequest is empty");
            return;
        }

        if (log.isDebugEnabled())
            log.debug("Executing ES BulkAsync ...");

        // ESClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        final ESBulkAsyncListener bal = new ESBulkAsyncListener(bulkRequest, false);
        ESClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, bal);
        bulkRequest = new BulkRequest();

        if (fmsgBulkRequest.requests().size() > 0)
        {
            if (log.isDebugEnabled())
                log.debug("Executing ES BulkAsync for Full Message Info...");
            // ESClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            final ESBulkAsyncListener balFmsg = new ESBulkAsyncListener(fmsgBulkRequest, true);
            ESClient.bulkAsync(fmsgBulkRequest, RequestOptions.DEFAULT, balFmsg);
            fmsgBulkRequest = new BulkRequest();
        }

        if (log.isDebugEnabled())
            log.debug("Executing Kafka Consumer CommitAsync ...");

        TopicConsumer.commitAsync();
        CommonUtility.sleepForAWhile(5);

        ProcCount += BatchCount;

        if (log.isDebugEnabled())
            log.debug(ProcCount + " records procesed");
        else
        {
            LogProcCount += BatchCount;

            if (LogProcCount >= LogProcLimit)
            {
                log.info(ProcCount + " records procesed");
                LogProcCount = 0;
            }
        }

        BatchCount = 0;
    }

    @Override
    public void run()
    {

        try
        {
            log.info("Consumer Thread started");
            log.info("Connecting to ElaticSearch...");
            ESClient = esConnect();
            log.info("Creating Kafka Consumer...");
            TopicConsumer = kafkaConnect();
            log.info("subscribing topic: " + this.KafkaTopicName);
            TopicConsumer.subscribe(Collections.singleton(this.KafkaTopicName), this);

            final int PollMS    = this.AppConfig.getInt("consumer.poll.ms");
            final int PollDelay = this.AppConfig.getInt("consumer.poll.delay.ms");

            log.info("polling topic");

            bulkRequest     = new BulkRequest();
            fmsgBulkRequest = new BulkRequest();

            IdleTimeMS      = DateTimeUtility.getCurrentTimeInMillis();

            while (!stopped.get())
            {

                if ((BatchCount > 0) &&
                        ((DateTimeUtility.getCurrentTimeInMillis() - IdleTimeMS) >= IdleFlushTime))
                {
                    if (log.isDebugEnabled())
                        log.debug("Idle Time threshold has exceeded, Flushing data, Batch Count: " + BatchCount);

                    writeData();
                    IdleTimeMS = DateTimeUtility.getCurrentTimeInMillis();
                }

                final ConsumerRecords<String, IMessage> pollRecords = TopicConsumer.poll(
                        Duration.of(PollMS, ChronoUnit.MILLIS));

                final int                               pollCount   = pollRecords.count();

                
                if(pollCount==0) {
                if (log.isDebugEnabled())
                    log.debug("KafkaConsumerGroupId : "+KafkaConsumerGroupId+" : KafkaTopicName : "+KafkaTopicName+" Poll Count: " + pollCount);
                }else {
                	
                    logdata.debug("KafkaConsumerGroupId : "+KafkaConsumerGroupId+" : KafkaTopicName : "+KafkaTopicName+" Poll Count: " + pollCount);

                }
                if (pollCount == 0)
                {
                    CommonUtility.sleepForAWhile(PollDelay);
                    continue;
                }

                processData(pollRecords);
                IdleTimeMS = DateTimeUtility.getCurrentTimeInMillis();
            }
        }
        catch (final WakeupException we)
        {

            // Ignore exception if closing
            if (!this.stopped.get())
            {
                if (log.isDebugEnabled())
                    log.debug("Stop Flag is already set in the WakeupException");
                throw we;
            }
        }
        catch (final Exception ex)
        {
            log.error(ex.getMessage(), ex);
            ex.printStackTrace(System.err);
        }
        finally
        {

            try
            {
                if (!this.stopped.get())
                    this.stopped.set(true);

                if (BatchCount > 0)
                    writeData();

                if (ESClient != null)
                    ESClient.close();

                if (TopicConsumer != null)
                    TopicConsumer.close();

                log.info("Total records processed: " + ProcCount);
                log.info("Consumer Thread stopped");
              
            }
            catch (final Exception ex2)
            {
                log.error(ex2.getMessage(), ex2);
                ex2.printStackTrace(System.err);
            }
        }
    }

    public String getConsumerThreadName()
    {
        return this.ConsumerThreadName;
    }

    public boolean isConsumerStopped()
    {
        return stopped.get();
    }

    public void stopConsumer()
    {
        if (stopped.get())
            return;
        stopped.set(true);
        CommonUtility.sleepForAWhile(500);
        if (TopicConsumer != null)
            TopicConsumer.wakeup();
    }

    @Override
    public void onPartitionsRevoked(
            Collection<TopicPartition> partitions)
    {
        log.info("Partitions revoked: " + partitions);
    }

    @Override
    public void onPartitionsAssigned(
            Collection<TopicPartition> partitions)
    {
        log.info("Partitions assigned: " + partitions);
    }

}
