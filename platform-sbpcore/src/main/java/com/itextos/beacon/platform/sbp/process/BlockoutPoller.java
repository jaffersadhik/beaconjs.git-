package com.itextos.beacon.platform.sbp.process;

import com.itextos.beacon.platform.sbp.dao.DBPoller;

public class BlockoutPoller
        extends
        AbstractDataPoller
{

    public BlockoutPoller(
            int aAppInstanceId)
    {
        super(aAppInstanceId, DBPoller.TABLE_NAME_BLOCKOUT);
    }

}