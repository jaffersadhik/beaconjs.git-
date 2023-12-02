package com.itextos.beacon.http.ifb;

import com.itextos.beacon.commonlib.messageobject.IMessage;
import com.itextos.beacon.http.ifb.inmem.FallbackQ;

public class FallBackProcess
{

    private FallBackProcess()
    {}

    public static boolean sendToFallBack(
            IMessage aIMessage)
    {
        boolean isDone = false;
        while (!isDone)
            try
            {
                FallbackQ.getInstance().addMessage(aIMessage);
                isDone = true;
            }
            catch (final Exception e)
            {
                isDone = false;
            }

        return isDone;
    }

}