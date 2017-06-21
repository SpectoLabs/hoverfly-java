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
     * Static factory method for creating a {@link HoverflyClientBuilder}
     * @return a builder for HoverflyClient
     */
    static HoverflyClientBuilder custom() {
        return new HoverflyClientBuilder();
    }

    /**
     * Static factory method for default Hoverfly client
     * @return a default HoverflyClient
     */
    static HoverflyClient createDefault() {
        return new HoverflyClientBuilder().build();
    }

    /**
     * HTTP client builder for Hoverfly admin API
     */
    class HoverflyClientBuilder {

        private String scheme = HoverflyConstants.HTTP;
        private String host = HoverflyConstants.LOCALHOST;
        private int port = HoverflyConstants.DEFAULT_ADMIN_PORT;
        private String authToken = null;

        HoverflyClientBuilder() {
        }

        public HoverflyClientBuilder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public HoverflyClientBuilder host(String host) {
            this.host = host;
            return this;
        }

        public HoverflyClientBuilder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Get token from environment variable "HOVERFLY_AUTH_TOKEN" to authenticate with admin API
         * @return this HoverflyClientBuilder for further customizations
         */
        public HoverflyClientBuilder withAuthToken() {
            this.authToken = System.getenv(HoverflyConstants.HOVERFLY_AUTH_TOKEN);
            return this;
        }

        public HoverflyClient build() {
            return new OkHttpHoverflyClient(scheme, host, port, authToken);
        }
    }
}
