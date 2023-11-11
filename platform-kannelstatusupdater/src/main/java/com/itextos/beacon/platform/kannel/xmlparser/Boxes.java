package com.itextos.beacon.platform.kannel.xmlparser;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(
        name = "boxes")
public class Boxes
{

    @XmlElementRef
    Box box;

}