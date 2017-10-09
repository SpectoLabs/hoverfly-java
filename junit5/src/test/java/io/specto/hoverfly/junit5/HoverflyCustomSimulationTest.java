package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.SslConfigurer;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

@HoverflySimulate(config = @HoverflyConfig(adminPort = 8889, proxyPort = 8890),
        source = @HoverflySimulate.Source(value = "test-service-https.json", type = HoverflySimulate.SourceType.CLASSPATH))
@ExtendWith(HoverflyExtension.class)
class HoverflyCustomSimulationTest {

    private static OkHttpClient client;

    @BeforeAll
    static void init(Hoverfly hoverfly) {
        SslConfigurer sslConfigurer = hoverfly.getSslConfigurer();
        client = new OkHttpClient.Builder()
                .sslSocketFactory(sslConfigurer.getSslContext().getSocketFactory(), sslConfigurer.getTrustManager())
                .build();
    }

    @Test
    void shouldImportSimulationFromCustomSource() throws IOException {

        final Request request = new Request.Builder().url("https://www.my-test.com/api/bookings/1")
            .build();

        final Response response = client.newCall(request).execute();

        assertThatJson(response.body().string()).node("bookingId").isStringEqualTo("1");
    }

    @Test
    void shouldUseCustomPorts(Hoverfly hoverfly) {
        assertThat(hoverfly.getHoverflyConfig().getAdminPort()).isEqualTo(8889);
        assertThat(hoverfly.getHoverflyConfig().getProxyPort()).isEqualTo(8890);
    }
}
