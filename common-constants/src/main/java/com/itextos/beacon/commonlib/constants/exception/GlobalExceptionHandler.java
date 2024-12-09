package com.itextos.beacon.commonlib.constants.exception;

import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.smslog.ExceptionLog;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        
        ExceptionLog.log("Uncaught exception in thread: " + t.getName()+" \n "+ErrorMessage.getStackTrace(e));
    }
}