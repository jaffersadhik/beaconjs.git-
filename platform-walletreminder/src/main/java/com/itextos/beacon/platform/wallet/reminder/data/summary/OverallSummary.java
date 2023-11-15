package com.itextos.beacon.platform.wallet.reminder.data.summary;

import java.text.MessageFormat;
import java.util.Date;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.wallet.reminder.data.DataCollector;
import com.itextos.beacon.platform.wallet.reminder.utils.SortBy;
import com.itextos.beacon.platform.wallet.reminder.utils.WalletReminderProperties;

public class OverallSummary
        extends
        FullSummary
{

    @Override
    public String getSubject()
    {
        final String lDailyBalanceRemainderSubjectFormat = WalletReminderProperties.getInstance().getOverallBalanceRemainderSubjectFormat();
        return MessageFormat.format(lDailyBalanceRemainderSubjectFormat, DateTimeUtility.getFormattedDateTime(new Date(DataCollector.getInstance().getLastDataReceived()), DateTimeFormat.DEFAULT));
    }

    @Override
    SortBy getSortBy()
    {
        return WalletReminderProperties.getInstance().getOverallSortBy();
    }

    @Override
    boolean getGroupBy()
    {
        return WalletReminderProperties.getInstance().getOverallGroupBy();
    }

}