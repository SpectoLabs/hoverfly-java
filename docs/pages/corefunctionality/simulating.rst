.. _simulating:

Simulating
==========

The core of this library is the Hoverfly class, which abstracts away and orchestrates a Hoverfly instance.  A flow might be as follows:

.. code-block:: java

    try (Hoverfly hoverfly = new Hoverfly(configs(), SIMULATE)) {

        hoverfly.start();
        hoverfly.importSimulation(classpath("simulation.json"));

        // do some requests here
    }

When running Hoverfly standalone you can clean it by calling *reset* method.

.. code-block:: java
    try (Hoverfly hoverfly = new Hoverfly(configs(), SIMULATE)) {

        hoverfly.start();
        // can import or do some requests

        hoverfly.reset();

    }