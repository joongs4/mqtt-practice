package com.example.mqttpractice.mqtt.dto;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.util.List;


@Data
@XmlRootElement(name = "configurations", namespace = "http://eurotech.com/esf/2.0")
@XmlAccessorType(XmlAccessType.FIELD)
public class EsfConfigurations {

    @XmlElement(name = "configuration", namespace = "http://eurotech.com/esf/2.0")
    private List<Configuration> configurations;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Configuration {

        @XmlAttribute
        private String pid;

        @XmlElement(name = "properties", namespace = "http://eurotech.com/esf/2.0")
        private Properties properties;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Properties {

        @XmlElement(name = "property", namespace = "http://eurotech.com/esf/2.0")
        private List<Property> properties;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Property {

        @XmlAttribute
        private String name;

        @XmlAttribute
        private String type;

        @XmlAttribute
        private boolean array;

        @XmlAttribute
        private boolean encrypted;

        @XmlElement(name = "value", namespace = "http://eurotech.com/esf/2.0")
        private String value;
    }
}
