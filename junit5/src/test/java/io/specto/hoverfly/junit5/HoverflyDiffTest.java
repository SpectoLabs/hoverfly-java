package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflyDiff;
import io.specto.hoverfly.junit5.api.HoverflySimulate;
import io.specto.hoverfly.junit5.api.HoverflyValidate;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class HoverflyDiffTest {

    private OkHttpClient client = new OkHttpClient();

    @Nested
    @ExtendWith(HoverflyExtension.class)
    @HoverflyDiff(
        source = @HoverflySimulate.Source(value = "hoverfly/diff/captured-simulation-for-diff.json",
            type = HoverflySimulate.SourceType.CLASSPATH),
        config = @HoverflyConfig(proxyLocalHost = true, captureHeaders = "Date")
    )
    class NestedNoDiffTest {

        @Test
        void shouldInjectCustomInstanceAsParameterWithRequiredMode(Hoverfly hoverfly) {
            assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.DIFF);
        }

        @Test
        @HoverflyValidate(reset = true)
        void shouldValidateHoverflyHealthApi(Hoverfly hoverfly) throws IOException {

            final Request request = new Request.Builder()
                .url("http://localhost:" + hoverfly.getHoverflyConfig().getAdminPort() + "/api/health")
                .build();

            final Response response = client.newCall(request).execute();

            assertThat(response.code()).isEqualTo(200);
        }
    }

    @Nested
    @ExtendWith(HoverflyExtension.class)
    @HoverflyDiff(
        source = @HoverflySimulate.Source(value = "hoverfly/diff/captured-wrong-simulation-for-diff.json",
            type = HoverflySimulate.SourceType.CLASSPATH),
        config = @HoverflyConfig(proxyLocalHost = true, captureHeaders = "Date")
    )
    class NestedDiffTest {

        @Test
        void shouldInjectCustomInstanceAsParameterWithRequiredMode(Hoverfly hoverfly) {
            assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.DIFF);
        }

        @Test
        void shouldValidateHoverflyHealthApiAndFailWhenDifferent(Hoverfly hoverfly) throws IOException {

            final Request request = new Request.Builder()
                .url("http://localhost:" + hoverfly.getHoverflyConfig().getAdminPort() + "/api/health")
                .build();

            final Response response = client.newCall(request).execute();
            assertThat(response.code()).isEqualTo(200);

            Throwable thrown = catchThrowable(() -> {
                hoverfly.assertThatNoDiffIsReported(true);
            });

            assertThat(thrown).isInstanceOf(AssertionError.class);
        }
    }
}
