package io.specto.hoverfly.junit.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PayloadView {
    private final DataView data;
    private final MetaView meta;

    @JsonCreator
    public PayloadView(@JsonProperty("data") DataView data,

                       @JsonProperty("meta") MetaView meta) {
        this.data = data;
        this.meta = meta;
    }

    public DataView getData() {
        return data;
    }

    public MetaView getMeta() {
        return meta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PayloadView that = (PayloadView) o;

        return new EqualsBuilder()
                .append(data, that.data)
                .append(meta, that.meta)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(data)
                .append(meta)
                .toHashCode();
    }
}