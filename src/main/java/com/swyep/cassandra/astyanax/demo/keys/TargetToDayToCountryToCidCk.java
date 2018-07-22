package com.swyep.cassandra.astyanax.demo.keys;

import com.netflix.astyanax.annotations.Component;

import java.util.Objects;

public class TargetToDayToCountryToCidCk {

    @Component(ordinal = 0)
    private String targetId;

    @Component(ordinal = 1)
    private String country;

    @Component(ordinal = 1)
    private String callId;

    @Component(ordinal = 1)
    private String value;

    public TargetToDayToCountryToCidCk() {
    }

    public TargetToDayToCountryToCidCk(String targetId, String country, String callId, String value) {
        this.targetId = targetId;
        this.country = country;
        this.callId = callId;
        this.value = value;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetToDayToCountryToCidCk that = (TargetToDayToCountryToCidCk) o;
        return Objects.equals(targetId, that.targetId) &&
                Objects.equals(country, that.country) &&
                Objects.equals(callId, that.callId) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetId, country, callId, value);
    }

    @Override
    public String toString() {
        return "TargetToDayToCountryToCidCk{" +
                "targetId='" + targetId + '\'' +
                ", country='" + country + '\'' +
                ", callId='" + callId + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
