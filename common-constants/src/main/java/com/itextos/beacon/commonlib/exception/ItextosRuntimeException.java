package com.itextos.beacon.commonlib.exception;

public class ItextosRuntimeException
        extends
        RuntimeException
{

    private static final long serialVersionUID = 5501361566335613837L;

    public ItextosRuntimeException()
    {
        super();
    }

    public ItextosRuntimeException(
            String aMessage)
    {
        super(aMessage);
    }

    public ItextosRuntimeException(
            Throwable aCause)
    {
        super(aCause);
    }

    public ItextosRuntimeException(
            String aMessage,
            Throwable aCause)
    {
        super(aMessage, aCause);
    }

    public ItextosRuntimeException(
            String aMessage,
            Throwable aCause,
            boolean aEnableSuppression,
            boolean aWritableStackTrace)
    {
        super(aMessage, aCause, aEnableSuppression, aWritableStackTrace);
    }

}
