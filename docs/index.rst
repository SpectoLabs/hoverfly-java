
.. image:: logo-large.png

|
|

.. image:: https://circleci.com/gh/SpectoLabs/hoverfly-java.svg?style=shield
    :target: https://circleci.com/gh/SpectoLabs/hoverfly-java

.. image:: https://readthedocs.org/projects/hoverfly-java/badge/?version=latest
    :target: https://hoverfly-java.readthedocs.io/en/latest/

.. image:: https://codecov.io/gh/spectolabs/hoverfly-java/branch/master/graph/badge.svg
    :target: https://codecov.io/gh/spectolabs/hoverfly-java


.. image:: https://img.shields.io/maven-central/v/io.specto/hoverfly-java.svg
    :target: https://mvnrepository.com/artifact/io.specto/hoverfly-java




|

What is Hoverfly Java?
######################

`Hoverfly <http://hoverfly.io>`_ is a lightweight service virtualisation tool which allows you to stub / simulate HTTP(S) services. It is a proxy written in `Go <https://golang.org/>`_ which responds to HTTP(S) requests with stored responses, pretending to be it's real counterpart.

It enables you to get around common testing problems caused by external dependencies, such as non-deterministic data, flakiness, not yet implemented API's, licensing fees, slow tests and more.

**Hoverfly Java** is a native language binding which gives you an expressive API for managing Hoverfly in Java.  It gives you a Hoverfly class which abstracts away the binary and API calls, a :ref:`dsl` for creating simulations, and a :ref:`junit` integration for using it within JUnit tests.

Hoverfly Java is developed and maintained by `SpectoLabs <https://specto.io>`_.

.. include:: pages/quickstart/quickstart.rst

|

Contents
========

.. toctree::
    :maxdepth: 2

    pages/quickstart/quickstart
    pages/corefunctionality/corefunctionality
    pages/junit/junit
    pages/misc/misc
