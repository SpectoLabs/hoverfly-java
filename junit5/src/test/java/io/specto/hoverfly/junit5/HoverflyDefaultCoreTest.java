package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.SimulationSource;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.serverError;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(HoverflyExtension.class)
class HoverflyDefaultCoreTest {

    private OkHttpClient client = new OkHttpClient();

    @Test
    void shouldBeAbleToSimulate(Hoverfly hoverfly) throws IOException {
        hoverfly.simulate(dsl(
                service("www.my-test.com")
                        .get("/api/bookings/1")
                        .willReturn(serverError())));

        final Request request = new Request.Builder()
                .url("http://www.my-test.com/api/bookings/1")
                .header("Content-Type", "text/plain; charset=utf-8")
                .build();

        final Response response = client.newCall(request).execute();

        assertThat(response.code()).isEqualTo(500);
    }


    @Test
    void shouldInjectDefaultHoverfly(Hoverfly hoverfly) {
        assertThat(hoverfly.isHealthy()).isTrue();
        assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.SIMULATE);
        assertThat(hoverfly.getHoverflyConfig().getDestination()).isNull();
        assertThat(hoverfly.getHoverflyConfig().isProxyLocalHost()).isFalse();
        assertThat(hoverfly.getHoverflyConfig().isRemoteInstance()).isFalse();
    }

    @Test
    void shouldNotImportAnySimulationByDefault(Hoverfly hoverfly) {
        assertThat(hoverfly.getSimulation()).isEqualTo(SimulationSource.empty().getSimulation());
    }

    @Test
    void shouldBeAbleToSwithToCaptureModeForThisTest(Hoverfly hoverfly) {
        hoverfly.setMode(HoverflyMode.CAPTURE);

        assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.CAPTURE);
    }


}
