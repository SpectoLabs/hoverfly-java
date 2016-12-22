/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2016-2016 SpectoLabs Ltd.
 */
package io.specto.hoverfly.junit.dsl;

/**
 * Entry point to a DSL which can be used to generate a Hoverfly simulation.  Example code:
 * <pre>
 * hoverflyRule.simulate(dsl(
 *
 *      service("www.my-test.com")
 *
 *          .post("/api/bookings").body("{\"flightId\": \"1\"}")
 *          .willReturn(created("http://localhost/api/bookings/1"))
 *
 *          .get("/api/bookings/1")
 *          .willReturn(success("{\"bookingId\":\"1\"}", "application/json")),
 *
 *      service("www.other-service.com")
 *
 *          .put("/api/bookings/1").body("{\"flightId\": \"1\", \"class\": \"PREMIUM\"}")
 *          .willReturn(success())
 *
 *          .delete("/api/bookings/1")
 *          .willReturn(noContent())
 *
 *          .get("/api/bookings").query("destination=new%20york")
 *          .willReturn(success("{\"bookingId\":\"2\"}", "application/json")))
 *      )
 * );
 * </pre>
 *
 * @see StubServiceBuilder
 * @see RequestMatcherBuilder
 * @see ResponseBuilder
 * @see ResponseCreators
 */
public class HoverflyDsl {

    private HoverflyDsl() {
    }

    /**
     * Instantiates a DSL for a given service.  Once you do this, you can create request matchers to response mappings by following these semantics:
     * <p>
     * service("www.service.com").method("/some/path").willReturn(created())
     * </p>
     *
     * @param baseUrl the base URL you want all subsequent requests mappings to contain
     * @return the {@link StubServiceBuilder}
     * @see ResponseCreators
     * @see ResponseBuilder
     */
    public static StubServiceBuilder service(final String baseUrl) {
        return new StubServiceBuilder(baseUrl);
    }
}
