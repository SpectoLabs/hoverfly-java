.. _ssl:

SSL
===

When requests pass through Hoverfly, it needs to decrypt them in order for it to persist them to a database, or to perform matching.  So you end up with SSL between Hoverfly and
the external service, and then SSL again between your client and Hoverfly.  To get this to work, Hoverfly comes with it's own self-signed certificate which has to be trusted by
your client.  To avoid the pain of configuring your keystore, Hoverfly's certificate is trusted automatically when you instantiate it.
