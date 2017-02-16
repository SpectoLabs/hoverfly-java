.. _sources:

Sources
=======

There are a few different potential sources for Simulations:

.. code-block:: java

    SimulationSource.classpath("simulation.json"); //classpath
    SimulationSource.url("http://www.my-service.com/simulation.json"); // URL
    SimulationSource.url(new URL("http://www.my-service.com/simulation.json")); // URL
    SimulationSource.file(Paths.get("src", "simulation.json")); // File
    SimulationSource.dsl(service("www.foo.com").get("/bar).willReturn(success())); // Object
    SimulationSource.simulation(new Simulation()); // Object
    SimulationSource.empty(); // None