package com.itextos.beacon.kafkabackend.kafka2elasticsearch.start;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.elasticsearch.client.RestClient;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfo;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.errorlog.ErrorLog;
import com.itextos.beacon.errorlog.K2ESLog;
import com.itextos.beacon.kafkabackend.kafka2elasticsearch.kafkaconsumer.AppConfigLoader;
import com.itextos.beacon.kafkabackend.kafka2elasticsearch.kafkaconsumer.AppConfiguration;
import com.itextos.beacon.kafkabackend.kafka2elasticsearch.kafkaconsumer.ESIndexColMapValue;
import com.itextos.beacon.kafkabackend.kafka2elasticsearch.kafkaconsumer.Kafka2ESConstants;
import com.itextos.beacon.kafkabackend.kafka2elasticsearch.kafkaconsumer.Kafka2ESConsumerThread;

public class StartApplicationDN
{

    private static final K2ESLog                              log                     = K2ESLog.getInstance();
    public static String                                  ESClientTypeConfig      = null;
    public static AppConfiguration                        AppConfig               = null;


    public static String                                  ESDocUpdTmColumn        = null;

    public static ArrayList<ESIndexColMapValue>           ListESColMap            = null;

    public static String                                  KafkaTopicName          = null;
    
    public static String                                  PRIORITY          = null;

    public static int                                     KafkaConsGrpSeq         = -1;

    public  String                                  KafkaConsGrpID          = null;

    public static HashMap<String, Kafka2ESConsumerThread> HMConsumerThreads       = null;

 //   public static Thread                                  mainThread              = null;

    public static RestClient                              ES_LRC_Client           = null;
    public static RestClient                              ESErr_LRC_Client        = null;

    public static void testConsumer()
            throws Exception
    {
        TestKafkaConsumer.testConsumeSubMessage("t2db-submission", "cg-t2sub-zz-2");
    }

