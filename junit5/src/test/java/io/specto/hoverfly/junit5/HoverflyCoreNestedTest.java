package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.jsonWithSingleQuotes;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.serverError;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

class HoverflyCoreNestedTest {


    private static Hoverfly hoverflyInstance;

    @Nested
    @ExtendWith(HoverflyExtension.class)
    class HoverflyBeforeEachTest {

        private OkHttpClient client = new OkHttpClient();

        @BeforeEach
        void setupHoverfly(Hoverfly hoverfly) {
            hoverflyInstance = hoverfly;
            hoverfly.simulate(dsl(
                    service("www.my-test.com")

                        // Path param for template
                        .get("/api/bookings/1")
                        .willReturn(success().body(jsonWithSingleQuotes(
                                "{'id':{{ Request.Path.[2] }},'origin':'London','destination':'Singapore','time':'2011-09-01T12:30','_links':{'self':{'href':'http://localhost/api/bookings/{{ Request.Path.[2] }}'}}}"
                        )))
                        .get("/api/bookings/error")
                        .willReturn(serverError())));
        }

        @Test
        void shouldBeAbleToSimulateABookingQuery() throws Exception {
            final Request request = new Request.Builder()
                    .url("http://www.my-test.com/api/bookings/1")
                    .header("Content-Type", "text/plain; charset=utf-8")
                    .build();

            final Response response = client.newCall(request).execute();

            assertThat(response.code()).isEqualTo(200);
            String body = response.body().string();
            assertThatJson(body).node("id").isEqualTo(1);
        }

        @Test
        void shouldBeAbleToSimulateErrorScenario() throws Exception {
            final Request request = new Request.Builder()
                    .url("http://www.my-test.com/api/bookings/error")
                    .header("Content-Type", "text/plain; charset=utf-8")
                    .build();

            final Response response = client.newCall(request).execute();

            assertThat(response.code()).isEqualTo(500);

        }
    }

    @AfterAll
    static void assertHoverflyIsTerminated() {
        assertThat(hoverflyInstance.isHealthy()).isFalse();
    }
}
