package io.specto.hoverfly.junit.api;


import com.fasterxml.jackson.databind.JsonNode;
import io.specto.hoverfly.junit.api.command.JournalIndexCommand;
import io.specto.hoverfly.junit.api.command.SortParams;
import io.specto.hoverfly.junit.api.model.CsvDataSource;
import io.specto.hoverfly.junit.api.model.ModeArguments;
import io.specto.hoverfly.junit.api.view.DiffView;
import io.specto.hoverfly.junit.api.view.HoverflyInfoView;
import io.specto.hoverfly.junit.api.view.JournalIndexView;
import io.specto.hoverfly.junit.api.view.StateView;
import io.specto.hoverfly.junit.core.HoverflyConstants;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.model.Journal;
import io.specto.hoverfly.junit.core.model.Request;
import io.specto.hoverfly.junit.core.model.Simulation;
import java.util.List;
import okhttp3.OkHttpClient;

/**
 * Http client for querying Hoverfly admin endpoints
 */
public interface HoverflyClient {


    /**
     * Set the given simulation data, overwriting any existing one.
     * @param simulation Hoverfly simulation data to set
     */
    void setSimulation(Simulation simulation);

    void setSimulation(String simulation);

    /**
     * Append the given simulation to the existing one in Hoverfly, duplicated pair will not be added
     * @param simulation Hoverfly simulation data to append
     */
    void addSimulation(Simulation simulation);

    Simulation getSimulation();

    /**
     * Get the simulation from Hoverfly as {@link JsonNode}
     * @return simulation data as {@link JsonNode}
     */
    JsonNode getSimulationJson();

    void deleteSimulation();

    Journal getJournal(int offset, int limit);

    Journal getJournal(int offset, int limit, SortParams sortParams);

    Journal searchJournal(Request request);

    void deleteJournal();

    List<JournalIndexView> getJournalIndex();

    void addJournalIndex(JournalIndexCommand journalIndexCommand);

    void deleteJournalIndex(String indexName);

    List<CsvDataSource> getCsvDataSources();

    void addCsvDataSource(CsvDataSource dataSource);

    void deleteCsvDataSource(String name);

    /**
     * Deletes all state from Hoverfly.
     */
    void deleteState();

    /**
     * Gets the state from Hoverfly.
     *
     * @return the {@link StateView}
     */
    StateView getState();

    /**
     * Deletes all state from Hoverfly and then sets the state with the specified {@link StateView}.
     *
     * @param stateView the {@link StateView}
     */
    void setState(StateView stateView);

    /**
     * Updates state in Hoverfly. Will update each state key referenced in the specified {@link StateView}.
     *
     * @param stateView the {@link StateView}
     */
    void updateState(StateView stateView);

    DiffView getDiffs();

    void cleanDiffs();

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
        private OkHttpClient client = null;

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

        public Builder withHttpClient(OkHttpClient client) {
            this.client = client;
            return this;
        }

        public HoverflyClient build() {
            if (client == null) {
                return new OkHttpHoverflyClient(scheme, host, port, authToken);
            }

            return new OkHttpHoverflyClient(scheme, host, port, client);
        }
    }

}
