package io.specto.hoverfly.junit.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModeArguments {

    private List<String> headersWhitelist;

    public ModeArguments() {
    }

    public ModeArguments(List<String> headersWhitelist) {
        this.headersWhitelist = headersWhitelist;
    }

    public List<String> getHeadersWhitelist() {
        return headersWhitelist;
    }

    public void setHeadersWhitelist(List<String> headersWhitelist) {
        this.headersWhitelist = headersWhitelist;
    }
}
