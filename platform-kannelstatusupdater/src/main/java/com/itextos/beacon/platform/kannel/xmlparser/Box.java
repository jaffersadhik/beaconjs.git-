package com.itextos.beacon.platform.kannel.xmlparser;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(
        name = "box")
public class Box
{

    @XmlElement(
            name = "queue")
    public String queue;

    @XmlElement(
            name = "type")
    public String type;

}