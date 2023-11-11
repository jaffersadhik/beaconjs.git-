package com.itextos.beacon.platform.sbp.process;

import com.itextos.beacon.platform.sbp.dao.DBPoller;

public class SchedulePoller
        extends
        AbstractDataPoller
{

    public SchedulePoller(
            int aAppInstanceId)
    {
        super(aAppInstanceId, DBPoller.TABLE_NAME_SCHEDULE);
    }

}