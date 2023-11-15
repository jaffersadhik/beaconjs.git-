package com.itextos.beacon.interfaces.generichttpapi.common.interfaces;

import com.itextos.beacon.interfaces.generichttpapi.common.data.InterfaceRequestStatus;

public interface IResponseProcessor
{

    void setStatusObject(
            InterfaceRequestStatus aRequestStatus,
            String aClientId,
            String aReqType,
            String aOffset);

    void setServletContext(
            String aServletContext);

    void setUname(
            String aUserName);

    String getRequestType();

    String getServletContext();

    String generateResponse();

    int getHttpStatus();

}