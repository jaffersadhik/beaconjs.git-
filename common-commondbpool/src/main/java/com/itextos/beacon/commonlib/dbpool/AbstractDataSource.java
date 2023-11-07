package com.itextos.beacon.commonlib.dbpool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract class AbstractDataSource
{

    private static final Log         log               = LogFactory.getLog(AbstractDataSource.class);
    protected final JndiInfo         conId;
    protected final DataSourceConfig dataSourceConfig;
    protected boolean                dataSourceCreated = false;

    AbstractDataSource(
            JndiInfo aConId,
            DataSourceConfig aDataSourceConfig)
    {
        conId            = aConId;
        dataSourceConfig = aDataSourceConfig;
    }

    boolean isDataSourceCreated()
    {
        return dataSourceCreated;
    }

    boolean createPool()
    {

        if (!dataSourceCreated)
        {
            if (log.isInfoEnabled())
                log.info("Creating connection pool : connection ID : '" + conId + "'");

            dataSourceCreated = createDataSource();

            if (log.isDebugEnabled())
                log.debug("Creating connection pool : connection ID : '" + conId + "' Status : '" + dataSourceCreated + "'");
        }

        return dataSourceCreated;
    }

    abstract boolean createDataSource();

    JndiInfo getConnectionID()
    {
        return conId;
    }

    DataSourceConfig getDataSourceConfig()
    {
        return dataSourceConfig;
    }

}