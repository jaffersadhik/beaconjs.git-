package com.itextos.beacon.commonlib.daemonprocess;

import com.itextos.beacon.commonlib.exception.ItextosException;

public interface ShutdownHook
{

    void shutdown()
            throws ItextosException;

}