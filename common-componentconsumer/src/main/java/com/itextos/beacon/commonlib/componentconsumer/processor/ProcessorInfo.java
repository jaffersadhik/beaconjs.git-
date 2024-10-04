package com.itextos.beacon.commonlib.componentconsumer.processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.extend.Utility;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.daemonprocess.ShutdownHandler;
import com.itextos.beacon.commonlib.daemonprocess.ShutdownHook;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.messageprocessor.data.KafkaDBConstants;
import com.itextos.beacon.commonlib.messageprocessor.data.KafkaDataLoader;
import com.itextos.beacon.commonlib.messageprocessor.data.KafkaDataLoaderUtility;
import com.itextos.beacon.commonlib.messageprocessor.data.KafkaInformation;
import com.itextos.beacon.commonlib.messageprocessor.data.StartupRuntimeArguments;
import com.itextos.beacon.commonlib.messageprocessor.data.db.KafkaClusterComponentMap;
import com.itextos.beacon.commonlib.messageprocessor.data.db.KafkaComponentInfo;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.CoreExecutorPoolSingleton;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.smslog.ComponentProcessorLog;
import com.itextos.beacon.smslog.ConsumerTopicList;
import com.itextos.beacon.smslog.DebugLog;
import com.itextos.beacon.smslog.ErrorLog;
import com.itextos.beacon.smslog.StartupFlowLog;
import com.itextos.beacon.smslog.TopicLog;
import com.itextos.beacon.splog.SPLog;

