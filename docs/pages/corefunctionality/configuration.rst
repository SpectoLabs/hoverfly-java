.. _configuration:

Configuration
=============

Hoverfly takes a config object, which contains sensible defaults if not configured.  Ports will be randomised to unused ones, which is useful on something like a CI server if you want
to avoid port clashes.
You can also set fixed port:

.. code-block:: java

    localConfigs().proxyPort(8080)


You can configure Hoverfly to process requests to certain destinations / hostnames.

.. code-block:: java

    localConfigs().destination("www.test.com") // only process requests to www.test.com
    localConfigs().destination("api") // matches destination that contains api, eg. api.test.com
    localConfigs().destination(".+.test.com") // matches destination by regex, eg. dev.test.com or stage.test.com

You can configure Hoverfly to proxy localhost requests. This is useful if the target server you are trying to simulate is running on localhost.

.. code-block:: java

    localConfigs().proxyLocalHost()

You can configure Hoverfly to capture request headers which is turned off by default:

.. code-block:: java

    localConfigs().captureHeaders("Accept", "Authorization")
    localConfigs().captureAllHeaders()

You can configure Hoverfly to run as a web server on default port 8500:

.. code-block:: java

    localConfigs().asWebServer()

You can configure Hoverfly to skip TLS verification. This option allows Hoverfly to perform "insecure" SSL connections to target server that uses invalid certificate (eg. self-signed certificate):

.. code-block:: java

    localConfigs().disableTlsVerification()


If you are developing behind a cooperate proxy, you can configure Hoverfly to use an upstream proxy:

.. code-block:: java

    localConfigs().upstreamProxy(new InetSocketAddress("127.0.0.1", 8900))


By default Hoverfly binary is copied to the system temporary folder to run. In some cases, you may not have permission to write to the temporary folder, eg. in CI server,
what you can do is to specify a different Hoverfly working directory:

.. code-block:: java

    localConfigs().binaryLocation("/absolute/path/to/hoverfly/directory")


Logging
-------
Hoverfly logs to SLF4J by default, meaning that you have control of Hoverfly logs using JAVA logging framework.
Here is an example ``logback.xml`` that directs Hoverfly ``WARN`` logs to the console:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>

    <configuration scan="false" debug="false">

        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <charset>utf-8</charset>
                <Pattern>%date{ISO8601} [%-5level] %logger{10} %msg%n</Pattern>
            </encoder>
        </appender>

        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
        <logger name="hoverfly" level="WARN" additivity="false">
            <appender-ref ref="CONSOLE" />
        </logger>

    </configuration>


You can override the default ``hoverfly`` logger name:

.. code-block:: java

    localConfigs().logger("io.test.hoverfly")

Or change the log output directly to stdout:

.. code-block:: java

    localConfigs().logToStdOut()

Hoverfly by default generates ``INFO`` logs regardless of the external SLF4J logger configs. To get debug logging, you need
to set the log level explicitly:

.. code-block:: java

    localConfigs().logLevel(LogLevel.DEBUG)


Middleware
----------

You can configure Hoverfly to use a local middleware (for more details, please check out `Hoverfly Middleware <http://hoverfly.readthedocs.io/en/latest/pages/keyconcepts/middleware.html>`_):

.. code-block:: java

    localConfigs().localMiddleware("python", "middleware/modify_response.py")

You should provide the absolute or relative path of the binary, in this case, ``python`` for running the python middleware. The second input is the middleware script file in the classpath (eg. ``test/resources`` folder)


SSL
---

When requests pass through Hoverfly, it needs to decrypt them in order for it to persist them to a database, or to perform matching.  So you end up with SSL between Hoverfly and
the external service, and then SSL again between your client and Hoverfly.  To get this to work, Hoverfly comes with it's own CA certificate which has to be trusted by
your client. To avoid the pain of configuring your keystore, Hoverfly's certificate is trusted automatically when you instantiate it.

Alternatively, you can override the default CA certificate by providing your own certificate and key files via the ``HoverflyConfig`` object, for example:

.. code-block:: java

    localConfigs()
        .overrideDefaultCaCert("ssl/ca.crt", "ssl/ca.key");

The input to these config options should be the file path relative to the classpath. Any PEM encoded certificate and key files are supported.

Mutual TLS authentication
-------------------------

For two-way or mutual SSL authentication, you can provide Hoverfly with a client certificate and a certificate key that you use to authenticate with the remote server.

.. code-block:: java

    localConfigs()
        .enableClientAuth("ssl/client-auth.crt", "ssl/client-auth.key");

The input to these config options should be the file path relative to the classpath. Any PEM encoded certificate and key files are supported.

You can enable Mutual TLS for specific hosts, for example:

.. code-block:: java

    localConfigs()
        .enableClientAuth("ssl/client-auth.crt", "ssl/client-auth.key", "foo.com", "bar.com");

You can also provide a client CA cert:

.. code-block:: java

    localConfigs()
        .enableClientAuth("ssl/client-auth.crt", "ssl/client-auth.key")
        .clientAuthCaCertPath("ssl/client-ca.crt");


Simulation Preprocessor
-----------------------

The ``SimulationPreprocessor`` interface lets you apply custom transformation to the ``Simulation`` object before importing to Hoverfly. This can be useful if you want to batch add/remove
matchers, or update matcher types, like weakening matching criteria of captured data. Here is an example of adding a glob matcher for all the paths:

.. code-block:: java

    HoverflyConfig configBuilder = new LocalHoverflyConfig().simulationPreprocessor(s ->
                s.getHoverflyData().getPairs()
                        .forEach(
                                p -> p.getRequest().getPath()
                                        .add(new RequestFieldMatcher<>(RequestFieldMatcher.MatcherType.GLOB, "/preprocessed/*"))
                        )
        );

See :ref:`extension` :ref:`extension_config` if you are using JUnit5.


Using externally managed instance
---------------------------------

It is possible to configure Hoverfly to use an existing API simulation managed externally. This could be a private
Hoverfly cluster for sharing API simulations across teams, or a publicly available API sandbox powered by Hoverfly.


You can enable this feature easily with the ``remoteConfigs()`` fluent builder. The default settings point to localhost on
default admin port 8888 and proxy port 8500.


You can point it to other host and ports

.. code-block:: java

    remoteConfigs()
        .host("10.0.0.1")
        .adminPort(8080)
        .proxyPort(8081)

Depends on the set up of the remote Hoverfly instance, it may require additional security configurations.

You can provide a custom CA certificate for the proxy.

.. code-block:: java

    remoteConfigs()
        .proxyCaCert("ca.pem") // the name of the file relative to classpath

You can configure Hoverfly to use an HTTPS admin endpoint.

.. code-block:: java

    remoteConfigs()
        .withHttpsAdminEndpoint()

You can provide the token for the custom Hoverfly authorization header, this will be used for both proxy and admin
endpoint authentication without the need for username and password.

.. code-block:: java

    remoteConfigs()
        .withAuthHeader() // this will get auth token from an environment variable named 'HOVERFLY_AUTH_TOKEN'

    remoteConfigs()
        .withAuthHeader("some.token") // pass in token directly
