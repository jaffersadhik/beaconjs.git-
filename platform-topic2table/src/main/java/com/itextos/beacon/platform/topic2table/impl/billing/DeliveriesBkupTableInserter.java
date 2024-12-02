package com.itextos.beacon.platform.topic2table.impl.billing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.ProcessorInfo;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.platform.topic2table.impl.AbstractTableInserterWrapper;

public class DeliveriesBkupTableInserter
        extends
        AbstractTableInserterWrapper
{

    private static final Log       log            = LogFactory.getLog(DeliveriesBkupTableInserter.class);
    private static final Component THIS_COMPONENT = Component.T2DB_DELIVERIES_BKUP;

    public DeliveriesBkupTableInserter(
            String aThreadName,
            Component aComponent,
            ClusterType aPlatformCluster,
            String aTopicName,
            ConsumerInMemCollection aConsumerInMemCollection,
            int aSleepInMillis)
    {
        super(aThreadName, aComponent, aPlatformCluster, aTopicName, aConsumerInMemCollection, aSleepInMillis, Table2DBInserterId.DELIVERIES_BKUP);
    }

    public static void start()
    {

        try
        {
            final ProcessorInfo lProcessor = new ProcessorInfo(THIS_COMPONENT);
            lProcessor.process();
        }
        catch (final Exception e)
        {
            log.error("Exception while starting component '" + THIS_COMPONENT + "'", e);
        }
    }

    public static void main(
            String[] args)
    {
        start();
    }

}