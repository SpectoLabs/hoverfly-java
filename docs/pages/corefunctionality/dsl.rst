.. _dsl:

DSL
===

The rule now has fluent DSL which allows you to build request matcher to response mappings in Java opposed to importing them as JSON.

The rule is fluent and hierarchical, allowing you to define multiple service endpoints as follows:

.. code-block:: java

    simulationSource.dsl(
        service("www.my-test.com")

            .post("/api/bookings").body("{\"flightId\": \"1\"}")
            .willReturn(created("http://localhost/api/bookings/1"))

            .get("/api/bookings/1")
            .willReturn(success("{\"bookingId\":\"1\"\}", "application/json")),

        service("www.anotherService.com")

            .put("/api/bookings/1").body(json(new Booking("foo", "bar")))
            .willReturn(success())

            .delete("/api/bookings/1")
            .willReturn(noContent())
        )

The entry point for the DSL is `HoverflyDSL.service`.  After calling this you can provide a `method` and `path`, followed by optional request components.
You can then use `willReturn` to state which response you want when there is a match, which takes `responseBuilder` object that you can instantiate directly,
or via the helper class `ResponseCreators`.