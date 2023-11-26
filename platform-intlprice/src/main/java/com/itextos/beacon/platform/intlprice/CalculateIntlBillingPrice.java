package com.itextos.beacon.platform.intlprice;

import com.itextos.beacon.inmemory.intlcredit.IntlSmsRates;

public class CalculateIntlBillingPrice
{

    private final String  mClientId;
    private final String  mCountry;
    private final String  mFromCurrency;
    private final String  mBillingCurrency;
    private final String  mRefCurrency;
    private final boolean mConvertDatewise;

    public CalculateIntlBillingPrice(
            String aClientId,
            String aCountry,
            String aFromCurrency,
            String aBillingCurrency,
            String aRefCurrency,
            boolean aConvertDatewise)
    {
        super();
        mClientId        = aClientId;
        mCountry         = aCountry;
        mFromCurrency    = aFromCurrency;
        mBillingCurrency = aBillingCurrency;
        mRefCurrency     = aRefCurrency;
        mConvertDatewise = aConvertDatewise;
    }

    public CalculateBillingPrice calculate()
    {
        final IntlSmsRates          lIntlPrice             = CurrencyUtil.getIntlPrice(mClientId, mCountry);
        final CalculateBillingPrice lCalculateBillingPrice = new CalculateBillingPrice(mClientId, lIntlPrice.getBaseSmsRate(), lIntlPrice.getBaseAddlFixedRate(), mFromCurrency, mBillingCurrency,
                mRefCurrency, mConvertDatewise);
        CurrencyUtil.getBillingPrice(lCalculateBillingPrice);
        return lCalculateBillingPrice;
    }

}