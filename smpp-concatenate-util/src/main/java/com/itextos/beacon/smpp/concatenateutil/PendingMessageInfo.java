package com.itextos.beacon.smpp.concatenateutil;

public class PendingMessageInfo
{

    private final String mRefNumber;
    private final int    mReceivedPartsCount;
    private int          mTotalPartsCount;
    private long         mReceivedTime;

    public  PendingMessageInfo(
            String aRefNumber,
            int aReceivedPartsCount)
    {
        super();
        mRefNumber          = aRefNumber;
        mReceivedPartsCount = aReceivedPartsCount;
    }

    public int getTotalPartsCount()
    {
        return mTotalPartsCount;
    }

    public  void setTotalPartsCount(
            int aTotalPartsCount)
    {
        mTotalPartsCount = aTotalPartsCount;
    }

    public  long getReceivedTime()
    {
        return mReceivedTime;
    }

    public void setReceivedTime(
            long aReceivedTime)
    {
        mReceivedTime = aReceivedTime;
    }

    public String getRefNumber()
    {
        return mRefNumber;
    }

    public int getReceivedPartsCount()
    {
        return mReceivedPartsCount;
    }

    public boolean isAllPartsReceived()
    {
        return mTotalPartsCount == mReceivedPartsCount;
    }

    public  boolean isExpired(
            long aTimeCanWait)
    {
        return (mReceivedTime + aTimeCanWait) < System.currentTimeMillis();
    }

    @Override
    public String toString()
    {
        return "PendingMessageInfo [mRefNumber=" + mRefNumber + ", mReceivedPartsCount=" + mReceivedPartsCount + ", mTotalPartsCount=" + mTotalPartsCount + ", mReceivedTime=" + mReceivedTime + "]";
    }

}