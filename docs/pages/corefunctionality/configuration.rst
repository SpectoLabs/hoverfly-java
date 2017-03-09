.. _configuration:

Configuration
=============

Hoverfly takes a config object, which contains sensible defaults if not configured.  Ports will be randomised to unused ones, which is useful on something like a CI server if you want
to avoid port clashes.
You can also set fixed port:

.. code-block:: java

    configs().proxyPort(8080)

You can also configure Hoverfly to use a remote instance which is already running

.. code-block:: java

    configs().useRemoteInstance() // localhost
    configs().useRemoteInstance("1.2.3.4") // other host name or address

You can configure Hoverfly to process requests to certain destinations / hostnames

.. code-block:: java

    configs().destination("www.test.com") // only process requests to www.test.com
    configs().destination("api") // matches destination that contains api, eg. api.test.com