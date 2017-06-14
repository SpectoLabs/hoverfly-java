.. _matchers:

Request field matchers
======================

Hoverfly-Java abstracts away some of the complexity of building the `request field matchers supported by Hoverfly <http://hoverfly.readthedocs.io/en/latest/pages/reference/hoverfly/request_matchers.html>`_.

By default, the DSL request builder assumes exact matching when you pass in a string.

You can also pass in a matcher created by the ``HoverflyMatchers`` factory class. 
The supported types of matchers are limited to ``exactMatch``, ``globMatch``, and ``regexMatch`` (not yet implemented) for URL, path and query.

The ``body`` method supports all the above plus JSON/XML equality matching, and JSONPath/XPath matching.

Here are some examples:

.. code-block:: java

    SimulationSource.dsl(

        service(matches("www.*-test.com"))          // Matches url with wildcard
            .get(startsWith("/api/bookings/"))      // Matches request path that starts with /api/bookings/
            .queryParam("page", any())              // Matches page query with any value
            .willReturn(success(json(booking)))

            .put("/api/bookings/1")
            .body(equalsToJson(json(booking)))      // Matches body which equals to a JSON Object
            .willReturn(success())

            .post("/api/bookings")
            .body(matchesJsonPath("$.flightId"))    // Matches body with a JSON path expression
            .willReturn(created("http://localhost/api/bookings/1"))

            .put("/api/bookings/1")
            .body(equalsToXml(xml(booking)))        // Matches body which equals to a XML object
            .willReturn(success())

            // XmlPath Matcher
            .post("/api/bookings")
            .body(matchesXPath("/flightId"))        // Matches body with a xpath expression
            .willReturn(created("http://localhost/api/bookings/1"))
    )

``HoverflyMatchers`` also provides the following matching methods:

.. code-block:: java

    HoverflyMatchers.contains("foo")
    HoverflyMatchers.endsWith("foo")


Fuzzy matching is possible for request method, query and body with these simple built-in DSL methods:

.. code-block:: java

    SimulationSource.dsl(
        service("www.booking-is-down.com")
            .anyMethod(any())
            .anyQueryParams()
            .anyBody()
            .willReturn(serverError().body("booking is down"))
    )


Headers are not used for matching unless they are specified. If you need to set a header to match on, use the ``header`` method:

.. code-block:: java

    SimulationSource.dsl(
        service("www.my-test.com")
            .post("/api/bookings")
            .body("{\"flightId\": \"1\"}")
            .header("Content-Type", "application/json")     // Count as a match when request contains this Content-Type header
            .willReturn(created("http://localhost/api/bookings/1"))
    }

When you supply a simulation source to the ``HoverflyRule``, you can enable the ``printSimulationData`` option to help debugging.
This will print the simulation JSON data used by Hoverfly to ``stdout``:

.. code-block:: java

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(simulationSource)
        .printSimulationData();