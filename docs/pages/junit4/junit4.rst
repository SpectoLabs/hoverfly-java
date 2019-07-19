.. _junit4:


JUnit 4
=======

An easier way to orchestrate Hoverfly is via the JUnit Rule. This is because it will create destroy the process for you automatically, doing any cleanup work and auto-importing / exporting if required.

.. _simulatemode:

Simulate
--------

.. code-block:: java

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(classpath("simulation.json"));

.. _capturemode:
Capture
-------

.. code-block:: java

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inCaptureMode("simulation.json");

File is relative to ``src/test/resources/hoverfly``.

.. _multicapture:

Multi-Capture
-------------

.. code-block:: java

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inCaptureMode();

    public void someTest() {
        hoverflyRule.capture("firstScenario.json");

        // test
    }

    public void someTest() {
        hoverflyRule.capture("someOtherScenario.json");

        // test
    }

File is relative to ``src/test/resources/hoverfly``.

.. _incrementalcapture:

Incremental Capture
-------------------

In capture mode, ``HoverflyRule`` by default exports the simulation and overwrites any existing content of the supplied file.
This is not very helpful if you add a new test and you don't want to re-capture everything.
In this case, you can enable the incremental capture option. ``HoverflyRule`` will check and import if the supplied simulation file exists. Any new request / response pairs will be appended to the existing file during capturing.

.. code-block:: java

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inCaptureMode("simulation.json",
            localConfigs()
                    .enableIncrementalCapture());



.. _captureorsimulate:

Capture or Simulate
-------------------

You can create a Hoverfly Rule that is started in capture mode if the simulation file does not exist and in simulate mode if the file does exist.
File is relative to ``src/test/resources/hoverfly``.

.. code-block:: java

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inCaptureOrSimulationMode("simulation.json");

Use @ClassRule
--------------

It is recommended to start Hoverfly once and share it across multiple tests by using a ``@ClassRule`` rather than ``@Rule``.  This means you don't have the overhead of starting one process per test,
and also guarantees that all your system properties are set correctly before executing any of your test code.

One caveat is that if you need to have a clean state for verification, you will need to manually reset the journal before each test:

.. code-block:: java

    @Before
    public void setUp() throws Exception {

        hoverflyRule.resetJournal();

    }

However this is not required if you are calling ``hoverflyRule.simulate`` in each test to load a new set of simulations, as the journal reset is triggered automatically in this case.


