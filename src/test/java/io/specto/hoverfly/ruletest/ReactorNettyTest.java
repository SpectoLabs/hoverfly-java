package io.specto.hoverfly.ruletest;

import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClient;
import reactor.ipc.netty.http.client.HttpClientRequest;
import reactor.ipc.netty.http.client.HttpClientResponse;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.time.Duration;

import static io.specto.hoverfly.junit.core.SimulationSource.classpath;

public class ReactorNettyTest {

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(classpath("test-service.json"), HoverflyConfig.configs().plainHttpTunneling());

    @Test
    public void hoverflyProxy() throws Exception {
        Mono<HttpClientResponse> remote = HttpClient.create(o -> o.httpProxy(ops -> ops
                .address(new InetSocketAddress("localhost", hoverflyRule.getProxyPort()))
        ))
                .get("http://www.my-test.com/api/bookings/1",
                        HttpClientRequest::sendHeaders);

        Mono<String> page = remote
                .flatMapMany(r -> r.receive()
                        .retain()
                        .asString()
                        .limitRate(1))
                .reduce(String::concat);
        StepVerifier.create(page)
                .expectNextMatches(s -> s.contains("bookingId"))
                .expectComplete()
                .verify(Duration.ofSeconds(30));
    }
}
