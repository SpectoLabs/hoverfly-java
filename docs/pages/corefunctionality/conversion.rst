.. _conversion:

Request/response body conversion
================================

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