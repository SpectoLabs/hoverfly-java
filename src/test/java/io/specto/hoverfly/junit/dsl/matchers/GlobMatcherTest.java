package io.specto.hoverfly.junit.dsl.matchers;


import io.specto.hoverfly.junit.core.model.FieldMatcher;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobMatcherTest {

    @Test
    public void shouldGetFieldMatcher() {

        GlobMatcher matcher = GlobMatcher.newInstance("fo*o");

        FieldMatcher fieldMatcher = matcher.getFieldMatcher();


        FieldMatcher expected = new FieldMatcher.Builder().globMatch("fo*o").build();
        assertThat(fieldMatcher).isEqualTo(expected);
    }
}