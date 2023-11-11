package com.itextos.beacon.platform.vcprocess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.intlrouteinfo.util.IntlRouteUtil;
import com.itextos.beacon.platform.msgflowutil.billing.CalculateBillingPrice;
import com.itextos.beacon.platform.msgflowutil.billing.CalculateIntlBillingPrice;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;
import com.itextos.beacon.platform.vcprocess.util.VCProducer;

public class IntlMsgVerifyProcessor
{

    private final Log            log = LogFactory.getLog(IntlMsgVerifyProcessor.class);

    private final MessageRequest mMessageRequest;
    private final Component      mSourceComponent;

    public IntlMsgVerifyProcessor(
            Component aSourceComponent,
            MessageRequest aMessageRequest)
    {
        this.mMessageRequest  = aMessageRequest;
        this.mSourceComponent = aSourceComponent;
    }

    public void messageProcess()
            throws Exception
    {
        if (!checkIntlRequirements())
            return;

        final String  lClientId                 = mMessageRequest.getClientId();
        final String  lCountry                  = CommonUtility.nullCheck(mMessageRequest.getCountry(), true).toUpperCase();
        final boolean lConvertDatewise          = mMessageRequest.getBillingCurrencyConversionType() == 2 ? true : false;
        final String  lPlatformBaseCurrency     = PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.BASE_CURRENCY);
        final String  lPlatformIntlBaseCurrency = PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.INTL_BASE_CURRENCY);

        if (log.isDebugEnabled())
        {
            log.debug("Country :" + lCountry);
            log.debug("Intl From Currency : " + lPlatformIntlBaseCurrency);
            log.debug("Billing Currency : " + mMessageRequest.getBillingCurrency());
            log.debug("Reffrence Currency : " + lPlatformBaseCurrency);
        }

        final CalculateIntlBillingPrice lBillingPrice = new CalculateIntlBillingPrice(lClientId, lCountry, mMessageRequest.getBillingCurrency(), lPlatformIntlBaseCurrency, lPlatformBaseCurrency,
                lConvertDatewise);
        CalculateBillingPrice           lCalculateBillingPrice;

        try
        {
        	   mMessageRequest.setBaseCurrency(lPlatformIntlBaseCurrency);
        
            lCalculateBillingPrice = lBillingPrice.calculate();

            if (log.isDebugEnabled())
            {
                log.debug("Intl Billing SMS Rate : " + lCalculateBillingPrice.getBillingSmsRate());
                log.debug("Intl Billing Additional Rate : " + lCalculateBillingPrice.getBillingAdditionalFixedRate());
            }

            mMessageRequest.setBaseSmsRate(lCalculateBillingPrice.getBaseSmsRate());
            mMessageRequest.setBaseAddFixedRate(lCalculateBillingPrice.getBaseAdditionalFixedRate());

            mMessageRequest.setBillingSmsRate(lCalculateBillingPrice.getBillingSmsRate());
            mMessageRequest.setBillingAddFixedRate(lCalculateBillingPrice.getBillingAdditionalFixedRate());

            mMessageRequest.setRefCurrency(lCalculateBillingPrice.getRefCurrency());
            mMessageRequest.setRefSmsRate(lCalculateBillingPrice.getRefSmsRate());
            mMessageRequest.setRefAddFixedRate(lCalculateBillingPrice.getRefAdditionalFixedRate());

            mMessageRequest.setBillingExchangeRate(lCalculateBillingPrice.getBillingConversionRate());
            mMessageRequest.setRefExchangeRate(lCalculateBillingPrice.getRefConversionRate());
        }
        catch (final ItextosRuntimeException e)
        {
            mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.PRICE_CONVERSION_FAILED.getStatusCode());
            mMessageRequest.setAdditionalErrorInfo("Pice Convertion failed for currency - '" + lPlatformIntlBaseCurrency + "'");
            VCProducer.sendToPlatformRejection(mSourceComponent, mMessageRequest);
            return;
        }
        catch (final Exception e)
        {
            mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.INTL_CREDIT_NOT_SPECIFIED.getStatusCode());
            mMessageRequest.setAdditionalErrorInfo("Intl Credit Not specified for Country - '" + lCountry + "'");
            VCProducer.sendToPlatformRejection(mSourceComponent, mMessageRequest);
            return;
        }

        /*
         * final ClientIntlCredits lCustomerIntlCredits =
         * MessageFlowUtil.getClientIntlCreditsInfo();
         * final IntlCredits lIntlCredits = MessageFlowUtil.getIntlCreditsInfo();
         * IntlSmsRates lIntllRates = lCustomerIntlCredits.getCustomerCredits(lClientId,
         * lCountry);
         * if (lIntllRates.getBaseSmsRate() <= 0)
         * {
         * if (log.isDebugEnabled())
         * log.debug("Client Intl Rates : " + lIntllRates);
         * if (lIntlCredits.isCountryHavingCredits(lCountry))
         * {
         * lIntllRates = lIntlCredits.getCountryCredits(lCountry);
         * if (log.isDebugEnabled())
         * log.debug("Country Intl Credits : " + lIntllRates);
         * }
         * }
         */

        /*
         * if (lIntllRates.getBaseSmsRate() <= 0)
         * {
         * mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.
         * INTL_CREDIT_NOT_SPECIFIED.getStatusCode());
         * mMessageRequest.
         * setAdditionalErrorInfo("Intl Credit Not specified for Country - '" + lCountry
         * + "'");
         * VCProducer.sendToPlatformRejection(mSourceComponent, mMessageRequest);
         * return;
         * }
         */

        final boolean isCreditCheckEnabled = CommonUtility.isEnabled(mMessageRequest.getValue(MiddlewareConstant.MW_CREDIT_CHECK));

        if (log.isDebugEnabled())
            log.debug("Credit Check Enabled : " + isCreditCheckEnabled);

        if ((mMessageRequest.getBillType() == 1) || isCreditCheckEnabled)
        {
            VCProducer.sendToNextComponent(mSourceComponent, Component.WC, mMessageRequest);

            if (log.isDebugEnabled())
                log.debug("Message Send to Wallet Topic: Successfully");
        }
        else
        {
            VCProducer.sendToNextComponent(mSourceComponent, Component.RC, mMessageRequest);

            if (log.isDebugEnabled())
                log.debug("Message  sendtoIntlRouteQueue: Successfully");
        }
    }

    private boolean checkIntlRequirements()
            throws Exception
    {
        final PlatformStatusCode lErrorCode = IntlRouteUtil.checkAndUpdateRouteBasedOnIntlRoute(mMessageRequest);

        if (lErrorCode != null)
        {
            if (log.isDebugEnabled())
                log.debug("Intl Rejected Status Code : " + lErrorCode);

            mMessageRequest.setSubOriginalStatusCode(lErrorCode.getStatusCode());
            VCProducer.sendToNextComponent(mSourceComponent, Component.PRC, mMessageRequest);
            return false;
        }

        return true;
    }

}
