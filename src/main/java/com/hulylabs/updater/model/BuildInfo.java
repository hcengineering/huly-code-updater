package com.hulylabs.updater.model;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@JacksonXmlRootElement(localName = "build")
public class BuildInfo implements Serializable {
    @JacksonXmlProperty(isAttribute = true)
    public String number;
    @JacksonXmlProperty(isAttribute = true)
    public String version;
    @JacksonXmlProperty(isAttribute = true)
    public String fullNumber;
    @JacksonXmlCData(value = true)
    public String message;
    public BuildInfoButton button;
    @JacksonXmlProperty(localName = "patch")
    public List<BuildInfoPatch> patches = Collections.emptyList();

    public BuildInfo() {
    }

    public BuildInfo(String number, String message, BuildInfoPatch patch) {
        this.number = number;
        this.version = "2025.1 " + number;
        this.fullNumber = number;
        this.message = message;
        this.button = new BuildInfoButton();
        this.patches = List.of(patch);
    }

    public BuildInfo(String number, String message) {
        this.number = number;
        this.version = "2025.1 " + number;
        this.fullNumber = number;
        this.message = message;
        this.button = new BuildInfoButton();
    }
}
