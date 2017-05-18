.. _misc:

Miscellaneous
=============

Apache HttpClient
-----------------

This doesn't respect JVM system properties for things such as the proxy and truststore settings. Therefore when you build one you would need to:

.. code-block:: java

    HttpClient httpClient = HttpClients.createSystem();
    // or
    HttpClient httpClient = HttpClientBuilder.create().useSystemProperties().build();


Or on older versions you may need to:

.. code-block:: java

    HttpClient httpClient = new SystemDefaultHttpClient();


In addition, Hoverfly should be initialized before Apache HttpClient to ensure that the relevant JVM system properties are set before they are used by Apache library to configure the HttpClient.

There are several options to achieve this:

* Use `@ClassRule` and it guarantees that `HoverflyRule` is executed at the very start and end of the test case
* If using `@Rule` is inevitable, you should initialize the HttpClient inside your `@Before` setUp method which will be executed after `@Rule`
* As a last resort, you may want to manually configured Apache HttpClient to use custom proxy or SSL context, please check out `HttpClient examples <https://hc.apache.org/httpcomponents-client-ga/examples.html>`_

Legacy Schema Migration
-----------------------

If you have recorded data in the legacy schema generated before hoverfly-junit v0.1.9, you will need to run the following commands using `Hoverfly <http://hoverfly.io>`_ to migrate to the new schema:

.. code-block:: bash

    $ hoverctl start
    $ hoverctl delete simulations
    $ hoverctl import --v1 path-to-my-json/file.json
    $ hoverctl export path-to-my-json/file.json
    $ hoverctl stop

V1 to V2 Schema Migration
-------------------------

Starting from Hoverfly-java v0.5.0, the simulation schema is upgraded to v2 which supports matchers. Although it is backward compatible with v1, upgrading to v2 is recommended:

.. code-block:: bash

    $ hoverctl start
    $ hoverctl delete simulations
    $ hoverctl import path-to-my-json/file.json
    $ hoverctl export path-to-my-json/file.json
    $ hoverctl stop

Using Snapshot Version
----------------------

To use snapshot version, you should include the OSS snapshot repository in your build file.

If using Maven, add the following repository to your pom:

.. parsed-literal::

    <repositories>
        <repository>
            <id>oss-snapshots</id>
                <name>OSS Snapshots</name>
            <url>https://oss.sonatype.org/content/repositories/snapshots</url>
            <snapshots>
                <enabled>true</enabled>
                </snapshots>
        </repository>
    </repositories>

Or with Gradle add the repository to your build.gradle file:

.. parsed-literal::

    repositories {
        maven {
            url 'https://oss.sonatype.org/content/repositories/snapshots'
        }
    }

