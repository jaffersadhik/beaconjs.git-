package com.itextos.beacon.commonlib.messageobject;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.RouteType;

public class ErrorObject
        extends
        BaseMessage
{

    private static final long serialVersionUID = -9195548432339659158L;

    ErrorObject(
            ClusterType aClusterType,
            InterfaceType aInterfaceType,
            InterfaceGroup aInterfaceGroup,
            MessageType aMessageType,
            MessagePriority aMessagePriority,
            RouteType aRouteType)
    {
        super(aClusterType, aInterfaceType, aInterfaceGroup, aMessageType, aMessagePriority, aRouteType, "ErrorObject");
    }

}