package com.vgekhtman.automation.sut.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Synchronous outbound call to the payment dependency. Deliberately has no retries or circuit breaker:
 * any non-200 response, timeout, or connection error is reported as a plain failure and the caller
 * turns that into a {@code PAYMENT_FAILED} order — a documented business outcome, not an API error.
 */
public class PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public PaymentClient(String baseUrl, ObjectMapper objectMapper) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    /** @return {@code true} only on an HTTP 200 from {@code POST {baseUrl}/charge}. */
    public boolean charge(String orderId, int amount) {
        try {
            String body = objectMapper.writeValueAsString(Map.of("orderId", orderId, "amount", amount));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/charge"))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("payment /charge for order {} returned HTTP {}", orderId, response.statusCode());
            return response.statusCode() == 200;
        } catch (Exception e) {
            log.warn("payment /charge for order {} failed: {}", orderId, e.toString());
            return false;
        }
    }
}
