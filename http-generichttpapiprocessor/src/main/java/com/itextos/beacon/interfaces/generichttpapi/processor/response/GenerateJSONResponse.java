package com.itextos.beacon.interfaces.generichttpapi.processor.response;

import com.itextos.beacon.interfaces.generichttpapi.common.data.response.ResponseObject;
import com.itextos.beacon.interfaces.generichttpapi.common.utils.Utility;

public class GenerateJSONResponse
        extends
        GenerateAbstractResponse
{

    public GenerateJSONResponse(
            String aIP)
    {
        super(aIP);
    }

    @Override
    protected String getErrorString()
    {
        return Utility.getJsonErrorResponse(getResponseDateTimeString());
    }

    @Override
    protected String getGeneralReqTypeSepecificResponse(
            ResponseObject aRo)
    {
        return Utility.getGeneralJsonResponse(aRo);
    }

}