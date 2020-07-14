/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * <p>
 * Copyright 2016-2016 SpectoLabs Ltd.
 */
package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HoverflyMetaData {

    private static final List<String> EXCLUDED_FIELDS = Arrays.asList("hoverflyVersion", "timeExported");

    private final String schemaVersion;
    private String hoverflyVersion;
    private String timeExported;

    public HoverflyMetaData() {
        schemaVersion = "v5.1";
    }

    @JsonCreator
    public HoverflyMetaData(@JsonProperty("schemaVersion") String schemaVersion,
                            @JsonProperty("hoverflyVersion") String hoverflyVersion,
                            @JsonProperty("timeExported") String timeExported) {
        this.schemaVersion = schemaVersion;
        this.hoverflyVersion = hoverflyVersion;
        this.timeExported = timeExported;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public String getHoverflyVersion() {
        return hoverflyVersion;
    }

    public String getTimeExported() {
        return timeExported;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, EXCLUDED_FIELDS);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, EXCLUDED_FIELDS);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
