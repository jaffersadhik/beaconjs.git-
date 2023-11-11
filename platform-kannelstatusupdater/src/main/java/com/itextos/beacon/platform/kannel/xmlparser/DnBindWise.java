package com.itextos.beacon.platform.kannel.xmlparser;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(
        name = "dlr")
public class DnBindWise
{

    @XmlElement(
            name = "received")
    public String dn;

}