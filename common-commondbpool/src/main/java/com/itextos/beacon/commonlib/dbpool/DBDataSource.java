package com.itextos.beacon.commonlib.dbpool;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class DBDataSource
        extends
        AbstractDataSource
{

    private static final Log log              = LogFactory.getLog(DBDataSource.class);

    private BasicDataSource  mBasicDataSource = null;

    DBDataSource(
            JndiInfo aConId,
            DataSourceConfig aDataSourceConfig)
    {
        super(aConId, aDataSourceConfig);
    }

    @Override
    boolean createDataSource()
    {

        try
        {
            mBasicDataSource = BasicDataSourceFactory.createDataSource(dataSourceConfig.getConfigAsProperties());
            return true;
        }
        catch (final Exception e)
        {
            log.error("Unable to create a database connection.", e);
            e.printStackTrace();
        }
        return false;
    }

    Connection getConnection()
            throws SQLException
    {
        if (!isDataSourceCreated())
            createPool();

        return mBasicDataSource.getConnection();
    }

    void closeDataSource()
            throws Exception
    {

        try
        {
            mBasicDataSource.close();
        }
        catch (final Exception e)
        {
            throw e;
        }
    }

    @Override
    public String toString()
    {
        return "DBConnectionPool [mBasicDataSource=" + mBasicDataSource + "]";
    }

    int getInitialSize()
    {
        if (!isDataSourceCreated())
            return -1;
        return mBasicDataSource.getInitialSize();
    }

    int getMaxIdle()
    {
        if (!isDataSourceCreated())
            return -1;
        return mBasicDataSource.getMaxIdle();
    }

    int getMaxTotal()
    {
        if (!isDataSourceCreated())
            return -1;
        return mBasicDataSource.getMaxTotal();
    }

    int getMinIdle()
    {
        if (!isDataSourceCreated())
            return -1;
        return mBasicDataSource.getMinIdle();
    }

    int getNumActive()
    {
        if (!isDataSourceCreated())
            return -1;
        return mBasicDataSource.getNumActive();
    }

    int getNumIdle()
    {
        if (!isDataSourceCreated())
            return -1;
        return mBasicDataSource.getNumIdle();
    }

    int getMaxOpenPreparedStatements()
    {
        if (!isDataSourceCreated())
            return -1;
        return mBasicDataSource.getMaxOpenPreparedStatements();
    }

}