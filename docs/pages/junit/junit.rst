.. _junit:


JUnit
=====

An easier way to orchestrate Hoverfly is via the JUnit Rule. This is because it will create destroy the process for you automatically, doing any cleanup work and auto-importing / exporting if required.

Simulate
--------

.. code-block:: java

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(classpath("simulation.json"));

Capture
-------

.. code-block:: java

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inCaptureMode("simulation.json");

File is relative to ``src/test/resources/hoverfly``.

Capture or Simulate
-------------------

You can create a Hoverfly Rule that is started in capture mode if the simulation file does not exist and in simulate mode if the file does exist.
File is relative to ``src/test/resources/hoverfly``.

.. code-block:: java

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inCaptureOrSimulationMode("simulation.json");

Use @ClassRule
--------------

It is recommended to boot Hoverfly once and share it across multiple tests by using a `@ClassRule` rather than `@Rule`.  This means you don't have the overhead of starting one process per test,
and also guarantees that all your system properties are set correctly before executing any of your test code.
