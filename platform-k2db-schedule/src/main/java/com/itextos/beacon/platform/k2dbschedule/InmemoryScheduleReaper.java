package com.itextos.beacon.platform.k2dbschedule;

import com.itextos.beacon.platform.sbc.dao.DBHandler;
import com.itextos.beacon.platform.sbc.data.InmemScheduleQueue;

public class InmemoryScheduleReaper
        extends
        InmemoryQueueReaper
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final InmemoryScheduleReaper INSTANCE = new InmemoryScheduleReaper();

    }

    public static InmemoryScheduleReaper getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private InmemoryScheduleReaper()
    {
        super(InmemScheduleQueue.getInstance(), DBHandler.TABLE_NAME_SCHEDULE);
    }

}
