package com.hulylabs.updater.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "product")
public class Product {
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    public String name = "HulyCode";
    @JacksonXmlElementWrapper(localName = "code")
    public String code = "IC";
    public ProductChannel channel;
}