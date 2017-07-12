.. _templating:

Response Templating
===================

If you need to build a response dynamically based on the request data, you can do so using templating:


.. code-block:: java

    hoverfly.importSimulation(dsl(
        service("www.my-test.com")

            // Using path in template
            .get("/api/bookings/1")
            .willReturn(success().body(jsonWithSingleQuotes(
                    "{'id':{{ Request.Path.[2] }},'origin':'London','destination':'Singapore','_links':{'self':{'href':'http://localhost/api/bookings/{{ Request.Path.[2] }}'}}}"
            )))

            // Using query Param in template
            .get("/api/bookings")
                .queryParam("destination", "London")
                .queryParam("page", any())
            .willReturn(success().body(jsonWithSingleQuotes(
                    "{'id':'1', 'destination':'{{ Request.QueryParam.destination }}','_links':{'self':{'href':'http://localhost/api/bookings?page={{ Request.QueryParam.page }}'}}}"
            )))));


The first example sets the id in response body based on the last path segment in the request using ``{{ Request.Path.[2] }}``.
The second example sets some response body data based on the request param using ``{{ Request.QueryParam.destination }}`` and ``{{ Request.QueryParam.page }}``.

For more details, please check out the `Hoverfly documentation <https://hoverfly.readthedocs.io/en/latest/pages/keyconcepts/templating/templating.html>`_.