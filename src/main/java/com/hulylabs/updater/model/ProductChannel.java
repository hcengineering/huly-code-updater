package com.hulylabs.updater.model;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "channel")
public class ProductChannel {
    @JacksonXmlProperty(isAttribute = true)
    public String id = "HulyCode-EAP";
    @JacksonXmlProperty(isAttribute = true)
    public String name = "HulyCode EAP";
    @JacksonXmlProperty(isAttribute = true)
    public String status = "eap";
    @JacksonXmlProperty(isAttribute = true)
    public String url = "https://github.com/hcengineering/huly-code";
    @JacksonXmlProperty(isAttribute = true)
    public String feedback = "https://github.com/hcengineering/huly-code/issues";
    @JacksonXmlProperty(isAttribute = true)
    public String majorVersion = "2025";
    @JacksonXmlProperty(isAttribute = true)
    public String licensing = "eap";
    @JacksonXmlProperty(localName = "build")
    public List<BuildInfo> builds;
}
