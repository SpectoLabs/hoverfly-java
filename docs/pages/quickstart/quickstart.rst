.. _quickstart:

Quickstart
##########

Maven
=====

If using Maven, add the following dependency to your pom:

.. parsed-literal::

    <dependency>
        <groupId>io.specto</groupId>
        <artifactId>hoverfly-java</artifactId>
        <version>\ |version|\ </version>
        <scope>test</scope>
    </dependency>

Gradle
======

Or with Gradle add the dependency to your *.gradle file:

.. parsed-literal::

   testCompile "io.specto:hoverfly-java:|version|"

Code example
============

The simplest way is to get started is with the JUnit rule. Behind the scenes the JVM proxy settings will be configured to use the managed Hoverfly process, so you can just make requests as normal, only this time Hoverfly will respond instead of the real service (assuming your HTTP client respects JVM proxy settings):

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