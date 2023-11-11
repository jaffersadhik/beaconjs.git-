package com.itextos.beacon.platform.wallet.reminder.quartz.jobs;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.exception.ItextosException;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.wallet.reminder.data.DataCollector;
import com.itextos.beacon.platform.wallet.reminder.data.summary.FullSummary;
import com.itextos.beacon.platform.wallet.reminder.email.EmailObject;
import com.itextos.beacon.platform.wallet.reminder.email.FromEmail;
import com.itextos.beacon.platform.wallet.reminder.email.RmlEmailSender;
import com.itextos.beacon.platform.wallet.reminder.email.ToEmail;
import com.itextos.beacon.platform.wallet.reminder.utils.WalletReminderProperties;

public abstract class WalletBalanceJob
        implements
        Job
{

    private static final Log log = LogFactory.getLog(WalletBalanceJob.class);

    @Override
    public void execute(
            JobExecutionContext aContext)
            throws JobExecutionException
    {
        if (log.isDebugEnabled())
            log.debug("Job Execution Time : " + DateTimeUtility.getFormattedDateTime(aContext.getFireTime(), DateTimeFormat.DEFAULT));

        final long startTime = System.currentTimeMillis();

        doProcess(aContext);

        final long endTime = System.currentTimeMillis();

        if (log.isInfoEnabled())
            log.info("Time taken to complete the job " + (endTime - startTime) + " millis");
    }

    private void doProcess(
            JobExecutionContext aContext)
    {

        try
        {
            DataCollector.getInstance().getData();
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the data from servers.", e);
            return;
        }

        process(aContext);
        printNextFireTime(aContext);
    }

    abstract void printNextFireTime(
            JobExecutionContext aContext);

    abstract void process(
            JobExecutionContext aContext);

    static void sendAdminEmail(
            FullSummary aFullSummary)
            throws ItextosException
    {
        final FromEmail    fromEmail       = new FromEmail(WalletReminderProperties.getInstance().getFromName(), WalletReminderProperties.getInstance().getFromEmailId());
        final EmailObject  emailObject     = new EmailObject(fromEmail, aFullSummary.getSubject(), aFullSummary.getSummary());

        final List<String> lAdminEmailList = WalletReminderProperties.getInstance().getAdminEmailList();
        for (final String toEmail : lAdminEmailList)
            emailObject.addTo(new ToEmail(toEmail, toEmail));

        final RmlEmailSender mailSender = new RmlEmailSender(emailObject);
        mailSender.sendEmail();
    }

}
