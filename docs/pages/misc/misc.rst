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

* Use ``@ClassRule`` and it guarantees that ``HoverflyRule`` is executed at the very start and end of the test case
* If using ``@Rule`` is inevitable, you should initialize the HttpClient inside your ``@Before`` setUp method which will be executed after ``@Rule``
* As a last resort, you may want to manually configured Apache HttpClient to use custom proxy or SSL context, please check out `HttpClient examples <https://hc.apache.org/httpcomponents-client-ga/examples.html>`_

OkHttpClient
------------
If you are using `OkHttpClient <http://square.github.io/okhttp/>`_ to make HTTPS requests, you will need to configure it to use the custom ``SSLContext`` and ``TrustManager`` that supports Hoverfly CA cert:

.. code-block:: java

    SslConfigurer sslConfigurer = hoverflyRule.getSslConfigurer();
    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .sslSocketFactory(sslConfigurer.getSslContext().getSocketFactory(), sslConfigurer.getTrustManager())
            .build();

Spock Framework
---------------

If you are testing with BDD and `Spock Framework <http://spockframework.org/>`_, you could also use Hoverfly-Java JUnit Rule. Just initialize a `HoverflyRule` in the Specification, and annotate it with `@ClassRule` and `@Shared` which indicates the `HoverflyRule` is shared among all the feature methods:

.. code-block:: java

    class MySpec extends Specification {

        @Shared
        @ClassRule
        HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode()

        // Feature methods

        def setup() {
            // Reset the journal before each feature if you need to do a verification
            hoverflyRule.resetJournal()
        }
    }


Legacy Schema Migration
-----------------------

If you have recorded data in the legacy schema generated before hoverfly-junit v0.1.9, you will need to run the following commands using `Hoverfly <http://hoverfly.io>`_ to migrate to the new schema:

.. code-block:: bash

    $ hoverctl start
    $ hoverctl import --v1 path-to-my-json/file.json
    $ hoverctl export path-to-my-json/file.json
    $ hoverctl stop

Migration to the latest (V5) schema
-----------------------------------

Starting from Hoverfly-java v0.11.0, the simulation schema is upgraded to v5 which is a big leap in terms of the maturity of header and query matchers,
and the possibility to introduce more request matchers without any breaking changes in the future. Although Hoverfly is designed to be backward compatible with all the previous schemas, upgrading to v5 is highly recommended:

.. code-block:: bash

    $ hoverctl start
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

