package com.example.mqttpractice.mqtt.dto;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class KuraPayload {
    private Date timestamp;
    private Map<String, Object> metrics;
    private byte[] body;
    private Position position;

    @Data
    public static class Position {
        private Double longitude;
        private Double latitude;
        private Double altitude;
        private Double precision;
        private Double heading;
        private Double speed;
        private Date timestamp;
        private Integer satellites;
        private Integer status;
    }

    @Data
    public static class Metric {
        private String name;
        private ValueType type;
        private Object value;

        public enum ValueType {
            STRING,
            DOUBLE,
            INT32,
            FLOAT,
            INT64,
            BOOL,
            BYTES;

            public static ValueType fromObject(Object value) {
                if (value instanceof String) {
                    return STRING;
                } else if (value instanceof Double) {
                    return DOUBLE;
                } else if (value instanceof Integer) {
                    return INT32;
                } else if (value instanceof Float) {
                    return FLOAT;
                } else if (value instanceof Long) {
                    return INT64;
                } else if (value instanceof Boolean) {
                    return BOOL;
                } else if (value instanceof byte[]) {
                    return BYTES;
                } else {
                    throw new IllegalArgumentException("Unsupported value type: " + value.getClass());
                }
            }
        }
    }
}