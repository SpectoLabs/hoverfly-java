.. _dsl:

DSL
===

Hoverfly Java has a DSL which allows you to build request matcher to response mappings in Java instead of importing them as JSON.

The DSL is fluent and hierarchical, allowing you to define multiple service endpoints as follows:

.. code-block:: java

    SimulationSource.dsl(
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

The entry point for the DSL is ``HoverflyDSL.service``.  After calling this you can provide a ``method`` and ``path``, followed by optional request components.
You can then use ``willReturn`` to state which response you want when there is a match, which takes ``ResponseBuilder`` object that you can instantiate directly,
or via the helper class ``ResponseCreators``.


Simulate network delay
----------------------
You can also simulate fixed network delay using DSL.

Global delays can be set for all requests or for a particular HTTP method:

.. code-block:: java

    SimulationSource.dsl(
        service("www.slow-service.com")
            .andDelay(3, TimeUnit.SECONDS).forAll(),

        service("www.other-slow-service.com")
            .andDelay(3, TimeUnit.SECONDS).forMethod("POST")
    )

Per-request delay can be set as follows:

.. code-block:: java

    SimulationSource.dsl(
        service("www.not-so-slow-service.com")
            .get("/api/bookings")
            .willReturn(success().withFixedDelay(1, TimeUnit.SECONDS))
        )
    )


Request/response body conversion
--------------------------------

There is currently an ``HttpBodyConverter`` interface which can be used to marshall Java objects into strings, and also set a content type header automatically.

It can be used for both request and response body, and supports JSON and XML data format out-of-the-box.

.. code-block:: java

    // For request body matcher
    .body(equalsToJson(json(myObject)))     // with default objectMapper
    .body(equalsToJson(json(myObject, myObjectMapper)))     // with custom objectMapper

    // For response body
    .body(xml(myObject))
    .body(xml(myObject, myObjectMapper))


There is an implementation which lets you write inline JSON body efficiently with single quotes.

.. code-block:: java

    .body(jsonWithSingleQuotes("{'bookingId':'1'}"))
    .body(jsonWithSingleQuotes("{'merchantName':'Jame\\'s'}"))  // escape single quote in your data if necessary
