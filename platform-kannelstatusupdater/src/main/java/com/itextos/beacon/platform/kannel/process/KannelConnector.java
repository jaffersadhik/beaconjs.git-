package com.itextos.beacon.platform.kannel.process;

import java.io.StringReader;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.httputility.BasicHttpConnector;
import com.itextos.beacon.commonlib.httputility.HttpResult;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.kannel.beans.KannelStatusInfo;
import com.itextos.beacon.platform.kannel.xmlparser.Gateway;

public class KannelConnector
{

    private static Log log = LogFactory.getLog(KannelConnector.class);

    private KannelConnector()
    {}

    public static KannelStatusInfo getKannelStatus(
            String aKannelID,
            String aKannelURL)
    {
        final KannelStatusInfo kannelStatusInfo = new KannelStatusInfo(aKannelID);

        try
        {
            final URL url = new URL(aKannelURL);
            kannelStatusInfo.setKannelIp(url.getHost());
            kannelStatusInfo.setKannelPort(url.getPort());
            final HttpResult httpResult = BasicHttpConnector.connect(aKannelURL, true);

            if (log.isDebugEnabled())
                log.debug("Kannel Http Result : " + httpResult);

            if (httpResult.isSuccess())
            {
                final String xml = httpResult.getResponseString();

                if (log.isDebugEnabled())
                    log.debug("Response from server for the Kannel id :" + aKannelID + " URL : " + aKannelURL + " is : '" + xml + "'");

                if (!"".equals(CommonUtility.nullCheck(xml, true)))
                    setKannelStatus(xml, kannelStatusInfo);
                else
                    kannelStatusInfo.setKannelAvailable(false);
            }
            else
            {
                kannelStatusInfo.setKannelAvailable(false);

                if (log.isDebugEnabled())
                    log.debug("Kannel Status for Url:'" + aKannelURL + "', " + kannelStatusInfo.isKannelAvailable());
            }
        }
        catch (final Exception e)
        {
            log.error("Error while getting the Kannel Status", e);
            kannelStatusInfo.setKannelAvailable(false);
        }
        return kannelStatusInfo;
    }

    private static void setKannelStatus(
            String xml,
            KannelStatusInfo kannelStatusInfo)
    {

        try
        {
            final JAXBContext  context = JAXBContext.newInstance(Gateway.class);
            final Unmarshaller um      = context.createUnmarshaller();
            final Gateway      gateway = (Gateway) um.unmarshal(new StringReader(xml));

            if (log.isDebugEnabled())
                log.debug("Parsed the XML successfully");

            kannelStatusInfo.setUpTime(gateway.getUptime());
            kannelStatusInfo.setSMS(gateway.getSMSInfo());
            kannelStatusInfo.setDN(gateway.getDNMap());
            kannelStatusInfo.setSmscList(gateway.getSMSCS());
            kannelStatusInfo.generateSummary();

            final long queueSize = gateway.getSMSBoxQueued();

            if (queueSize == -1)
            {
                kannelStatusInfo.setKannelAvailable(false);
                kannelStatusInfo.setSmsBoxQueue(0);
            }
            else
            {
                kannelStatusInfo.setKannelAvailable(true);
                kannelStatusInfo.setSmsBoxQueue(queueSize);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while parsing and getting the details from XML", e);
            kannelStatusInfo.setKannelAvailable(false);
        }
    }

}