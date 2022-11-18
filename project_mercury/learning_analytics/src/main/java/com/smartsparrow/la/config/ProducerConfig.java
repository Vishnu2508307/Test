package com.smartsparrow.la.config;

import java.util.Objects;

import com.pearson.autobahn.common.domain.OperationalType;
import com.pearson.autobahn.producersdk.config.AutobahnProducerConfig;

public class ProducerConfig {

    private AutobahnProducerConfig autobahnProducerConfig;
    private OperationalType operationalType;

    public AutobahnProducerConfig getAutobahnProducerConfig() {
        return autobahnProducerConfig;
    }

    public ProducerConfig setAutobahnProducerConfig(AutobahnProducerConfig autobahnProducerConfig) {
        this.autobahnProducerConfig = autobahnProducerConfig;
        return this;
    }

    public OperationalType getOperationalType() {
        return operationalType;
    }

    public ProducerConfig setOperationalType(OperationalType operationalType) {
        this.operationalType = operationalType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProducerConfig that = (ProducerConfig) o;
        return Objects.equals(autobahnProducerConfig, that.autobahnProducerConfig) &&
                operationalType == that.operationalType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(autobahnProducerConfig, operationalType);
    }

    @Override
    public String toString() {
        return "ProducerConfig{" +
                "autobahnProducerConfig=" + autobahnProducerConfig +
                ", operationalType=" + operationalType +
                '}';
    }
}
