.. _ssl:

SSL
===

When requests pass through Hoverfly, it needs to decrypt them in order for it to persist them to a database, or to perform matching.  So you end up with SSL between Hoverfly and
the external service, and then SSL again between your client and Hoverfly.  To get this to work, Hoverfly comes with it's own self-signed certificate which has to be trusted by
your client.  To avoid the pain of configuring your keystore, Hoverfly's certificate is trusted automatically when you instantiate it.

Alternatively, you can override the default SSL certificate by providing your own certificate and key files via the HoverflyConfig object, for example:

.. code-block:: java

    configs()
        .sslCertificatePath("ssl/ca.crt")
        .sslKeyPath("ssl/ca.key");

The input to these config options should be the file path relative to the classpath. Any PEM encoded certificate and key files are supported.

If the default SSL certificate is overridden, hoverfly-java will not automatically set it trusted,  and it is the users' responsibility to configure SSL context for their HTTPS client.
