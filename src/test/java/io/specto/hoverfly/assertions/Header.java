package io.specto.hoverfly.assertions;

import java.util.AbstractMap;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class Header extends AbstractMap.SimpleImmutableEntry<String, List<String>> {

    private Header(final String key, final List<String> value) {
        super(key, value);
    }

    public static Header header(final String key, final String... values) {
        return new Header(key, newArrayList(values));
    }

    public static Header header(final String key, final List<String> values) {
        return new Header(key, values);
    }
}