    public static synchronized void logMsg(
            String msg)
    {
        System.out.println(DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS)
                + ": " + Thread.currentThread().getName() + ": " + msg);
    }

    @SuppressWarnings("resource")
    static void fetchESColMapFromDB()
            throws Exception
    {
       
        String       SQL             = "select column_name, mapped_name, column_type, default_value, ci_column_required ";
        SQL += " from configuration.es_sub_del_t2_col_map where index_type='deliveries' and column_name != '" + AppConfig.getString("es.index.uidcolumn") + "'";

        log.info("ES Index Column Map SQL: " + SQL);

        Connection conn=null;
        Statement  stmt=null;
        ResultSet rsColMap=null;
        try {
         conn = DBDataSourceFactory.getConnectionFromThin(JndiInfo.CONFIGURARION_DB);
       
        stmt = conn.createStatement();
        stmt.setFetchSize(500);
         rsColMap = stmt.executeQuery(SQL);

        ListESColMap = new ArrayList<>();
        boolean ErrorFlag    = false;
        String  ErrorMessage = null;

        while (rsColMap.next())
        {
            final String column_name   = CommonUtility.nullCheck(rsColMap.getString(1), true);
            final String map_name      = CommonUtility.nullCheck(rsColMap.getString(2), true);
            final String column_type   = CommonUtility.nullCheck(rsColMap.getString(3), true);
            final String default_value = CommonUtility.nullCheck(rsColMap.getString(4), true);
            final int    ci_required   = rsColMap.getInt(5);

            if ("".equals(column_name) || "".equals(map_name) || "".equals(column_type))
            {
                ErrorFlag    = true;
                ErrorMessage = "Column Name/Map Name/Column Type cannot be Empty/Null";
                break;
            }

            boolean ci_req_flag = false;
            if (ci_required != 0)
                ci_req_flag = true;

            ListESColMap.add(new ESIndexColMapValue(column_name, map_name, column_type, default_value, ci_req_flag));
        }

        }catch(Exception e) {
        	
        	ErrorLog.log(ErrorMessage.getStackTraceAsString(e));
        	
        }finally {
        	
        	try {
        		rsColMap.close();
        	}catch(Exception e) {
        		
        	}
        	try {
                stmt.close();
        	}catch(Exception e) {
        		
        	}
        	try {
                conn.close();
        	}catch(Exception e) {
        		
        	}
        }
        

       
    }


    public static void stopConsumerThreads()
            throws Exception
    {

        try
        {

            for (final Kafka2ESConsumerThread ct : HMConsumerThreads.values())
            {
                final String ctName = ct.getConsumerThreadName();

                if (ct.isConsumerStopped())
                    log.info("Consumer Thread: " + ctName + " has already stopped");
                else
                {
                    logMsg("Stopping Consumer Thread: " + ctName);
                    ct.stopConsumer();
                    ct.join();
                    CommonUtility.sleepForAWhile();
                }
            }
            logMsg("Consumer Threads are stopped");
        }
        catch (final Exception ex)
        {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    public static void main(
            String[] args)
    {

        try
        {

        	/*
            if (args.length < 4)
            {
                System.err.println("Invalid Arguments");
                System.err.println("Usage: StartApplication <Mode> <TOPIC_NAME> <Consumer Greqp Seq> <Thread Count>");
            }

			*/

            PRIORITY  = System.getenv("priority");//args[1];
            KafkaConsGrpSeq = Integer.parseInt(System.getenv("topicgroupid"));//Integer.parseInt(args[2]);
            final int threadCount = Integer.parseInt(System.getenv("threadcount"));//Integer.parseInt(args[3]);

         //   KafkaConsGrpID = "cg-" + KafkaTopicName + "-" + KafkaConsGrpSeq;

            if (threadCount <= 0)
            {
                log.error("Consumer Threads count cannot be Zero, exiting ...");
                System.err.println("Consumer Threads count cannot be Zero, exiting ...");
                return;
            }

        

         
             
                    ESDocUpdTmColumn = Kafka2ESConstants.delUpdTmColumn;
                    KafkaTopicName=rechangeTopicName(Component.T2DB_DELIVERIES.getKey());

            // final String vmName = ManagementFactory.getRuntimeMXBean().getName();
            // AppProcID = vmName.substring(0, vmName.indexOf("@"));


           


            AppConfig   = AppConfigLoader.getInstance().getAppConfiguration();

           

            log.info("Kafka Topic Name: " + KafkaTopicName);

            log.info("Fetching Column map details from DB ...");
            fetchESColMapFromDB();

            if ((ListESColMap == null) || (ListESColMap.size() == 0))
            {
                log.error("No Mapping Column details found, exiting ...");
                System.err.println("No Mapping Column details found, exiting ...");
                return;
            }

            HMConsumerThreads = new HashMap<>();
            
            StringTokenizer st=new StringTokenizer(PRIORITY,",");
            
            while(st.hasMoreTokens()) {
            	
            	String priority=st.nextToken();
            	
            	String topicname=KafkaTopicName;
            	
            	if(!priority.equals("default")) {
            		
            		topicname=topicname+"-"+priority;
            	}
           
            	 for (int ti = 1; ti <= threadCount; ti++)
                 {
                     
                     String   KafkaConsGrpID = "cg-" + topicname + "-" + KafkaConsGrpSeq;

                     final String           thName = "t-" + KafkaConsGrpID + "-" + ti;


                     Kafka2ESConsumerThread ct     = null;

                     ct = new Kafka2ESConsumerThread(thName,topicname,KafkaConsGrpID,AppConfig,Component.T2DB_DELIVERIES,ListESColMap);
                     HMConsumerThreads.put(thName, ct);
                     log.info("Starting Consumer Thread: " + thName);
                     ct.start();
                     CommonUtility.sleepForAWhile();
                 }

            }

           
           
            for (final Kafka2ESConsumerThread ct : HMConsumerThreads.values())
                ct.join();
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
                if (HMConsumerThreads != null)
                    for (final Kafka2ESConsumerThread ct : HMConsumerThreads.values())
                        if (!ct.isConsumerStopped())
                        {
                            ct.stopConsumer();
                            ct.join();
                            CommonUtility.sleepForAWhile();
                        }

                CommonUtility.sleepForAWhile(1000);
                log.info("Kafka2ES Consumer Application Mode:  stopped");
                logMsg("Kafka2ES Consumer Application Mode:  stopped");
            }
            catch (final Exception ex2)
            {
                log.error(ex2.getMessage(), ex2);
                ex2.printStackTrace(System.err);
            }
        }
    }

    
    public static String rechangeTopicName(String topicname) {
    	
    	return topicname.replaceAll("_", "-");
    }
}
