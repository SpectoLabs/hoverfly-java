Overview
########
What is Hoverfly?
=================

`Hoverfly <http://hoverfly.io>`_ is a lightweight service virtualisation tool which allows you to stub / simulate HTTP(S) services. It is a proxy written in `Go <https://golang.org/>`_ which responds to HTTP(S) requests with stored responses, pretending to be it's real counterpart.

It enables you to get around common testing problems caused by external dependencies, such as non-deterministic data, flakiness, not yet implemented API's, licensing fees, slow tests and more.

Hoverfly Java
=============

Hoverfly Java is a native language binding which gives you an expressive API for managing Hoverfly in Java.  It gives you a Hoverfly class which abstracts away the binary and API calls, a DSL for creating simulations, and a JUnit integration for using it within JUnit tests.


Quick start
###########

Maven
=====

If using Maven, add the following dependency to your pom:

.. code-block:: xml

    <dependency>
        <groupId>io.specto</groupId>
        <artifactId>hoverfly-java</artifactId>
        <version>0.3.0</version>
        <scope>test</scope>
    </dependency>

Gradle
======

Or with Gradle add the dependency to your *.gradle file:

.. code-block:: groovy

   testCompile "io.specto:hoverfly-java:0.3.0"

Code example
============

The simplest way is to get started is with the JUnit rule. Just give it some valid Hoverfly JSON. Behind the scenes the JVM proxy settings will be configured to use the managed Hoverfly process, so you can just make requests as normal, only this time Hoverfly will respond instead of the real service (assuming your HTTP client respects JVM proxy settings).

.. code-block:: java

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulateMode(dsl(
        service("www.my-test.com")
            .get("/api/bookings/1")
            .willReturn(success("{\"bookingId\":\"1\"\}", "application/json"))
    ));

    @Test
    public void shouldBeAbleToGetABookingUsingHoverfly() {
        // When
        final ResponseEntity<String> getBookingResponse = restTemplate.getForEntity("http://www.my-test.com/api/bookings/1", String.class);

        // Then
        assertThat(getBookingResponse.getStatusCode()).isEqualTo(OK);
        assertThatJSON(getBookingResponse.getBody()).isEqualTo("{"\"bookingId\":\"1\"}");
    }

Core
####

Simulating
==========

The core of this library is the Hoverfly class, which abstracts away and orchestrates a Hoverfly instance.  A flow might be as follows:

.. code-block:: java

    final Hoverfly hoverfly = new Hoverfly(config(), SIMULATE);
    hoverfly.start();
    hoverfly.importSimulation(classpath("simulation.json"))
    // do some requests here
    hoverfly.stop();

Capturing
=========

The previous examples have only used Hoverfly in simulate mode. You can also run it in capture mode, meaning that requests will be made to the real service as normal,
only they will be intercepted and recorded by Hoverfly.  This can be a simple way of breaking a test's dependency on an external service; wait until you have a green
test, then switch back into simulate mode using the simulation data recorded during capture mode.

.. code-block:: java

    final Hoverfly hoverfly = new Hoverfly(config(), CAPTURE);
    hoverfly.start();
    // do some requests here
    hoverfly.exportSimulation(Paths.get("some-path/simulation.json"))
    hoverfly.stop();

Sources
=======

There are a few different potential sources for Simulations:

.. code-block:: java

    SimulationSource.classpath("simulation.json"); //classpath
    SimulationSource.url(new URL("http://www.my-service.com/simulation")); // URL
    SimulationSource.dsl(service("www.foo.com").get("/bar).willReturn(success())); // Object
    SimulationSource.simulation(new Simulation()); // Object
    SimulationSource.empty(); // None

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

            .put("/api/bookings/1").body("{\"flightId\": \"1\"\"}")
            .willReturn(success())

            .delete("/api/bookings/1")
            .willReturn(noContent())
        )

The entry point for the DSL is `HoverflyDSL.service`.  After calling this you can provide a `method` and `path`, followed by optional request components.
You can then use `willReturn` to state which response you want when there is a match, which takes `responseBuilder` object that you can instantiate directly,
or via the helper class `ResponseCreators`.


Configuration
=============

Hoverfly takes a config object, which contains sensible defaults if not configured.  Ports will be randomised to unused ones, which is useful on something like a CI server if you want
to avoid port clashes.
You can also set fixed port:

.. code-block:: java

    config().proxyPort(8080)

SSL
===

When requests pass through Hoverfly, it needs to decrypt them in order for it to persist them to a database, or to perform matching.  So you end up with SSL between Hoverfly and
the external service, and then SSL again between your client and Hoverfly.  To get this to work, Hoverfly comes with it's own self-signed certificate which has to be trusted by
your client.  To avoid the pain of configuring your keystore, Hoverfly's certificate is trusted automatically when you instantiate it.

JUnit
#####

Overview
========

An easier way to orchestrate Hoverfly is via the JUnit Rule.  This is because it will create destroy the process for you automatically, doing any cleanup work and auto-importing / exporting if required.

Simulate
========

.. code-block:: java

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(classpath("simulation.json"));

Capture
=======

.. code-block:: java

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inCaptureMode(classpath("simulation.json"));

Use @ClassRule
==============

It is recommended to boot Hoverfly once and share it across multiple tests by using a `@ClassRule` rather than `@Rule`.  This means you don't have the overhead of starting one process per test,
and also guarantees that all your system properties are set correctly before executing any of your test code.

Misc
####

Apache HttpClient
=================

This doesn't respect JVM system properties for things such as the proxy and truststore settings. Therefore when you build one you would need to:

.. code-block:: java

    HttpClient httpClient = HttpClients.createSystem();
    // or
    HttpClient httpClient = HttpClientBuilder.create().useSystemProperties().build();


Or on older versions you may need to:

.. code-block:: java

    HttpClient httpClient = new SystemDefaultHttpClient();


In addition, Hoverfly should be initialized before Apache HttpClient to ensure that the relevant JVM system properties are set before they are used by Apache library to configure the HttpClient.

There are several options to achieve this:

* Use `@ClassRule` and it guarantees that `HoverflyRule` is executed at the very start and end of the test case
* If using `@Rule` is inevitable, you should initialize the HttpClient inside your `@Before` setUp method which will be executed after `@Rule`
* As a last resort, you may want to manually configured Apache HttpClient to use custom proxy or ssl context, please check out `HttpClient examples <https://hc.apache.org/httpcomponents-client-ga/examples.html>`_


Legacy Schema Migration
=======================

If you have recorded data in the legacy schema generated before hoverfly-junit v0.1.9, you will need to run the following commands using `Hoverfly <http://hoverfly.io>`_ to migrate to the new schema:

.. code-block:: bash
    $ hoverctl start
    $ hoverctl delete simulations
    $ hoverctl import --v1 path-to-my-json/file.json
    $ hoverctl export path-to-my-json/file.json
    $ hoverctl stop
```
