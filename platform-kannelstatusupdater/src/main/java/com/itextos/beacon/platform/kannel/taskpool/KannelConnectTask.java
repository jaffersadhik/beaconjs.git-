package com.itextos.beacon.platform.kannel.taskpool;

import java.util.concurrent.Callable;

import com.itextos.beacon.platform.kannel.beans.KannelStatusInfo;
import com.itextos.beacon.platform.kannel.process.KannelConnector;

public class KannelConnectTask
        implements
        Callable<KannelStatusInfo>
{

    private final String kannelid;
    private final String kannelurl;

    public KannelConnectTask(
            String aKannelid,
            String aKannelURL)
    {
        this.kannelid  = aKannelid;
        this.kannelurl = aKannelURL;
    }

    @Override
    public KannelStatusInfo call()
    {
        return KannelConnector.getKannelStatus(kannelid, kannelurl);
    }

}