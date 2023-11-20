package com.itextos.beacon.platform.wallet.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.platform.decimalutil.PlatformDecimalUtil;
import com.itextos.beacon.platform.wallet.data.WalletInput;
import com.itextos.beacon.platform.wallet.data.WalletResult;

public class WalletDeductRefundProcessor
{

    private static final Log log = LogFactory.getLog(WalletDeductRefundProcessor.class);

    private WalletDeductRefundProcessor()
    {}

    public static WalletResult deductWalletForSMS(
            WalletInput aWalletInput)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("Dedut Wallet For SMS called for " + aWalletInput);

        try
        {
            final String  clientId  = aWalletInput.getClientId();
            final int     noOfParts = aWalletInput.getNoOfParts();
            final double  smsRate   = aWalletInput.getSmsRate();
            final double  dltRate   = aWalletInput.getDltRate();

            final boolean isIntl    = aWalletInput.isIntl();

            if (log.isDebugEnabled())
                log.debug("Is Intl Request :" + isIntl);

            if (StringUtils.isEmpty(clientId))
                throw new ItextosException("ERROR: Client id cannot be empty => " + clientId);

            if (noOfParts <= 0)
                throw new ItextosException("ERROR: Invalid value for noofsplits (has to be >0) recieved => " + noOfParts);

            if (isIntl)
            {
                if (smsRate <= 0)
                    throw new ItextosException("SMS Rate not having a proper value for internation message. Please check the configuration. " + aWalletInput);
            }
            else
                if ((smsRate <= 0) || (dltRate <= 0))
                    throw new ItextosException("SMS Rate or DLT Rate is not having a proper value for domestic message. Please check the configuration. " + aWalletInput);

            final double amountTobeDeducted = PlatformDecimalUtil.getRoundedValueForProcess((smsRate + dltRate) * noOfParts);

            if (log.isDebugEnabled())
                log.debug("Deducting amount from redis for user " + clientId + " and amount to be deducted is [" + amountTobeDeducted + "] ...");

            // STEP:1 deduct the amount from the wallet
            final double decrementedVal = RedisProcess.addOrDeduct(clientId, -amountTobeDeducted);

            sendToQueue(aWalletInput);

            if (log.isDebugEnabled())
                log.debug(
                        "Deducting amount from redis for user " + clientId + " and amount to be deducted is [" + amountTobeDeducted + "] ... SUCCESS and balance after deduction is " + decrementedVal);

            // STEP:2 check if wallet has enough balance, which can be identified based on
            // the decrementedVal (+ve - has bal, -ve - no bal)
            if (decrementedVal < 0)
            {
                final WalletInput newDeductRefundInput = aWalletInput.getRefundObject();

                log.error("Since the balance after deduction went to negative, returning the amount to wallet and failing the message. Balance after deduction " + decrementedVal + " for the input "
                        + aWalletInput);

                returnDeductedAmount(clientId, amountTobeDeducted);
                newDeductRefundInput.updateReason(newDeductRefundInput.getReason().isBlank() ? "Wallet Went Negative refund." : newDeductRefundInput.getReason() + " :: Wallet Went Negative refund.");

                sendToQueue(newDeductRefundInput);

                return WalletResult.getFailWalletResult();
            }

            return WalletResult.getSuccessWalletResult(smsRate, dltRate, noOfParts);
        }
        catch (final Exception e)
        {
            log.error("ERROR to be propagated to the caller. Failed deducting amount for " + aWalletInput, e);
            throw e;
        }
    }

    private static void sendToQueue(
            WalletInput aWalletInput)
    {
        aWalletInput.setProcessed();
        WalletKafkaProcessor.getInstance().addWalletInfo(aWalletInput);
    }

    private static void returnDeductedAmount(
            String aClientId,
            double aAmountTobeDeducted)
    {

        try
        {
            // return back the amount
            if (log.isDebugEnabled())
                log.debug("[Out of Credits] returning amount for user " + aClientId + " and amount to be returned is [" + aAmountTobeDeducted + "] ...");

            final double curBal = RedisProcess.addOrDeduct(aClientId, aAmountTobeDeducted);

            if (log.isDebugEnabled())
                log.debug(
                        "[Out of Credits] returning amount for user " + aClientId + " and amount to be returned is [" + aAmountTobeDeducted + "] ... SUCCESS and balance after deduction is " + curBal);
        }
        catch (final Exception e1)
        {
            // TODO: Data leakage. Need to be handled (Important)
            log.fatal("***Wallet Leakage*** Could not return back the wallet amount for user, amount[" + aClientId + "," + aAmountTobeDeducted + "]", e1);
        }
    }

    public static void returnAmountToWallet(
            WalletInput aWalletInput)
            throws Exception
    {

        try
        {
            final String clientId           = aWalletInput.getClientId();
            final double smsRate            = aWalletInput.getSmsRate();
            final double dltRate            = aWalletInput.getDltRate();
            final int    noOfParts          = aWalletInput.getNoOfParts();
            double       amountTobeRefunded = 0;

            if (log.isDebugEnabled())
                log.debug("Incoming params => clientid [" + clientId + "], smsrate [" + smsRate + "], dltrate [" + dltRate + "], noOfSplits [" + noOfParts + "]");

            amountTobeRefunded = PlatformDecimalUtil.getRoundedValueForProcess((smsRate + dltRate) * noOfParts);

            if (log.isInfoEnabled())
            {
                log.info("The amount to be refunded (smsrate+dltrate) * noofsplits for user " + clientId + " is [" + amountTobeRefunded + "]");
                log.debug("Refunding amount for user " + clientId + " and amount to be refunded is [" + amountTobeRefunded + "] ...");
            }

            final double curBal = RedisProcess.addOrDeduct(clientId, amountTobeRefunded);
            sendToQueue(aWalletInput);

            if (log.isDebugEnabled())
                log.debug("Refunding amount for user " + clientId + " and amount to be refunded is [" + amountTobeRefunded + "] ... SUCCESS and balance after refund is " + curBal);
        }
        catch (final Exception e)
        {
            log.error("WalletDeductRefundProcessor.returnAmountToWallet() *****ERROR to be propagated to the caller " + aWalletInput, e);
            throw e;
        }
    }

    public static boolean hasInMemoryCleared()
    {
        return WalletKafkaProcessor.getInstance().isCompleted();
    }

}
