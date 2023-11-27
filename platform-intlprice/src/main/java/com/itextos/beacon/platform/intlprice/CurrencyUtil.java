package com.itextos.beacon.platform.intlprice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.currencyconversion.CurrencyReloader;
import com.itextos.beacon.inmemory.customfeatures.InmemCustomFeatures;
import com.itextos.beacon.inmemory.intlcredit.ClientIntlCredits;
import com.itextos.beacon.inmemory.intlcredit.IntlCredits;
import com.itextos.beacon.inmemory.intlcredit.IntlSmsRates;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.decimalutility.PlatformDecimalUtil;

public class CurrencyUtil
{

    private static final Log    log               = LogFactory.getLog(CurrencyUtil.class);

    private static final String REST_OF_THE_WORLD = "ROW";

    private CurrencyUtil()
    {}

    public static void getBillingPrice(
            CalculateBillingPrice aCalculateBillingPrice)

    {
    	/*
         final double lBillingConversionRate     = getConversionRate(aCalculateBillingPrice.getFromCurrency(), aCalculateBillingPrice.getBillingCurrency(), aCalculateBillingPrice.isConvertDatewise());
        final double lRefConversionRate         = getConversionRate(aCalculateBillingPrice.getFromCurrency(), aCalculateBillingPrice.getRefCurrency(), aCalculateBillingPrice.isConvertDatewise());
		*/
    	
    	final double lBillingConversionRate     = 1.0D;
    	final double lRefConversionRate         = 1.0D;
    	
        final double billingSmsRate             = getRoundedValueForProcess(aCalculateBillingPrice.getBaseSmsRate());
        final double billingAdditionalFixedRate = getRoundedValueForProcess(aCalculateBillingPrice.getBaseAdditionalFixedRate());
        final double refSmsRate                 = getRoundedValueForProcess(aCalculateBillingPrice.getBaseSmsRate()) * lRefConversionRate;
        final double refAdditionalFixedRate     = getRoundedValueForProcess(aCalculateBillingPrice.getBaseAdditionalFixedRate()) * lRefConversionRate;

        aCalculateBillingPrice.setBillingConversionRate(lBillingConversionRate);
        aCalculateBillingPrice.setBillingSmsRate(billingSmsRate);
        aCalculateBillingPrice.setBillingAdditionalFixedRate(billingAdditionalFixedRate);
        aCalculateBillingPrice.setRefConversionRate(lRefConversionRate);
        aCalculateBillingPrice.setRefSmsRate(refSmsRate);
        aCalculateBillingPrice.setRefAdditionalFixedRate(refAdditionalFixedRate);
    }

