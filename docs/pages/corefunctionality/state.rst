.. _state:

Stateful simulation
===================

Hoverfly Java supports stateful simulation which means a mocked service could return different responses for the same request
depends on the current state(s). (for more details, please check out `Hoverfly State <http://hoverfly.readthedocs.io/en/latest/pages/keyconcepts/state/state.html>`_).

Stateful simulation can be setup via DSL. A simple example is that you can simulate a GET booking API to return a good
response if state is not set, but calling the DELETE API will trigger the state transition, and any subsequent requests to
the GET API will results a 404 error.

.. code-block:: java

    SimulationSource.dsl(
        service("www.service-with-state.com")

            .get("/api/bookings/1")
            .willReturn(success("{\"bookingId\":\"1\"\}", "application/json"))

            .delete("/api/bookings/1")
            .willReturn(success().andSetState("Booking", "Deleted"))

            .get("/api/bookings/1")
            .withState("Booking", "Deleted")
            .willReturn(notFound())
    )

The following state control methods are available:

* ``withState(key, value)`` add a condition for the request to be matched based on the given state (which is a key-value pair)
* ``andSetState(key, value)`` applies to the response to set the state
* ``andRemoteState(key)`` can be applied to the response to remove a state by key.

You can also chain multiple state methods to create more complex scenarios.