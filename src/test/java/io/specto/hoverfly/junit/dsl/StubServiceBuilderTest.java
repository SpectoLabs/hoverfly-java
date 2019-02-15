package io.specto.hoverfly.junit.dsl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import io.specto.hoverfly.junit.core.model.Request;
import io.specto.hoverfly.junit.core.model.RequestFieldMatcher;
import io.specto.hoverfly.junit.core.model.RequestResponsePair;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.assertj.core.data.MapEntry;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.specto.hoverfly.assertions.Assertions.assertThat;
import static io.specto.hoverfly.junit.core.model.RequestFieldMatcher.newExactMatcher;
import static io.specto.hoverfly.junit.core.model.RequestFieldMatcher.newGlobMatcher;
import static io.specto.hoverfly.junit.core.model.RequestFieldMatcher.newRegexMatcher;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.*;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.*;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class StubServiceBuilderTest {

    @Test
    public void shouldExtractHttpsUrlScheme() {

        final Set<RequestResponsePair> pairs = service("https://www.my-test.com").get("/").willReturn(response()).getRequestResponsePairs();

        assertThat(pairs).hasSize(1);
        RequestResponsePair pair = pairs.iterator().next();
        assertThat(pair.getRequest().getDestination()).containsExactly(newExactMatcher("www.my-test.com"));
        assertThat(pair.getRequest().getScheme()).containsExactly(newExactMatcher("https"));
    }

    @Test
    public void shouldIgnoreSchemeIfItIsNotSet() {
        final Set<RequestResponsePair> pairs = service("www.my-test.com").get("/").willReturn(response()).getRequestResponsePairs();

        assertThat(pairs).hasSize(1);
        RequestResponsePair pair = pairs.iterator().next();
        assertThat(pair.getRequest().getDestination()).containsExactly(newExactMatcher("www.my-test.com"));
        assertThat(pair.getRequest().getScheme()).isNull();

    }

    @Test
    public void shouldExtractHttpScheme() {
        final Set<RequestResponsePair> pairs = service("http://www.my-test.com").get("/").willReturn(response()).getRequestResponsePairs();

        assertThat(pairs).hasSize(1);
        RequestResponsePair pair = pairs.iterator().next();
        assertThat(pair.getRequest().getDestination()).containsExactly(newExactMatcher("www.my-test.com"));
        assertThat(pair.getRequest().getScheme()).containsExactly(newExactMatcher("http"));
    }


    @Test
    public void shouldBuildExactMatchersForMethod() {
        assertExactMatcherForMethod(service("").get("/"), "GET");
        assertExactMatcherForMethod(service("").post("/"), "POST");
        assertExactMatcherForMethod(service("").put("/"), "PUT");
        assertExactMatcherForMethod(service("").patch("/") , "PATCH");
        assertExactMatcherForMethod(service("").delete("/"), "DELETE");
        assertExactMatcherForMethod(service("").options("/"), "OPTIONS");
        assertExactMatcherForMethod(service("").connect("/"), "CONNECT");
        assertExactMatcherForMethod(service("").head("/"), "HEAD");
    }

    @Test
    public void shouldBuildPathMatcher() {
        assertPathMatcher(service("").get(matches("/api/*/booking")), "/api/*/booking");
        assertPathMatcher(service("").post(matches("/api/*/booking")), "/api/*/booking");
        assertPathMatcher(service("").put(matches("/api/*/booking")), "/api/*/booking");
        assertPathMatcher(service("").patch(matches("/api/*/booking")), "/api/*/booking");
        assertPathMatcher(service("").delete(matches("/api/*/booking")), "/api/*/booking");
        assertPathMatcher(service("").options(matches("/api/*/booking")), "/api/*/booking");
        assertPathMatcher(service("").connect(matches("/api/*/booking")), "/api/*/booking");
        assertPathMatcher(service("").head(matches("/api/*/booking")), "/api/*/booking");
    }

    @Test
    public void shouldBuildAnyMethodRequest() {
        final Set<RequestResponsePair> pairs = service("www.base-url.com").anyMethod("/").willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getMethod()).isNull();
    }

    @Test
    public void shouldBuildAnyMethodRequestWithPathMatcher() {
        final Set<RequestResponsePair> pairs = service("www.base-url.com").anyMethod(matches("/api/*/booking")).willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getPath()).containsExactly(newGlobMatcher("/api/*/booking"));
    }

    @Test
    public void shouldBuildExactQueryMatcher() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/").queryParam("foo", "bar")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query).containsExactly(MapEntry.entry("foo", Collections.singletonList(newExactMatcher("bar"))));
    }

    // TODO Not supported
