package com.itextos.beacon.platform.sbccore.data;

public class InmemScheduleQueue
        extends
        InmemoryQueue
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final InmemScheduleQueue INSTANCE = new InmemScheduleQueue();

    }

    public static InmemScheduleQueue getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

}