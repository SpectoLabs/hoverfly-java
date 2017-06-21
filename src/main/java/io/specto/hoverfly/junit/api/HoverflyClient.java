package io.specto.hoverfly.junit.api;

import io.specto.hoverfly.junit.api.model.ModeArguments;
import io.specto.hoverfly.junit.api.view.HoverflyInfoView;
import io.specto.hoverfly.junit.core.HoverflyConstants;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.model.Simulation;

/**
 * Http client for querying Hoverfly admin endpoints
 */
public interface HoverflyClient {


    void setSimulation(Simulation simulation);

    Simulation getSimulation();

    HoverflyInfoView getConfigInfo();

    void setDestination(String destination);

    /**
     * Update Hoverfly mode
     * @param mode {@link HoverflyMode}
     */
    void setMode(HoverflyMode mode);

    /**
     * Update Hoverfly mode with additional arguments
     * @param mode {@link HoverflyMode}
     * @param modeArguments additional arguments such as headers to capture
     */
    void setMode(HoverflyMode mode, ModeArguments modeArguments);

    /**
     * Check Hoverfly is healthy
     * @return the status of Hoverfly
     */
    boolean getHealth();

    /**
     * Static factory method for creating a {@link Builder}
     * @return a builder for HoverflyClient
     */
    static Builder custom() {
        return new Builder();
    }

    /**
     * Static factory method for default Hoverfly client
     * @return a default HoverflyClient
     */
    static HoverflyClient createDefault() {
        return new Builder().build();
    }

    /**
     * HTTP client builder for Hoverfly admin API
     */
    class Builder {

        private String scheme = HoverflyConstants.HTTP;
        private String host = HoverflyConstants.LOCALHOST;
        private int port = HoverflyConstants.DEFAULT_ADMIN_PORT;
        private String authToken = null;

        Builder() {
        }

        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Get token from environment variable "HOVERFLY_AUTH_TOKEN" to authenticate with admin API
         * @return this Builder for further customizations
         */
        public Builder withAuthToken() {
            this.authToken = System.getenv(HoverflyConstants.HOVERFLY_AUTH_TOKEN);
            return this;
        }

        public HoverflyClient build() {
            return new OkHttpHoverflyClient(scheme, host, port, authToken);
        }
    }
}
