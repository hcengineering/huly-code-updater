package com.hulylabs.updater.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "patch")
public class BuildInfoPatch {
    @JacksonXmlProperty(isAttribute = true)
    public String from;
    @JacksonXmlProperty(isAttribute = true)
    public String size;
    @JacksonXmlProperty(isAttribute = true)
    public String fullFrom;
}
