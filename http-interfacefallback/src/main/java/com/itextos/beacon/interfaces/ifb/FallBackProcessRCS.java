package com.itextos.beacon.interfaces.ifb;

import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.interfaces.ifb.inmem.FallbackQ;

public class FallBackProcessRCS
{

    private FallBackProcessRCS()
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