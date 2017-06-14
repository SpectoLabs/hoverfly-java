.. _client:

Admin API client
================

Consuming Hoverfly Admin API is easy with *HoverflyClient*. It allows you to control an external Hoverfly instance, such as changing mode,
setting simulation data, etc.

You can create a default client that points to localhost:8888

.. code-block:: java

    HoverflyClient.createDefault();

You can customize the hostname and port. If the external Hoverfly requires authentication, you can provide an auth token from environment variable.

.. code-block:: java

    HoverflyClient.custom()
                .host("remote.host")
                .port(12345)
                .withAuthToken()        // this will try to get the auth token from an environment variable named 'HOVERFLY_AUTH_TOKEN'
                .build();
