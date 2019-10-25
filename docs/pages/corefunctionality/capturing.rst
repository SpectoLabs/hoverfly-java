.. _capturing:

Capturing
=========

The previous examples have only used Hoverfly in simulate mode. You can also run it in capture mode, meaning that requests will be made to the real service as normal,
only they will be intercepted and recorded by Hoverfly.  This can be a simple way of breaking a test's dependency on an external service; wait until you have a green
test, then switch back into simulate mode using the simulation data recorded during capture mode.

.. code-block:: java

    try(Hoverfly hoverfly = new Hoverfly(localConfigs(), CAPTURE)) {

        hoverfly.start();

        // do some requests here

        hoverfly.exportSimulation(Paths.get("some-path/simulation.json"));
    }


By default Hoverfly captures multiple identical requests once only, but you can set the following config to enable `stateful capture <https://hoverfly.readthedocs.io/en/latest/pages/tutorials/basic/capturingsequences/capturingsequences.html>`_ which should capture all requests sequentially.

.. code-block:: java

    Hoverfly hoverfly = new Hoverfly(localConfigs().enableStatefulCapture(), CAPTURE)
