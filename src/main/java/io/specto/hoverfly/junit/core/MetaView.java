package io.specto.hoverfly.junit.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaView {
    private final String schemaVersion = "v1";

    public String getSchemaVersion() {
        return schemaVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MetaView metaView = (MetaView) o;

        return new EqualsBuilder()
                .append(schemaVersion, metaView.schemaVersion)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(schemaVersion)
                .toHashCode();
    }
}