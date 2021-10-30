/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this classpath except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * <p>
 * Copyright 2016-2016 SpectoLabs Ltd.
 */
package io.specto.hoverfly.junit.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * An {@code OutputStream} designed to take in the Hoverfly JSON structured and log it correctly.
 */
class LoggingOutputStream extends OutputStream {

    private final ByteArrayOutputStream stream = new ByteArrayOutputStream(1000);
    private final Logger logger;

    private static final ObjectMapper LOG_PARSER = ObjectMapperFactory.getDefaultObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    LoggingOutputStream(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void write(final int b) {
        if (b == '\n') {
            final String line = stream.toString();
            stream.reset();

            try {
                final Map<?, ?> logLine = LOG_PARSER.readValue(line, Map.class);
                final String message = String.valueOf(logLine.remove("msg"));
                final String level = String.valueOf(logLine.remove("level"));
                logLine.remove("time");
                log(level, message, logLine);
            } catch (IOException e) {
                // Unparseable log message so only option is to just log the entire message
                logger.info(line);
            }

        } else {
            stream.write(b);
        }
    }

    private void log(final String level, final String message, final Map<?, ?> details) {
        switch (level) {
            case "panic":
            case "fatal":
            case "error":
                logger.error("{} {}", message, new MapToString(details));
                break;
            case "warning":
                logger.warn("{} {}", message, new MapToString(details));
                break;
            default:
                // fall through
            case "info":
                logger.info("{} {}", message, new MapToString(details));
                break;
            case "debug":
                logger.debug("{} {}", message, new MapToString(details));
                break;
        }
    }

    private static class MapToString {
        private final Map<?, ?> delegate;

        private MapToString(final Map<?, ?> delegate) {
            this.delegate = delegate;
        }

        @Override
        public String toString() {
            return delegate.isEmpty() ? "" : delegate.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(joining(" "));
        }
    }
}