public class ProcessorInfo
        implements
        ShutdownHook
{

    private static final Log                log                               = LogFactory.getLog(ProcessorInfo.class);

    private static final String             ALL                               = "ALL";

    private static final String             PROP_KEY_APPLICATION_PROCESS_ID   = "process.id";
    private static final String             PROP_KEY_APPLICATION_STARTED_TIME = "app.started.time";
    private static final String             PROP_KEY_SHUTDOWN_STARTED_TIME    = "shutdown.started.time";
    private static final String             PROP_KEY_SHUTDOWN_COMPLETED_TIME  = "shutdown.completed.time";

    private static final int                RESTART_WAIT_SECONDS              = 10;

    private final Component                 mComponent;
    private final StartupRuntimeArguments   mStartupRuntimeArguments;
    private final List<IComponentProcessor> allProcessors                     = new ArrayList<>();

    private boolean                         clusterNotSpecified               = false;

    public ProcessorInfo(
            Component aComponent)
    {
        this(aComponent, true);
    }

    public ProcessorInfo(
            Component aComponent,
            boolean aStartJettyServer)
    {
        mComponent = aComponent;

    //    readLockFile();
    //    createLockFile();

        mStartupRuntimeArguments = new StartupRuntimeArguments();
        

        if (log.isDebugEnabled())
            log.debug("Invoking for Component '" + mComponent + "' with Startup Arguments " + mStartupRuntimeArguments);
        
        StartupFlowLog.log("Invoking for Component '" + mComponent + "' with Startup Arguments " + mStartupRuntimeArguments);
        

    	DebugLog.log("Invoking for Component '" + mComponent + "' with Startup Arguments " + mStartupRuntimeArguments);
    	
        ShutdownHandler.getInstance().addHook(aComponent.toString(), this);

    //    startPrometheusServer(aStartJettyServer);

        // Processing the Fallback Data
        /** This is not required now as it will be taken there itself. */
        // new FallbackProducerDataProcess(aComponent);
    }

   

    public void process()
            throws Exception
    {
        final KafkaComponentInfo   kafkaComponentInfo  = KafkaDataLoader.getInstance().getKafkaProcessorInfo(mComponent);
        final List<ClusterType>    platformClusterList = getClustersToProcess();

        final List<InterfaceGroup> lInterfaceGroupList = mStartupRuntimeArguments.getInterfaceGroup();
        if (lInterfaceGroupList.isEmpty())
            lInterfaceGroupList.add(null);

        final List<MessagePriority> lMessagePriorityList = mStartupRuntimeArguments.getMessagePriority();
        if (lMessagePriorityList.isEmpty())
            lMessagePriorityList.add(null);

        final Map<String, List<String>> clientBasedTopics         = getClientBasedTopics();
        Map<String, List<String>>       clusterBasedDefaultTopics = new HashMap<>();
        final Map<String, List<String>> priorityBasedTopicsList   = new HashMap<>();

        final List<ClusterType>         types                     = KafkaDataLoader.getInstance().isSeparateInstanceClusters();

        for (final ClusterType cluster : platformClusterList)
        {
            if (log.isDebugEnabled())
                log.debug("Processing for the cluster Type '" + cluster + "'");
            
            StartupFlowLog.log("Processing for the cluster Type '" + cluster + "'");
            
            
            if (cluster != null)
            {
                final KafkaClusterComponentMap lKafkaCLusterInformation = KafkaDataLoader.getInstance().getKafkaClusterComponentMap(mComponent, cluster);

                if (lKafkaCLusterInformation == null)
                {
                    final String s = "Component '" + mComponent.getKey() + "' is not configured to Platform cluster '" + cluster + "'. Please configuration in table '"
                            + KafkaDBConstants.TABLE_NAME_PLATFORM_CLUSTER_COMPONENT_KAFKA_CLUSTER_MAP + "'";
                    logAndThrowException(s);
                }
            }

            if (clusterNotSpecified && types.contains(cluster))
            {
            	StartupFlowLog.log("Skipping '" + cluster + "' cluster as it has separate instance setup.");
            	log.fatal("Skipping '" + cluster + "' cluster as it has separate instance setup.");
                continue;
            }

            final List<String> defaultTopicNamesForCluster = getDefaultTopics(cluster);

            clusterBasedDefaultTopics.put(KafkaDataLoaderUtility.getNameOrDefault(cluster), defaultTopicNamesForCluster);

            for (final InterfaceGroup intfGroup : lInterfaceGroupList)
            {
                if (log.isDebugEnabled()) {
                	
                    log.debug("Processing for the cluster Type '" + cluster + "' and interface group '" + intfGroup + "'");

                }
                
                StartupFlowLog.log("Processing for the cluster Type '" + cluster + "' and interface group '" + intfGroup + "'");
                	
                for (final MessagePriority msgPriority : lMessagePriorityList)
                {
                    if (log.isDebugEnabled()) {
                        log.debug("Processing for the cluster Type '" + cluster + "' and interface group '" + intfGroup + "' IMessage Priority '" + msgPriority + "'");
                    }
                    
                    StartupFlowLog.log("Processing for the cluster Type '" + cluster + "' and interface group '" + intfGroup + "' IMessage Priority '" + msgPriority + "'");
                    final List<String> priorityBasedTopics = getPriorityBasedTopics(cluster, intfGroup, mStartupRuntimeArguments.getMessageType(), msgPriority);

                    if ((priorityBasedTopics != null) && !priorityBasedTopics.isEmpty())
                    {
                        final String lPriorityKeys = getPriorityKeys(cluster, intfGroup, mStartupRuntimeArguments.getMessageType(), msgPriority);
                        priorityBasedTopicsList.put(lPriorityKeys, priorityBasedTopics);
                    }
                }// for (final MessagePriority msgPriority : lMessagePriorityList)
            }// for (final InterfaceGroup intfGroup : lInterfaceGroupList)
        }// for (final ClusterType cluster : platformClusterList)

        if ((clusterBasedDefaultTopics.size() == 0) || ((clusterBasedDefaultTopics.size() == 1) && (clusterBasedDefaultTopics.containsKey(KafkaDBConstants.DEFAULT))))
            clusterBasedDefaultTopics = getDefaultTopicsForAllClusters();

        final Map<String, List<String>> topicsToConsume = consolidateAllTopics(platformClusterList, lInterfaceGroupList, lMessagePriorityList, clientBasedTopics, priorityBasedTopicsList,
                clusterBasedDefaultTopics);

        printTopicList(topicsToConsume);

        String module=System.getenv("module");
    	
    	
    	if(module.equals("smppdlrhandover")||module.equals("dnpost")) {
    		
    	List<String> tempTpopics=topicsToConsume.get("bulk");

    	if(tempTpopics!=null) {
    	tempTpopics.add("smpp-dlr-handover");
    	
    	tempTpopics.add("smpp-dlr-handover-intl");
    	
    	topicsToConsume.put("bulk", tempTpopics);
    	}
    	
    	}else if(module.equals("dnpostlogt2tb")||module.equals("biller")) {
    		
    		
    		List<String> tempTpopics=topicsToConsume.get("bulk");

        	if(tempTpopics!=null) {
        	tempTpopics.add("t2db-smpp-post-log");
        	
        	tempTpopics.add("t2db-smpp-post-log-intl");
        	
        	topicsToConsume.put("bulk", tempTpopics);
        	}
    	}
    	
        final Map<String, Map<String, ConsumerInMemCollection>> lConsumerInmemCollection = createConsumersBeforeStartingThread(topicsToConsume);

        createConsumerThreads(topicsToConsume, kafkaComponentInfo, lConsumerInmemCollection);
    }

    private List<ClusterType> getClustersToProcess()
    {
        final List<ClusterType> platformClusterList = mStartupRuntimeArguments.getPlatformCluster();

        if (platformClusterList.isEmpty())
        {
            clusterNotSpecified = true;
            final Set<String> lComponentClusters = KafkaDataLoader.getInstance().getComponentClusters(mComponent);

            if (lComponentClusters == null)
                platformClusterList.add(null);
            else
                for (final String strCluster : lComponentClusters)
                {
                    if (log.isDebugEnabled())
                        log.debug("Cluster Type passed '" + strCluster + "'");

                    if (KafkaDBConstants.DEFAULT.equals(strCluster))
                        platformClusterList.add(null);
                    else
                    {
                        final ClusterType lCluster = ClusterType.getCluster(strCluster);

                        if (lCluster != null)
                        {
                            final boolean lSeparateInstance = KafkaDataLoader.getInstance().isSeparateInstance(lCluster);
                            if (!lSeparateInstance)
                                platformClusterList.add(lCluster);
                            // else
                            // throw new ItextosRuntimeException("Cluster specific instance not specified in
                            // configuration.cluster_type table for cluster '" + strCluster + "'");
                        }
                        else
                            throw new ItextosRuntimeException("Invalid Cluster specified '" + strCluster + "'");
                    }
                }
        }
        else
            for (final ClusterType lCluster : platformClusterList)
            {
                final boolean lSeparateInstance = KafkaDataLoader.getInstance().isSeparateInstance(lCluster);
                if (!lSeparateInstance)
                    throw new ItextosRuntimeException("Cluster specific instance not specified in configuration.cluster_type table for cluster '" + lCluster + "'");
            }
        return platformClusterList;
    }

    private void createConsumerThreads(
            Map<String, List<String>> aTopicsToConsume,
            KafkaComponentInfo aKafkaComponentInfo,
            Map<String, Map<String, ConsumerInMemCollection>> aConsumerInmemCollection)
    {

        int totalThreadsCount = 0;

        StartupFlowLog.log("aTopicsToConsume : '" + aTopicsToConsume );

        for (final Entry<String, List<String>> entry : aTopicsToConsume.entrySet())
        {
            final String      clusterName     = entry.getKey();
            
            StartupFlowLog.log("clusterName : '" + clusterName );

            final ClusterType platformCluster = ClusterType.getCluster(clusterName);

            StartupFlowLog.log("platformCluster : '" + platformCluster );

  
            final KafkaClusterComponentMap             lKafkaCLusterInformation = KafkaDataLoader.getInstance().getKafkaClusterComponentMap(mComponent, platformCluster);
            final String                               className                = aKafkaComponentInfo.getComponentProcessClass();
            final int                                  threadsCount             = lKafkaCLusterInformation.getThreadsCount();
            final int                                  intlThreadsCount         = lKafkaCLusterInformation.getIntlThreadsCount();
            final int                                  sleepInMillis            = lKafkaCLusterInformation.getSleepTimeInMillis();

            final List<String>                         topics                   = entry.getValue();
            final Map<String, ConsumerInMemCollection> topicInMemCollection     = aConsumerInmemCollection.get(clusterName);

            if (log.isInfoEnabled())
                log.info("Consumer to start for the Cluster Type Name : '" + clusterName + "' Cluster " + platformCluster+ " topics : "+topics+" topicInMemCollection : "+topicInMemCollection);
    
            StartupFlowLog.log("Consumer to start for the Cluster Type Name : '" + clusterName + "' Cluster " + platformCluster+ " topics : "+topics+" topicInMemCollection : "+topicInMemCollection);
       
            for (final String topicName : topics)
            {


                final ConsumerInMemCollection inMemCollection = topicInMemCollection.get(topicName);

                final int                     tempThreadCount = topicName.endsWith(KafkaDBConstants.INTL_SUFFIX) ? intlThreadsCount : threadsCount;
                
                boolean isInternational=false;
                
                if( topicName.endsWith(KafkaDBConstants.INTL_SUFFIX)) {
                
                	isInternational=true;
                }

                boolean isHigh=false;

                if( topicName.indexOf(KafkaDBConstants.HIGH_SUFFIX)>-1) {
                    
                	isHigh=true;
                }

                String intl =System.getenv("intl");
                
               
                
                String priority =System.getenv("priority");
             
                
                if(intl==null || priority ==null) {
                	
                	
                	  if (log.isDebugEnabled())
                          log.debug("Working for the topic '" + topicName + "'");

                  	  for (int threadIndex = 1; threadIndex <= tempThreadCount; threadIndex++)
                        {
                            totalThreadsCount++;
                            startANewThread(clusterName, platformCluster, topicName, className, inMemCollection, sleepInMillis, threadIndex);
                        }
           
                     
                }else {
                	
                	
                	 if(intl==null) {
                      	intl="";
                      }
                 	 
                 	   
                      if(priority==null) {
                      	priority="";
                      }
                      
                    if(isInternational) {
                    	
                    	if(intl.equals("1")) {
                            if (log.isDebugEnabled())
                                log.debug("Working for the topic '" + topicName + "'");

                    	  for (int threadIndex = 1; threadIndex <= tempThreadCount; threadIndex++)
                          {
                              totalThreadsCount++;
                              startANewThread(clusterName, platformCluster, topicName, className, inMemCollection, sleepInMillis, threadIndex);
                          }
                    	}
                    }else if(isHigh) {
                    	
                    	if(intl.equals("0")&&priority.equals("high")) {
                    		
                            if (log.isDebugEnabled())
                                log.debug("Working for the topic '" + topicName + "'");

                            
                      	  for (int threadIndex = 1; threadIndex <= tempThreadCount; threadIndex++)
                            {
                                totalThreadsCount++;
                                startANewThread(clusterName, platformCluster, topicName, className, inMemCollection, sleepInMillis, threadIndex);
                            }
                      	}
                    }else {
                    	
                      	if(intl.equals("0")&&priority.equals("normal")) {
                      		
                            if (log.isDebugEnabled())
                                log.debug("Working for the topic '" + topicName + "'");

                        	  for (int threadIndex = 1; threadIndex <= tempThreadCount; threadIndex++)
                              {
                                  totalThreadsCount++;
                                  startANewThread(clusterName, platformCluster, topicName, className, inMemCollection, sleepInMillis, threadIndex);
                              }
                        	}
                    }
                    
                   
                }
                log.debug("intl: "+intl +" : priority :  "+priority+" : ");
                
                
              
            	
              
            
            }// for (final String topicName : topics)
        }// for (final Entry<String, List<String>> entry : topicsToConsume.entrySet())
        
        log.error("For component " + mComponent + " Total thread created " + totalThreadsCount);

    }

    private Map<String, Map<String, ConsumerInMemCollection>> createConsumersBeforeStartingThread(
            Map<String, List<String>> aTopicsToConsume)
    {
    
    	
    	
    	
    	StartupFlowLog.log("aTopicsToConsume : "+aTopicsToConsume);

        final Map<String, Map<String, ConsumerInMemCollection>> clusterInMemCollection = new HashMap<>();

        for (final Entry<String, List<String>> entry : aTopicsToConsume.entrySet())
        {
            final String                               clusterName          = entry.getKey();
            final ClusterType                          platformCluster      = ClusterType.getCluster(clusterName);
            final Map<String, ConsumerInMemCollection> topicInMemCollection = clusterInMemCollection.computeIfAbsent(clusterName, k -> new HashMap<>());

        	StartupFlowLog.log("clusterName : "+clusterName);
        	StartupFlowLog.log("platformCluster : "+platformCluster);

            for (final String topicName : entry.getValue())
            {
            	StartupFlowLog.log("clusterName : "+clusterName+"platformCluster : "+platformCluster+" topicName : "+topicName);

                final ConsumerInMemCollection temp = KafkaInformation.getInstance().createConsumer(mComponent, platformCluster, topicName);
                topicInMemCollection.put(topicName, temp);
                
            }
        }
        
    	StartupFlowLog.log("clusterInMemCollection : "+clusterInMemCollection);

        return clusterInMemCollection;
    }

    private static void printTopicList(
            Map<String, List<String>> aTopicsToConsume)
    {
        if (log.isDebugEnabled())
            for (final Entry<String, List<String>> entry : aTopicsToConsume.entrySet())
            {
                log.debug("Platform Cluster : '" + entry.getKey() + "'");

            	StartupFlowLog.log("Platform Cluster : '" + entry.getKey() + "'");

                for (final String s : entry.getValue()) {
 
                	log.debug("Topic Name :              '" + s + "'");
                	
                	StartupFlowLog.log("Topic Name :              '" + s + "'");
            }
            }
    }

    private Map<String, List<String>> consolidateAllTopics(
            List<ClusterType> aPlatformClusterList,
            List<InterfaceGroup> aInterfaceGroupList,
            List<MessagePriority> aMessagePriorityList,
            Map<String, List<String>> aClientBasedTopics,
            Map<String, List<String>> aPriorityBasedTopicsList,
            Map<String, List<String>> aClusterBasedDefaultTopics)
    {
        final Map<String, List<String>> topicsToConsume = new HashMap<>();

        if (log.isDebugEnabled())
        {
            log.debug(">>>>>>>>>>>>>>>> Client Based Topics ");

            for (final Entry<String, List<String>> entry : aClientBasedTopics.entrySet())
            {
                log.debug("\t'" + entry.getKey() + "'");

                for (final String topicName : entry.getValue())
                    log.debug("\t\t'" + topicName + "'");
            }

            log.debug(">>>>>>>>>>>>>>>> Priority Based Topics");

            for (final Entry<String, List<String>> entry : aPriorityBasedTopicsList.entrySet())
            {
                log.debug("\t'" + entry.getKey() + "'");

                for (final String topicName : entry.getValue())
                    log.debug("\t\t'" + topicName + "'");
            }

            log.debug(">>>>>>>>>>>>>>>> Cluster Based Default Topics");

            for (final Entry<String, List<String>> entry : aClusterBasedDefaultTopics.entrySet())
            {
                log.debug("\t'" + entry.getKey() + "'");
                if (entry.getValue() != null)
                    for (final String topicName : entry.getValue())
                        log.debug("\t\t'" + topicName + "'");
                else
                    log.debug("\t\t'IT SHOULD NOT BE NULL. NEED TO CHECK THIS.'");
            }
        }

        if (mStartupRuntimeArguments.isClientSpecific())
            addClientSpecificTopics(topicsToConsume, aClientBasedTopics);

        if (mStartupRuntimeArguments.isPrioritySpecific())
            addPrioritySpecificTopics(aPlatformClusterList, aInterfaceGroupList, aMessagePriorityList, topicsToConsume, aPriorityBasedTopicsList);

        if (!mStartupRuntimeArguments.isClientSpecific() && !mStartupRuntimeArguments.isPrioritySpecific())
        {
            final List<String> lClientBasedTopic = KafkaDataLoader.getInstance().getClientBasedTopic(mComponent);

            if (!lClientBasedTopic.isEmpty())
                addClientList(topicsToConsume, lClientBasedTopic);

            final Map<String, List<String>> lTopicNameBasedOnPriorities = new HashMap<>();

            if (mStartupRuntimeArguments.isClusterSpecific())
                for (final ClusterType clusterType : mStartupRuntimeArguments.getPlatformCluster())
                    lTopicNameBasedOnPriorities.putAll(KafkaDataLoader.getInstance().getTopicNameBasedOnPriorities(mComponent, clusterType));
            else
                for (final ClusterType clusterType : aPlatformClusterList)
                    lTopicNameBasedOnPriorities.putAll(KafkaDataLoader.getInstance().getTopicNameBasedOnPriorities(mComponent, clusterType));

            if (!lTopicNameBasedOnPriorities.isEmpty())
                addClusterSpecificTopics(topicsToConsume, lTopicNameBasedOnPriorities);

            addPrioritySpecificTopics(aPlatformClusterList, aInterfaceGroupList, aMessagePriorityList, topicsToConsume, aPriorityBasedTopicsList);
            addClusterSpecificTopics(topicsToConsume, aClusterBasedDefaultTopics);
        }
        return topicsToConsume;
    }

    private void startANewThread(
            String aClusterName,
            ClusterType aPlatformCluster,
            String aTopicName,
            String aClassName,
            ConsumerInMemCollection aInMemCollection,
            int aSleepInMillis,
            int aThreadIndex)
    {
        final String threadName = CommonUtility.combine("Thread", aClusterName, mComponent.getKey(), aTopicName, Integer.toString(aThreadIndex));

         if (log.isDebugEnabled())
            log.debug("Creating a thread with name '" + threadName + "' for the class '" + aClassName + "'");

         StartupFlowLog.log("createConsumerThreads : aClusterName : "+aClusterName+" aPlatformCluster :  "+aPlatformCluster+" aTopicName : "+aTopicName+" aClassName : "+aClassName );
         
         
        try
        {
            final Class<?>            cls                       = Class.forName(aClassName);
            final Constructor<?>      constructor               = cls.getDeclaredConstructor(Utility.getDeclaredConstrutorArgumentTypes());
            final IComponentProcessor currentComponentProcessor = (IComponentProcessor) constructor.newInstance(threadName, mComponent, aPlatformCluster, aTopicName, aInMemCollection, aSleepInMillis);
            allProcessors.add(currentComponentProcessor);
/*
            Thread virtualThread = Thread.ofVirtual().start(currentComponentProcessor);

            virtualThread.setName( threadName);
  */
            TopicLog.getInstance(aTopicName+"_initiated").log(aTopicName+" : "+new Date());
            CoreExecutorPoolSingleton.getInstance().addTask(currentComponentProcessor, threadName);
            /*
            final Thread processThread = new Thread(currentComponentProcessor, threadName);
            processThread.start();

*/
            StartupFlowLog.log("Thread '" + threadName + "'started for Component '" + mComponent + "' Cluster '" + aClusterName + "' Actual Cluster '" + aPlatformCluster + "' Topic name '" + aTopicName
                    + "' Thread index '" + aThreadIndex + "' with sleep time millis '" + aSleepInMillis + "'");
            
            if (log.isInfoEnabled())
                log.info("Thread '" + threadName + "'started for Component '" + mComponent + "' Cluster '" + aClusterName + "' Actual Cluster '" + aPlatformCluster + "' Topic name '" + aTopicName
                        + "' Thread index '" + aThreadIndex + "' with sleep time millis '" + aSleepInMillis + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while creating " + aThreadIndex + " thread for the component '" + mComponent + "' for Cluster '" + aClusterName + "' Topic name '" + aTopicName + "' Class Name '"
                    + aClassName + "' ThreadIndex '" + aThreadIndex + "'", e);
            
            ErrorLog.log("Exception while creating " + aThreadIndex + " thread for the component '" + mComponent + "' for Cluster '" + aClusterName + "' Topic name '" + aTopicName + "' Class Name '"
                    + aClassName + "' ThreadIndex '" + aThreadIndex + "'"+ErrorMessage.getStackTraceAsString(e));
        }
    }

    private static void addClientList(
            Map<String, List<String>> aTopicsToConsume,
            List<String> aClientBasedTopic)
    {
        final List<String> lList = aTopicsToConsume.computeIfAbsent(KafkaDBConstants.DEFAULT, k -> new ArrayList<>());
        lList.addAll(aClientBasedTopic);
    }

    private static void addClusterSpecificTopics(
            Map<String, List<String>> aTopicsToConsume,
            Map<String, List<String>> aClusterBasedDefaultTopics)
    {

        for (final Entry<String, List<String>> entry : aClusterBasedDefaultTopics.entrySet())
        {
            final List<String> topicsList = entry.getValue();

            if ((topicsList != null) && !topicsList.isEmpty())
            {
                final List<String> mainList = aTopicsToConsume.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
                addToList(mainList, topicsList);
            }
        }
    }

    private void addPrioritySpecificTopics(
            List<ClusterType> aPlatformClusterList,
            List<InterfaceGroup> aInterfaceGroupList,
            List<MessagePriority> aMessagePriorityList,
            Map<String, List<String>> aTopicsToConsume,
            Map<String, List<String>> aPriorityBasedTopics)
    {

        if (mStartupRuntimeArguments.isPrioritySpecific() || mStartupRuntimeArguments.isClusterSpecific())
        {

            if (mStartupRuntimeArguments.isPrioritySpecific() && aPriorityBasedTopics.isEmpty())
            {
                final String errorInfo = "Configuration to start the application with Priority based topics Interface Group '" + mStartupRuntimeArguments.getInterfaceGroup() + "' IMessage Type '"
                        + mStartupRuntimeArguments.getMessageType() + "' IMessage Priority '" + mStartupRuntimeArguments.getMessagePriority() + "'. But no topics configured in the table '"
                        + KafkaDBConstants.TABLE_NAME_PLATFORM_CLUSTER_KAFKA_TOPIC_MAP + "' for the component '" + mComponent.getKey() + "' and clusters '"
                        + mStartupRuntimeArguments.getPlatformCluster() + "'";
                logAndThrowException(errorInfo);
            }

            boolean            topicAvailable = false;
            final List<String> keys           = new ArrayList<>();

            for (final ClusterType cluster : aPlatformClusterList)
            {
                final String       lPlatformCluster = KafkaDataLoaderUtility.getNameOrDefault(cluster);
                final List<String> topicsList       = aTopicsToConsume.computeIfAbsent(lPlatformCluster, k -> new ArrayList<>());

                for (final InterfaceGroup intfGroup : aInterfaceGroupList)
                {
                    final String lInterfaceGroup = KafkaDataLoaderUtility.getNameOrDefault(intfGroup);
                    final String lMessageType    = KafkaDataLoaderUtility.getNameOrDefault(mStartupRuntimeArguments.getMessageType());

                    for (final MessagePriority mp : aMessagePriorityList)
                    {
                        final String mpName = KafkaDataLoaderUtility.getNameOrDefault(mp);
                        final String key    = CommonUtility.combine(lPlatformCluster, lInterfaceGroup, lMessageType, mpName);
                        keys.add(key);

                        final List<String> lList = aPriorityBasedTopics.get(key);

                        if ((lList != null) && !lList.isEmpty())
                        {
                            topicAvailable = true;
                            addToList(topicsList, lList);
                        }
                        else
                            logPriorityError(key);
                    }
                }
            }

            if (!topicAvailable)
            {
                final String errorInfo = "Unable to find the topics passed Priroties " + keys + ". Please check table configuration in '" + KafkaDBConstants.TABLE_NAME_PLATFORM_CLUSTER_KAFKA_TOPIC_MAP
                        + "'";
                logAndThrowException(errorInfo);
            }
        }
    }

    private static void addToList(
            List<String> aTopicsToConsume,
            List<String> aList)
    {
        for (final String s : aList)
            if (!aTopicsToConsume.contains(s))
                aTopicsToConsume.add(s);
    }

    private static void logPriorityError(
            String aKey)
    {
        log.error("'" + aKey + "' does not have a proper configuration for the client specific topics in '" + KafkaDBConstants.TABLE_NAME_PLATFORM_CLUSTER_KAFKA_TOPIC_MAP + "'");
    }

    private void logAndThrowException(
            String aErrorInfo)
    {
        final ItextosRuntimeException lItextosRuntimeException = new ItextosRuntimeException(aErrorInfo);
        log.error("Exception while loading the Topics for " + mStartupRuntimeArguments, lItextosRuntimeException);
        throw lItextosRuntimeException;
    }

    private void addClientSpecificTopics(
            Map<String, List<String>> aTopicsToConsume,
            Map<String, List<String>> aClientBasedTopics)
    {

        if (mStartupRuntimeArguments.isClientSpecific())
        {

            if (aClientBasedTopics.isEmpty())
            {
                final String errorInfo = "Configuration to start the application with specfied client id '" + mStartupRuntimeArguments.getClientIds() + "'. But no topics configured in the table '"
                        + KafkaDBConstants.TABLE_NAME_CLIENT_SPECIFIC_COMPONENT + "' for the component '" + mComponent.getKey() + "'";
                logAndThrowException(errorInfo);
            }

            boolean            topicAvailable = false;
            final List<String> lClientIds     = mStartupRuntimeArguments.getClientIds();
            final List<String> topicList      = aTopicsToConsume.get(KafkaDBConstants.DEFAULT);

            for (final String cliId : lClientIds)
            {
                final List<String> lList = aClientBasedTopics.get(cliId);

                if ((lList != null) && !lList.isEmpty())
                {
                    topicAvailable = true;
                    topicList.addAll(lList);
                }
                else
                    log.error("'" + cliId + "' does not have a proper configuration for the client specific topics in '" + KafkaDBConstants.TABLE_NAME_CLIENT_SPECIFIC_COMPONENT + "'");
            }

            if (!topicAvailable)
            {
                final String errorInfo = "Unable to find the client specific topics passed " + lClientIds + ". Please check table configuration in '"
                        + KafkaDBConstants.TABLE_NAME_CLIENT_SPECIFIC_COMPONENT + "'";
                logAndThrowException(errorInfo);
            }
        }
    }

    private List<String> getDefaultTopics(
            ClusterType lPlatformCluster)
    {
        final List<String> defaultTopics     = new ArrayList<>();
        final String       lDefaultTopicName = KafkaDataLoader.getInstance().getDefaultTopicName(mComponent, lPlatformCluster);

        if (lDefaultTopicName != null)
        {
            final String lDefaultTopicNameIntl = CommonUtility.combine(KafkaDBConstants.TOPIC_SEPARATOR, lDefaultTopicName, KafkaDBConstants.INTL_SUFFIX);
            defaultTopics.add(KafkaDataLoaderUtility.updateTopicName(lDefaultTopicName));
            defaultTopics.add(KafkaDataLoaderUtility.updateTopicName(lDefaultTopicNameIntl));
        }
        return defaultTopics;
    }

    private Map<String, List<String>> getDefaultTopicsForAllClusters()
    {
        final Map<String, List<String>> returnValue       = new HashMap<>();
        final Map<String, String>       lDefaultTopicName = KafkaDataLoader.getInstance().getDefaultTopicName(mComponent);

        for (final Entry<String, String> entry : lDefaultTopicName.entrySet())
            returnValue.put(entry.getKey(), getDomAndIntlTopics(entry.getValue()));
        return returnValue;
    }

    private static List<String> getDomAndIntlTopics(
            String aDefaultTopicName)
    {
        final String       lDefaultTopicNameIntl = CommonUtility.combine(KafkaDBConstants.TOPIC_SEPARATOR, aDefaultTopicName, KafkaDBConstants.INTL_SUFFIX);
        final List<String> defaultTopics         = new ArrayList<>();
        defaultTopics.add(KafkaDataLoaderUtility.updateTopicName(aDefaultTopicName));
        defaultTopics.add(KafkaDataLoaderUtility.updateTopicName(lDefaultTopicNameIntl));
        return defaultTopics;
    }

    private List<String> getPriorityBasedTopics(
            ClusterType aPlatformCluster,
            InterfaceGroup aInterfaceGroup,
            MessageType aMessageType,
            MessagePriority aMessagePriority)
    {
        return KafkaDataLoader.getInstance().getTopicNameBasedOnPriorities(mComponent, aPlatformCluster, aInterfaceGroup, aMessageType, aMessagePriority);
    }

    private static String getPriorityKeys(
            ClusterType lPlatformCluster,
            InterfaceGroup lInterfaceGroup,
            MessageType lMessageType,
            MessagePriority mp)
    {
        return CommonUtility.combine(KafkaDataLoaderUtility.getNameOrDefault(lPlatformCluster), KafkaDataLoaderUtility.getNameOrDefault(lInterfaceGroup),
                KafkaDataLoaderUtility.getNameOrDefault(lMessageType), KafkaDataLoaderUtility.getNameOrDefault(mp));
    }

    private Map<String, List<String>> getClientBasedTopics()
    {
        final Map<String, List<String>> clientBasedTopics = new HashMap<>();
        final List<String>              lClientIds        = mStartupRuntimeArguments.getClientIds();

        if (lClientIds.isEmpty())
        {
            // When client Ids are not given in startup, start the consumer for all client
            // ids.
            final List<String> lClientBasedTopic = KafkaDataLoader.getInstance().getClientBasedTopic(mComponent);
            if (!lClientBasedTopic.isEmpty())
                clientBasedTopics.put(ALL, lClientBasedTopic);
        }
        else
            for (final String cliId : lClientIds)
            {
                final String lClientBasedTopic = KafkaDataLoader.getInstance().getClientBasedTopic(mComponent, cliId);

                if (lClientBasedTopic != null)
                {
                    final List<String> temp = new ArrayList<>();
                    temp.add(lClientBasedTopic);
                    clientBasedTopics.put(cliId, temp);
                }
            }
        return clientBasedTopics;
    }

    /**
     * This method should be called in-case the shutdown hook default behavior is
     * not happening.
     */
    @Override
    public void shutdown()
    {
        log.fatal("Shutdownhook process Started for Component '" + mComponent + "'", new Exception("Called from"));
        updateLockFileForShutdownStart();

        KafkaInformation.getInstance().stopMe();

        for (final IComponentProcessor componentProcessor : allProcessors)
        {
            componentProcessor.stopProcessing();
            componentProcessor.doCleanup();
        }
        KafkaInformation.getInstance().flushProducers();
        KafkaInformation.getInstance().isAllProducersCompleted();

        updateLockFileShutdownCompleted();

        log.fatal(mComponent + " Process Completed Successfully ...............");
    }

    private String getLockFilename()
    {
        return mComponent + ".lock";
    }

    private void updateLockFileShutdownCompleted()
    {
        final File lFile = new File(getLockFilename());

        if (!lFile.exists())
        {
            final String s = mComponent + " Lock file not found. Something went wrong. Check the lock file settings.";
            logContent(s);
        }

        try (
                BufferedWriter bw = new BufferedWriter(new FileWriter(lFile, true));)
        {
            bw.write(PROP_KEY_SHUTDOWN_COMPLETED_TIME + "=" + DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) + CommonUtility.getLineSeparator());
        }
        catch (final Exception ex)
        {}
        final String s = "Lock file updated ...";
        logContent(s);
    }

    private void updateLockFileForShutdownStart()
    {
        final File lFile = new File(getLockFilename());

        if (!lFile.exists())
        {
            final String s = mComponent + " Lock file not found. Something went wrong. Check the lock file settings.";
            logContent(s);
        }

        try (
                BufferedWriter bw = new BufferedWriter(new FileWriter(lFile, true));)
        {
            bw.write(PROP_KEY_SHUTDOWN_STARTED_TIME + "=" + DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) + CommonUtility.getLineSeparator());
        }
        catch (final Exception e)
        {
            final String s = mComponent + " Problem in updating the lock file for shutdown start";
            logContent(s, e);
        }
        final String s = mComponent + " Lock file updated for shutdown start";
        logContent(s);
    }

    private void createLockFile()
    {
    	/*
        final File lFile = new File(getLockFilename());

        if (!lFile.exists())
        {
            final String s = mComponent + " Lock file not found. No need to delete. May be the application starting without lock file...";
            logContent(s);
        }
        else
            try
            {
                Files.delete(lFile.toPath());
                final String s = mComponent + " Lock file deleted successfully";
                logContent(s);
            }
            catch (final Exception e)
            {
                final String s = mComponent + " Unable to delete the lock file : '" + lFile.getAbsolutePath() + "'";
                logContent(s, e);
                System.exit(-9);
            }

        createNewLockFile();
        final String s = mComponent + " Lock file created ...";
        logContent(s);
        
        */
    }

    private void createNewLockFile()
    {
        final File lFile = new File(getLockFilename());

        try (
                BufferedWriter bw = new BufferedWriter(new FileWriter(lFile));)
        {
            bw.write(PROP_KEY_APPLICATION_PROCESS_ID + "=" + CommonUtility.getJvmProcessId() + System.getProperty("line.separator"));
            bw.write(PROP_KEY_APPLICATION_STARTED_TIME + "=" + DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) + CommonUtility.getLineSeparator());
        }
        catch (final Exception e)
        {
            final String s = mComponent + " Unable to create the lock file : '" + lFile.getAbsolutePath() + "'";
            logContent(s, e);
            System.exit(-11);
        }
    }

    private void readLockFile()
    {
        final File lFile = new File(getLockFilename());

        if (!lFile.exists())
        {
            final String s = "Lock file not found. Starting application normally without checking...";
            logContent(s);
        }
        else
            validateLockFile(lFile);

        final String s = mComponent + " Application Starting......";
        logContent(s);
    }

    private void validateLockFile(
            File aFile)
    {

        try (
                final FileInputStream fis = new FileInputStream(aFile);)
        {
            final Properties props = new Properties();
            props.load(fis);

            final String appProcessId          = props.getProperty(PROP_KEY_APPLICATION_PROCESS_ID);
            final Date   appStartedTime        = getDateFromProperty(props, PROP_KEY_APPLICATION_STARTED_TIME);
            final Date   shutdownStartedTime   = getDateFromProperty(props, PROP_KEY_SHUTDOWN_STARTED_TIME);
            final Date   shutdownCompletedTime = getDateFromProperty(props, PROP_KEY_SHUTDOWN_COMPLETED_TIME);

            final String info                  = "Application Process id '" + appProcessId + "' Started '" + appStartedTime + "' Shutdown started '" + shutdownStartedTime + "' Shutdown Completed '"
                    + shutdownCompletedTime + "'";
            logContent(info);

            if ((appProcessId == null) || (appStartedTime == null))
            {
                final String s = mComponent + " Not a valid lock file. Unable to start the application.";
                logContent(s);
                System.exit(-1);
                return;
            }

            if (shutdownStartedTime != null)
            {
                checkForStartTime(appStartedTime, shutdownStartedTime);
                checkForShuttingdownProcess(appProcessId, appStartedTime, shutdownCompletedTime);
                compareShutdownStartAndCompleteTime(appProcessId, shutdownStartedTime, shutdownCompletedTime);
                checkFoeWaitTime(shutdownCompletedTime);
            }
            else
            {
                // Very very Rare case
                final String s = mComponent + " It seems application is already running. It started at '" + appStartedTime + "'. Unable to start the application one more time.";
                logContent(s);
                System.exit(-3);
            }
        }
        catch (final Exception e)
        {
            final String s = mComponent + " Exception while validating the lock file.";
            logContent(s, e);
            System.exit(-99);
        }
    }

    private void checkForStartTime(
            Date aAppStartedTime,
            Date aShutdownStartedTime)
    {

        if (aAppStartedTime.after(aShutdownStartedTime))
        {
            final String s = mComponent + " Not a valid lock file. Shutdown start time is greater than application start time.";
            logContent(s);
            System.exit(-1);
        }
    }

    private void checkForShuttingdownProcess(
            String aAppProcessId,
            Date aAppStartedTime,
            Date aShutdownCompletedTime)
    {

        if ((aShutdownCompletedTime == null))
        {
            final String currentProcessId = CommonUtility.getJvmProcessId();

            if (currentProcessId.equals(aAppProcessId))
            {
                final String s = mComponent + " It seems application with process id '" + CommonUtility.getJvmProcessId() + "' started at '" + aAppStartedTime
                        + "' is under shutdown process. Unable to start the application one more time.";
                logContent(s);
                System.exit(-5);
            }
        }
    }

    private void compareShutdownStartAndCompleteTime(
            String aAppProcessId,
            Date aShutdownStartedTime,
            Date aShutdownCompletedTime)
    {

        if ((aShutdownCompletedTime != null) && (aShutdownStartedTime.after(aShutdownCompletedTime)))
        {
            final String currentProcessId = CommonUtility.getJvmProcessId();

            if (currentProcessId.equals(aAppProcessId))
            {
                final String s = mComponent + " Shutdown completed time (" + aShutdownCompletedTime + ") is before shutdown started time (" + aShutdownStartedTime
                        + "). Some problem in the lock file. Unable to start the application.";
                logContent(s);
                System.exit(-7);
            }
        }
    }

    private void checkFoeWaitTime(
            Date aShutdownCompletedTime)
    {

        if (aShutdownCompletedTime != null)
        {
            final Calendar nextMinStart = Calendar.getInstance();
            nextMinStart.setLenient(false);
            nextMinStart.setTime(aShutdownCompletedTime);
            nextMinStart.add(Calendar.SECOND, RESTART_WAIT_SECONDS);

            if (nextMinStart.getTime().getTime() > System.currentTimeMillis())
            {
                final String s1 = mComponent + " Shutdown completed by " + aShutdownCompletedTime + ". Need to wait for some time to start it again. Next allowed restart is at '"
                        + DateTimeUtility.getFormattedDateTime(nextMinStart.getTime(), DateTimeFormat.DEFAULT) + "'";
                logContent(s1);
                System.exit(-9);
            }
        }
    }

    private static Date getDateFromProperty(
            Properties aProps,
            String aPropKey)
    {
        return DateTimeUtility.getDateFromString(aProps.getProperty(aPropKey), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    }

    private static void logContent(
            String aString)
    {
        logContent(aString, null);
    }

    private static void logContent(
            String aString,
            Throwable aThrowable)
    {
        log.fatal(aString, aThrowable);
        System.out.println(DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) + " " + aString);
        if (aThrowable != null)
            aThrowable.printStackTrace();
    }

    public static void main(
            String[] args)
    {
        final ProcessorInfo processorInfo = new ProcessorInfo(Component.SBCV);

        try
        {
            processorInfo.process();
        }
        catch (final Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}