package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

@HoverflySimulate
@ExtendWith(HoverflyExtension.class)
class HoverflyDefaultsSimulationTest {

    private OkHttpClient client = new OkHttpClient();
    @Test
    void shouldImportSimulationFromDefaultLocation() throws IOException {

        final Request request = new Request.Builder()
            .url("http://www.my-test.com/api/bookings/1")
            .header("Content-Type", "text/plain; charset=utf-8")
            .build();

        final Response response = client.newCall(request).execute();

        String body = response.body().string();
        System.out.println("DEBUG " + response.code());
        assertThatJson(body).node("bookingId").isEqualTo("\"1\"");
    }

    @Test
    void shouldInjectDefaultInstanceAsParameter(Hoverfly hoverfly) {
        assertThat(hoverfly).isNotNull();
        assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.SIMULATE);
        assertThat(hoverfly.getHoverflyConfig().getDestination()).isEmpty();
        assertThat(hoverfly.getHoverflyConfig().isProxyLocalHost()).isFalse();
        assertThat(hoverfly.getHoverflyConfig().isRemoteInstance()).isFalse();
    }


}
