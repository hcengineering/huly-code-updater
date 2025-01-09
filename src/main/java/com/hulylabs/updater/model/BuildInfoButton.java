package com.hulylabs.updater.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "button")
public class BuildInfoButton {
    @JacksonXmlProperty(isAttribute = true)
    public String name = "Download";
    @JacksonXmlProperty(isAttribute = true)
    public String url = "https://dist.huly.io/code/index.html";
    @JacksonXmlProperty(isAttribute = true)
    public boolean download = true;
}
