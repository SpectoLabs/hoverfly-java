.. _configuration:

Configuration
=============

Hoverfly takes a config object, which contains sensible defaults if not configured.  Ports will be randomised to unused ones, which is useful on something like a CI server if you want
to avoid port clashes.
You can also set fixed port:

.. code-block:: java

    configs().proxyPort(8080)


You can configure Hoverfly to process requests to certain destinations / hostnames

.. code-block:: java

    configs().destination("www.test.com") // only process requests to www.test.com
    configs().destination("api") // matches destination that contains api, eg. api.test.com

You can configure Hoverfly to proxy localhost requests. This is useful if the target server you are trying to simulate is running on localhost.

.. code-block:: java

    configs().proxyLocalHost()

