package com.itextos.beacon.interfaces.generichttpapi.common.interfaces;

import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.exception.ItextosException;
import com.itextos.beacon.interfaces.generichttpapi.common.data.BasicInfo;
import com.itextos.beacon.interfaces.generichttpapi.common.data.InterfaceMessage;
import com.itextos.beacon.interfaces.generichttpapi.common.data.InterfaceRequestStatus;
import com.itextos.beacon.interfaces.generichttpapi.common.data.QueueObject;

public interface IRequestProcessor
{

    void parseBasicInfo(
            String aAuthorization)
            throws ItextosException;

    InterfaceRequestStatus validateBasicInfo();

    int getMessagesCount();

    int getNumbersCount(
            int aIndex);

    InterfaceMessage getSingleMessage();

    void setRequestStatus(
            InterfaceRequestStatus aRequestStatus);

    String generateResponse();

    InterfaceRequestStatus getMultipleMessages(
            boolean aStatus);

    /*
     * String appendCountryCode(
     * InterfaceMessage aMessage,
     * String aMobileNumber);
     */
    void pushKafkaTopic(
            String aReqType);

    boolean pushRRQueue(
            QueueObject aQueueObj,
            String aType);

    BasicInfo getBasicInfo();

    void setRequestString(
            String aRequestString);

    void resetRequestJson(
            JSONObject aRequestJson);

    int getHttpStatus();

}
