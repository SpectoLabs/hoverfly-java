.. _remotehoverfly:

Using externally managed instance
=================================

It is possible to configure Hoverfly to use an existing API simulation managed externally. This could be a private
Hoverfly cluster for sharing API simulations across teams, or a publicly available API sandbox powered by Hoverfly.


You can enable this feature easily with the configs fluent builder. The default settings point to localhost on
default admin port 8888 and proxy port 8500.

.. code-block:: java

    configs().remote()

You can point it to other host and ports

.. code-block:: java

    configs()
        .remote()
        .host("10.0.0.1")
        .adminPort(8080)
        .proxyPort(8081)

Depends on the set up of the remote Hoverfly instance, it may require additional security configurations.

You can provide a custom CA certificate for the proxy.

.. code-block:: java

    configs()
        .remote()
        .proxyCaCert("ca.pem") // the name of the file relative to classpath

You can configure Hoverfly to use an HTTPS admin endpoint.

.. code-block:: java

    configs()
        .remote()
        .withHttpsAdminEndpoint()

You can provide the token for the custom Hoverfly authorization header, this will be used for both proxy and admin
endpoint authentication without the need for username and password.

.. code-block:: java

    configs()
        .remote()
        .withAuthHeader() // this will get auth token from an environment variable named 'HOVERFLY_AUTH_TOKEN'

    configs()
        .remote()
        .withAuthHeader("some.token") // pass in token directly