//    @Test
//    public void shouldBuildQueryMatcherWithFuzzyKey() {
//        // When
//        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/").queryParam(any(), "bar")
//                .willReturn(response()).getRequestResponsePairs();
//
//        // Then
//        assertThat(pairs).hasSize(1);
//        Map<String, List<RequestFieldMatcher>> query = Iterables.getLast(pairs).getRequest().getQuery();
//        assertThat(query.getRegexMatch()).isEqualTo(".*=bar");
//        assertThat(query).containsExactly(MapEntry.entry("foo", Collections.singletonList(newExactMatcher("bar"))));
//    }

    @Test
    public void shouldBuildQueryMatcherWithFuzzyValue() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/").queryParam("foo", matches("b*r"))
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query).containsExactly(MapEntry.entry("foo", Collections.singletonList(newGlobMatcher("b*r"))));
    }


    // TODO Not supported
//    @Test
//    public void shouldBuildQueryMatcherWithFuzzyKeyAndValue() {
//        // When
//        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/").queryParam(endsWith("token"), any())
//                .willReturn(response()).getRequestResponsePairs();
//
//        // Then
//        assertThat(pairs).hasSize(1);
//        Map<String, List<RequestFieldMatcher>> query = Iterables.getLast(pairs).getRequest().getQuery();
//        assertThat(query.getRegexMatch()).isEqualTo(".*token$=.*");
//        assertThat(query.getExactMatch()).isNull();
//    }

    @Test
    public void shouldBuildExactQueryWithMultipleKeyValuePairs() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .queryParam("page", 1)
                .queryParam("size", 10)
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query).containsOnly(
                MapEntry.entry("page", Collections.singletonList(newExactMatcher("1"))),
                MapEntry.entry("size", Collections.singletonList(newExactMatcher("10")))
        );
    }

    @Test
    public void shouldBuildExactQueryForKeyWithMultipleValues() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .queryParam("category", "food", "drink")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query).containsExactly(
                MapEntry.entry("category", Collections.singletonList(newExactMatcher("food;drink")))
        );
    }

    @Test
    public void shouldBuildQueryWithMultipleFuzzyMatchers() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .queryParam("page", any())
                .queryParam("size", any())
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query).containsOnly(
                MapEntry.entry("page", Collections.singletonList(newRegexMatcher(".*"))),
                MapEntry.entry("size", Collections.singletonList(newRegexMatcher(".*")))
        );
    }

    @Test
    public void shouldBuildQueryWithBothExactAndFuzzyMatchers() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .queryParam("page", any())
                .queryParam("category", "food")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query).containsOnly(
                MapEntry.entry("page", Collections.singletonList(newRegexMatcher(".*"))),
                MapEntry.entry("category", Collections.singletonList(newExactMatcher("food")))
        );
    }

    @Test
    public void shouldBuildQueryParamMatcherThatIgnoresValue() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .queryParam("page")
                .queryParam("size")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query).containsOnly(
                MapEntry.entry("page", Collections.singletonList(newRegexMatcher(".*"))),
                MapEntry.entry("size", Collections.singletonList(newRegexMatcher(".*")))
        );
    }

    @Test
    public void shouldBuildAnyQueryMatcher() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .anyQueryParams()
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query).isNull();
    }

    @Test
    public void shouldBuildEmptyQueryMatcherWhenQueryParamIsNotSet() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query).isEmpty();
    }

    @Test
    public void shouldNotEncodeSpacesInQueryParams() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .queryParam("destination", "New York")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> query = Iterables.getLast(pairs).getRequest().getQuery();
        assertThat(query).containsExactly(
                MapEntry.entry("destination", Collections.singletonList(newExactMatcher("New York")))
        );
    }

    @Test
    public void shouldBuildExactHeaderMatcher() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/").header("foo", "bar")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> headers = Iterables.getLast(pairs).getRequest().getHeaders();
        assertThat(headers).containsExactly(MapEntry.entry("foo", Collections.singletonList(newExactMatcher("bar"))));
    }

    @Test
    public void shouldBuildHeadersMatcherWithFuzzyValue() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/").header("foo", matches("b*r"))
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> headers = Iterables.getLast(pairs).getRequest().getHeaders();
        assertThat(headers).containsExactly(MapEntry.entry("foo", Collections.singletonList(newGlobMatcher("b*r"))));
    }

    @Test
    public void shouldBuildMultipleHeaderMatchers() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer abc")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> headers = Iterables.getLast(pairs).getRequest().getHeaders();
        assertThat(headers).containsOnly(
                MapEntry.entry("Content-Type", Collections.singletonList(newExactMatcher("application/json"))),
                MapEntry.entry("Authorization", Collections.singletonList(newExactMatcher("Bearer abc")))
        );
    }

    @Test
    public void shouldBuildExactHeaderMatcherForKeyWithMultipleValues() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .header("Content-Type", "application/json", "text/html")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> headers = Iterables.getLast(pairs).getRequest().getHeaders();
        assertThat(headers).containsExactly(
                MapEntry.entry("Content-Type", Collections.singletonList(newExactMatcher("application/json;text/html")))
        );
    }

    @Test
    public void shouldBuildHeaderWithMultipleFuzzyMatchers() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .header("Content-Type", any())
                .header("Authorization", any())
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> headers = Iterables.getLast(pairs).getRequest().getHeaders();
        assertThat(headers).containsOnly(
                MapEntry.entry("Content-Type", Collections.singletonList(newRegexMatcher(".*"))),
                MapEntry.entry("Authorization", Collections.singletonList(newRegexMatcher(".*")))
        );
    }

    @Test
    public void shouldBuildHeadersWithBothExactAndFuzzyMatchers() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .header("Content-Type", "application/json")
                .header("Authorization", matches("Bearer *"))
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> headers = Iterables.getLast(pairs).getRequest().getHeaders();
        assertThat(headers).containsOnly(
                MapEntry.entry("Content-Type", Collections.singletonList(newExactMatcher("application/json"))),
                MapEntry.entry("Authorization", Collections.singletonList(newGlobMatcher("Bearer *")))
        );
    }

    @Test
    public void shouldBuildHeaderMatcherThatIgnoresValue() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .header("Content-Type")
                .header("Authorization")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> headers = Iterables.getLast(pairs).getRequest().getHeaders();
        assertThat(headers).containsOnly(
                MapEntry.entry("Content-Type", Collections.singletonList(newRegexMatcher(".*"))),
                MapEntry.entry("Authorization", Collections.singletonList(newRegexMatcher(".*")))
        );
    }

    @Test
    public void shouldBuildAnyHeaderWhenHeaderIsNotSet() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").get("/")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Map<String, List<RequestFieldMatcher>> headers = Iterables.getLast(pairs).getRequest().getHeaders();
        assertThat(headers).isEmpty();
    }


    @Test
    public void shouldBuildAnyBodyMatcher() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").post("/")
                .anyBody()
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        List<RequestFieldMatcher> body = Iterables.getLast(pairs).getRequest().getBody();
        assertThat(body).isNull();
    }

    @Test
    public void shouldBuildEmptyBodyMatcherWhenBodyIsNotSet() {
        // When
        final Set<RequestResponsePair> pairs = service("www.base-url.com").post("/")
                .willReturn(response()).getRequestResponsePairs();

        // Then
        assertThat(pairs).hasSize(1);
        Request request = Iterables.getLast(pairs).getRequest();
        assertThat(request).hasBodyContainsOneExactMatcher("");
    }

    @Test
    public void shouldAutomaticallyMarshallJson() {
        // When
        final RequestResponsePair requestResponsePair =
                service("www.some-service.com")
                        .post("/path")
                        .body(json(new SomeJson("requestFieldOne", "requestFieldTwo")))
                        .willReturn(success(json(new SomeJson("responseFieldOne", "responseFieldTwo"))))
                        .getRequestResponsePairs()
                        .iterator()
                        .next();

        // Then
        assertThat(requestResponsePair.getRequest())
                .hasBodyContainsOneExactMatcher("{\"firstField\":\"requestFieldOne\",\"secondField\":\"requestFieldTwo\"}");

        assertThat(requestResponsePair.getResponse())
                .hasBody("{\"firstField\":\"responseFieldOne\",\"secondField\":\"responseFieldTwo\"}");
    }

    @Test
    public void shouldByAbleToConfigureTheObjectMapperWhenMarshallingJson() throws JsonProcessingException {
        // When
        final ObjectMapper objectMapper = spy(new ObjectMapper());

        final RequestResponsePair requestResponsePair =
                service("www.some-service.com")
                        .post("/path")
                        .body(json(new SomeJson("requestFieldOne", "requestFieldTwo"), objectMapper))
                        .willReturn(success())
                        .getRequestResponsePairs()
                        .iterator()
                        .next();

        // Then
        assertThat(requestResponsePair.getRequest())
                .hasBodyContainsOneExactMatcher("{\"firstField\":\"requestFieldOne\",\"secondField\":\"requestFieldTwo\"}");

        verify(objectMapper).writeValueAsString(new SomeJson("requestFieldOne", "requestFieldTwo"));
    }

    @Test
    public void shouldBuildTemplatedResponseByDefault() {

        final RequestResponsePair pair = service("www.base-url.com")
                .get("/")
                .willReturn(success().body("{\"id\":{{ Request.Path.[2] }}"))
                .getRequestResponsePairs()
                .iterator().next();

        assertThat(pair.getResponse().isTemplated()).isTrue();
    }

    @Test
    public void shouldBeAbleToDisableTemplatedResponse() {

        final RequestResponsePair pair = service("www.base-url.com")
                .get("/")
                .willReturn(success().body("{\"id\":{{ Request.Path.[2] }}").disableTemplating())
                .getRequestResponsePairs()
                .iterator().next();

        assertThat(pair.getResponse().isTemplated()).isFalse();
    }

    @Test
    public void shouldBeAbleToSetTransitionStates() {

        final RequestResponsePair pair = service("www.base-url.com")
            .get("/")
            .willReturn(success().body("{\"id\":{{ Request.Path.[2] }}")
                .andSetState("firstStateKey", "firstStateValue")
                .andSetState("secondStateKey", "secondStateValue"))
            .getRequestResponsePairs()
            .iterator().next();

        assertThat(pair.getResponse().getTransitionsState())
            .containsOnly(
                entry("firstStateKey", "firstStateValue"),
                entry("secondStateKey", "secondStateValue"));
    }

    @Test
    public void shouldBeAbleToSetStatesToRemove() {

        final RequestResponsePair pair = service("www.base-url.com")
            .get("/")
            .willReturn(success().body("{\"id\":{{ Request.Path.[2] }}")
                .andRemoveState("firstStateToRemove")
                .andRemoveState("secondStateToRemove"))
            .getRequestResponsePairs()
            .iterator().next();

        assertThat(pair.getResponse().getRemovesState())
            .containsExactlyInAnyOrder("firstStateToRemove", "secondStateToRemove");
    }

    @Test
    public void shouldBeAbleToSetRequiredStates() {

        final RequestResponsePair pair = service("https://www.my-test.com")
            .get("/")
            .withState("firstStateKey", "firstStateValue")
            .withState("secondStateKey", "secondStateValue")
            .willReturn(response()).getRequestResponsePairs()
            .iterator().next();

        assertThat(pair.getRequest().getRequiresState())
            .containsOnly(
                entry("firstStateKey", "firstStateValue"),
                entry("secondStateKey", "secondStateValue"));
    }

    private void assertExactMatcherForMethod(RequestMatcherBuilder builder, String method) {
        final Set<RequestResponsePair> pairs = builder.willReturn(response()).getRequestResponsePairs();

        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getMethod()).containsExactly(newExactMatcher(method));
    }

    private void assertPathMatcher(RequestMatcherBuilder builder, String globValue) {
        final Set<RequestResponsePair> pairs = builder.willReturn(response()).getRequestResponsePairs();

        assertThat(pairs).hasSize(1);
        assertThat(Iterables.getLast(pairs).getRequest().getPath()).containsExactly(newGlobMatcher(globValue));
    }

    public static final class SomeJson {

        private final String firstField;
        private final String secondField;

        SomeJson(final String firstField, final String secondField) {
            this.firstField = firstField;
            this.secondField = secondField;
        }

        public String getFirstField() {
            return firstField;
        }

        public String getSecondField() {
            return secondField;
        }

        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}