.. _conversion:

Conversion
==========

There is currently a `BodyConverter` interface which can be used to serialise Java objects into strings, and also set a content type header automatically

.. code-block:: java

    .body(json(new JsonObject("foo", "bar"))) // default
    .body(json(new JsonObject("foo", "bar"), myObjectMapper)) // Object mapper configured
