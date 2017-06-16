package io.specto.hoverfly.junit.core.model;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.specto.hoverfly.junit.api.view.HoverflyInfoView;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class HoverflyInfoViewTest {

    @Test
    public void shouldNotSerializeNullField() throws Exception {

        HoverflyInfoView hoverflyInfoView = new HoverflyInfoView("www.test.com", null, null, null, null);

        ObjectMapper objectMapper = new ObjectMapper();

        String json = objectMapper.writeValueAsString(hoverflyInfoView);

        JSONAssert.assertEquals("{\"destination\": \"www.test.com\"}", json, true);

    }
}