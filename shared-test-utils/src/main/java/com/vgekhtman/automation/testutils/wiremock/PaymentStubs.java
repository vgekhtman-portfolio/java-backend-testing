package com.vgekhtman.automation.testutils.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * Controls the WireMock container's stub for the payment dependency's {@code POST /charge}. WireMock
 * runs as a container (see docker-compose.yml), not embedded in the test JVM, so this talks to its
 * admin API over HTTP rather than starting a server in-process.
 */
public final class PaymentStubs {

    /** Exceeds the SUT's own HttpClient request timeout (3s, see PaymentClient) so it observes a real timeout. */
    private static final int TIMEOUT_DELAY_MILLIS = 5_000;

    private final WireMock client;

    public PaymentStubs(String adminHost, int adminPort) {
        this.client = new WireMock(adminHost, adminPort);
    }

    /** Restores the default 200/APPROVED behavior shipped in sut/wiremock/mappings/charge-success.json. */
    public void stubPaymentSuccess() {
        client.register(post(urlPathEqualTo("/charge")).willReturn(okJson("{\"status\":\"APPROVED\"}")));
    }

    /** Overrides /charge to return the given non-200 status. */
    public void stubPaymentFailure(int statusCode) {
        client.register(post(urlPathEqualTo("/charge")).willReturn(aResponse().withStatus(statusCode)));
    }

    /** Overrides /charge to hang past the SUT's own call timeout, simulating a dependency that never responds. */
    public void stubPaymentTimeout() {
        client.register(post(urlPathEqualTo("/charge")).willReturn(aResponse().withFixedDelay(TIMEOUT_DELAY_MILLIS)));
    }

    /**
     * Discards any stub registered at runtime and reloads the file-defined mappings (i.e. back to just
     * charge-success.json). Call this in an {@code @AfterEach} in any test that overrides the stub, so
     * failure-path tests don't leak into later ones.
     */
    public void reset() {
        client.resetToDefaultMappings();
    }
}
