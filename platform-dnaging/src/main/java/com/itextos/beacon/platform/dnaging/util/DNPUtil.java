package com.itextos.beacon.platform.dnaging.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.customfeatures.InmemAccountDnTypeMappingInfo;
import com.itextos.beacon.inmemory.customfeatures.InmemCustomFeatures;
import com.itextos.beacon.inmemory.customfeatures.pojo.DlrTypeInfo;
import com.itextos.beacon.inmemory.errorinfo.ErrorCodeUtil;
import com.itextos.beacon.inmemory.errorinfo.data.CarrierErrorInfo;
import com.itextos.beacon.inmemory.errorinfo.data.ErrorCategory;
import com.itextos.beacon.inmemory.errorinfo.data.PlatformErrorInfo;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class DNPUtil
{

    private static final String UNKNOWN_ERROR = "-999";
    private static final String NACK_ERROR    = PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.NACK_ERROR_CODE);
    private static final Log    log           = LogFactory.getLog(DNPUtil.class);

    private DNPUtil()
    {}

 

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    public static int getAppConfigValueAsInt(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return CommonUtility.getInteger(lAppConfiguration.getConfigValue(aConfigParamConstant.getKey()));
    }

    public static String getCutomFeatureValue(
            String aClientId,
            CustomFeatures aCustomFeature)
    {
        final InmemCustomFeatures lCustomFeatures = (InmemCustomFeatures) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_FEATURES);
        return lCustomFeatures.getValueOfCustomFeature(aClientId, aCustomFeature.getKey());
    }

    public static DlrTypeInfo getDnTypeInfo(
            String aClientId)
    {
        final InmemAccountDnTypeMappingInfo lDnTypeInfo = (InmemAccountDnTypeMappingInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.DN_PROCESS_TYPE_CONFIG);
        return lDnTypeInfo.getDnTypeInfo(aClientId);
    }

}