package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit5.api.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(HoverflyExtension.class)
@HoverflySpy(
        source = @HoverflySimulate.Source(value = "hoverfly/test-service-https.json",
                type = HoverflySimulate.SourceType.CLASSPATH)
)
public class HoverflySpyTest {

    private OkHttpClient client = new OkHttpClient();

        @Test
        void shouldInjectCustomInstanceAsParameterWithRequiredMode(Hoverfly hoverfly) {
            assertThat(hoverfly.getMode()).isEqualTo(HoverflyMode.SPY);
        }

    @Test
    void shouldValidateHoverflyHealthApiAndFailWhenDifferent(Hoverfly hoverfly) throws IOException {

        final Request request = new Request.Builder()
                .url("http://localhost:" + hoverfly.getHoverflyConfig().getAdminPort())
                .build();

        final Response response = client.newCall(request).execute();
        assertThat(response.code()).isEqualTo(200);
    }
//    @Test
//    void shouldValidateHoverflyHealthApi() throws IOException {
//
//        final Request request = new Request.Builder()
//                .url("https://hoverfly.io")
//                .build();
//
//        final Response response = client.newCall(request).execute();
//
//        assertThat(response.code()).isEqualTo(200);
//    }
}
