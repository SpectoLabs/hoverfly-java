.. _configuration:

Configuration
=============

Hoverfly takes a config object, which contains sensible defaults if not configured.  Ports will be randomised to unused ones, which is useful on something like a CI server if you want
to avoid port clashes.
You can also set fixed port:

.. code-block:: java

    localConfigs().proxyPort(8080)


You can configure Hoverfly to process requests to certain destinations / hostnames

.. code-block:: java

    localConfigs().destination("www.test.com") // only process requests to www.test.com
    localConfigs().destination("api") // matches destination that contains api, eg. api.test.com

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

Middleware
----------

You can configure Hoverfly to use a local middleware (for more details, please check out `Hoverfly Middleware <http://hoverfly.readthedocs.io/en/latest/pages/keyconcepts/middleware.html>`_):

.. code-block:: java

    localConfigs().localMiddleware("python", "middleware/modify_response.py")

You should provide the absolute or relative path of the binary, in this case, ``python`` for running the python middleware. The second input is the middleware script file in the classpath (eg. ``test/resources`` folder)


SSL
---

When requests pass through Hoverfly, it needs to decrypt them in order for it to persist them to a database, or to perform matching.  So you end up with SSL between Hoverfly and
the external service, and then SSL again between your client and Hoverfly.  To get this to work, Hoverfly comes with it's own self-signed certificate which has to be trusted by
your client.  To avoid the pain of configuring your keystore, Hoverfly's certificate is trusted automatically when you instantiate it.

Alternatively, you can override the default SSL certificate by providing your own certificate and key files via the ``HoverflyConfig`` object, for example:

.. code-block:: java

    localConfigs()
        .sslCertificatePath("ssl/ca.crt")
        .sslKeyPath("ssl/ca.key");

The input to these config options should be the file path relative to the classpath. Any PEM encoded certificate and key files are supported.

If the default SSL certificate is overridden, hoverfly-java will not automatically set it trusted,  and it is the users' responsibility to configure SSL context for their HTTPS client.


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