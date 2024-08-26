package io.specto.hoverfly.junit5;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.SslConfigurer;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

// Should inherit the Hoverfly annotation from the base test.
class HoverflyBaseExtensionTest extends HoverflyBaseTest {

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

}
