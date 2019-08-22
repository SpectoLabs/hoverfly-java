package io.specto.hoverfly.junit5;

import io.specto.hoverfly.junit5.api.HoverflySimulate;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HoverflyExtensionCustomizationTest {

    private static final String SIMULATION_SOURCE = "HoverflyExtensionCustomizationTest.json";

    @Nested
    @HoverflySimulate(source = @HoverflySimulate.Source(SIMULATION_SOURCE))
    @ExtendWith(CustomHoverflyExtension.class)
    class Default {

        @Test
        void testCustomizedSimulationSource() throws IOException {
            final Request request = new Request.Builder().url("http://www.my-test.com/api/version").build();
            final Response response = new OkHttpClient().newCall(request).execute();
            assertEquals(getExpectedApiVersion(), response.body().string());
        }

        protected String getExpectedApiVersion() {
            return "v1";
        }
    }

    @Nested
    @HoverflySimulate(source = @HoverflySimulate.Source(SIMULATION_SOURCE))
    @ExtendWith(CustomHoverflyExtension.class)
    class SuiteV2 extends Default {

        @Override
        protected String getExpectedApiVersion() {
            return "v2";
        }
    }

    @Nested
    @HoverflySimulate(source = @HoverflySimulate.Source(SIMULATION_SOURCE))
    @ExtendWith(CustomHoverflyExtension.class)
    class SuiteV3 extends Default {

        @Override
        protected String getExpectedApiVersion() {
            return "v3";
        }
    }

    static class CustomHoverflyExtension extends HoverflyExtension {

        @Override
        protected String getPath(ExtensionContext context, HoverflySimulate.Source source) {
            String path = super.getPath(context, source);

            String testClassName = context.getTestClass().orElseThrow(IllegalStateException::new).getSimpleName();
            if (testClassName.startsWith("Suite")) {
                return "simulation-suite-" + testClassName.substring(5).toLowerCase() + "/" + path;
            }

            return path;
        }
    }
}