    public static double getConversionRate(
            String aFromCurrency,
            String aToCurrency,
            boolean aConvertDatewise)
    {
        CurrencyReloader lCurrencyReloader;

        if (aConvertDatewise)
            lCurrencyReloader = (CurrencyReloader) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CURRENCY_CONVERSION_DATE_BASED);
        else
            lCurrencyReloader = (CurrencyReloader) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CURRENCY_CONVERSION_MONTH_BASED);

        final double lConversionRate = getRoundedValueForProcess(lCurrencyReloader.getConversionRate(aFromCurrency, aToCurrency));

        if (lConversionRate == CurrencyReloader.INVALID_CONVERSION_VALUE)
            throw new ItextosRuntimeException("Currency Conversion mapping is not found for from '" + aFromCurrency + "' to '" + aToCurrency + "'");

        return lConversionRate;
    }

    static IntlSmsRates getIntlPrice(
            String aClientId,
            String aCountry)
    {
        final ItextosClient lItextosClient = new ItextosClient(aClientId);
        IntlSmsRates        lIntlSmsRates  = getClientSmsRates(lItextosClient, aCountry);

        if (lIntlSmsRates == null)
        {
            log.error("Intl rate for the country '" + aCountry + "' or Rest of World is not specified for the Client '" + aClientId + "'");
            final String useDefaultIntlRate = getDefaultIntlRateCustomFeature(lItextosClient);

            if ((useDefaultIntlRate == null) || CommonUtility.isEnabled(useDefaultIntlRate))
            {
                lIntlSmsRates = getDefaultIntlSmsRates(aCountry);

                if (lIntlSmsRates == null)
                {
                    final String s = "Intl rate for the country '" + aCountry + "' or Rest of World is not specified in the intl_rates table";

                    log.error(s);
                    throw new ItextosRuntimeException(s);
                }
            }
            else
            {
                final String s = "Intl rate for the country '" + aCountry + "' or Rest of World is not specified for the Client '" + aClientId
                        + "' Using Default Intl Rate is disbaled for the Client '" + aClientId + "'";

                log.error(s);
                throw new ItextosRuntimeException(s);
            }
        }

        return lIntlSmsRates;
    }

    private static IntlSmsRates getDefaultIntlSmsRates(
            String aCountry)
    {
        final IntlCredits lIntlCredits    = (IntlCredits) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INTL_COUNTRY_RATES);
        IntlSmsRates      lCountryCredits = lIntlCredits.getCountryCredits(aCountry);

        if (lCountryCredits == null)
            lCountryCredits = lIntlCredits.getCountryCredits(REST_OF_THE_WORLD);

        return lCountryCredits;
    }

    private static String getDefaultIntlRateCustomFeature(
            ItextosClient aItextosClient)
    {
        final InmemCustomFeatures lCustomFeatures       = (InmemCustomFeatures) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_FEATURES);
        String                    lValueOfCustomFeature = lCustomFeatures.getValueOfCustomFeature(aItextosClient.getClientId(), CustomFeatures.USE_DEFAULT_INTL_PRICE.getKey());

        if (lValueOfCustomFeature == null)
            lValueOfCustomFeature = lCustomFeatures.getValueOfCustomFeature(aItextosClient.getAdmin(), CustomFeatures.USE_DEFAULT_INTL_PRICE.getKey());

        if (lValueOfCustomFeature == null)
            lValueOfCustomFeature = lCustomFeatures.getValueOfCustomFeature(aItextosClient.getSuperAdmin(), CustomFeatures.USE_DEFAULT_INTL_PRICE.getKey());

        return lValueOfCustomFeature;
    }

    private static IntlSmsRates getClientSmsRates(
            ItextosClient aItextosClient,
            String aCountry)
    {
        final ClientIntlCredits lClientIntlCredits = (ClientIntlCredits) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CLIENT_INTL_RATES);

        IntlSmsRates            lCustomerCredits   = lClientIntlCredits.getCustomerCredits(aItextosClient.getClientId(), aCountry);

        if (lCustomerCredits == null)
            lCustomerCredits = lClientIntlCredits.getCustomerCredits(aItextosClient.getClientId(), REST_OF_THE_WORLD);

        if (lCustomerCredits == null)
            lCustomerCredits = lClientIntlCredits.getCustomerCredits(aItextosClient.getAdmin(), aCountry);

        if (lCustomerCredits == null)
            lCustomerCredits = lClientIntlCredits.getCustomerCredits(aItextosClient.getAdmin(), REST_OF_THE_WORLD);

        if (lCustomerCredits == null)
            lCustomerCredits = lClientIntlCredits.getCustomerCredits(aItextosClient.getSuperAdmin(), aCountry);

        if (lCustomerCredits == null)
            lCustomerCredits = lClientIntlCredits.getCustomerCredits(aItextosClient.getSuperAdmin(), aCountry);

        if (lCustomerCredits == null)
            lCustomerCredits = lClientIntlCredits.getCustomerCredits(aItextosClient.getSuperAdmin(), REST_OF_THE_WORLD);

        return lCustomerCredits;
    }

    private static double getRoundedValueForProcess(
            double aDouble)
    {
        return PlatformDecimalUtil.getRoundedValueForProcess(aDouble);
    }

}