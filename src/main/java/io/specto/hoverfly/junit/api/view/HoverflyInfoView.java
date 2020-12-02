package io.specto.hoverfly.junit.api.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.specto.hoverfly.junit.api.model.ModeArguments;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HoverflyInfoView {

    private final String destination;
    private final String mode;
    private final ModeArguments modeArguments;
    private final Usage usage;
    private final Middleware middleware;
    private final String upstreamProxy;
    private final String version;


    @JsonCreator
    public HoverflyInfoView(@JsonProperty("destination") String destination,
                            @JsonProperty("mode") String mode,
                            @JsonProperty("arguments") ModeArguments modeArguments,
                            @JsonProperty("usage") Usage usage,
                            @JsonProperty("middleware") Middleware middleware,
                            @JsonProperty("upstream-proxy") String upstreamProxy,
                            @JsonProperty("version") String version) {
        this.destination = destination;
        this.mode = mode;
        this.modeArguments = modeArguments;
        this.usage = usage;
        this.middleware = middleware;
        this.upstreamProxy = upstreamProxy;
        this.version = version;
    }


    public String getDestination() {
        return destination;
    }

    public String getMode() {
        return mode;
    }

    public ModeArguments getModeArguments() {
        return modeArguments;
    }

    public Usage getUsage() {
        return usage;
    }

    public Middleware getMiddleware() {
        return middleware;
    }

    public String getUpstreamProxy() {
        return upstreamProxy;
    }

    public String getVersion() {
        return version;
    }

    private static class Usage {

        private Counters counters;

        public Usage() {
        }

        public Counters getCounters() {
            return counters;
        }

        public void setCounters(Counters counters) {
            this.counters = counters;
        }
    }

    private static class Middleware {
        private String binary;
        private String script;
        private String remote;

        public String getBinary() {
            return binary;
        }

        public void setBinary(String binary) {
            this.binary = binary;
        }

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }

        public String getRemote() {
            return remote;
        }

        public void setRemote(String remote) {
            this.remote = remote;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Counters {
        private int capture;
        private int modify;
        private int simulate;
        private int synthesize;
        private int spy;

        public Counters() {
        }

        public int getCapture() {
            return capture;
        }

        public void setCapture(int capture) {
            this.capture = capture;
        }

        public int getModify() {
            return modify;
        }

        public void setModify(int modify) {
            this.modify = modify;
        }

        public int getSimulate() {
            return simulate;
        }

        public void setSimulate(int simulate) {
            this.simulate = simulate;
        }

        public int getSynthesize() {
            return synthesize;
        }

        public void setSynthesize(int synthesize) {
            this.synthesize = synthesize;
        }

        public int getSpy() {
            return spy;
        }

        public void setSpy(int spy) {
            this.spy = spy;
        }
    }
}
