package com.vgekhtman.automation.testsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.net.URI;

/**
 * Records every Rest-Assured call in the Allure report as a step ("POST /orders -> 201 Created") holding the
 * request and the response as plain-text attachments, which the report shows inline.
 *
 * <p>Replaces {@code allure-rest-assured}, whose single JSON attachment is only rendered by the Allure 3 report
 * and appears as a file download in the Allure 2 report used here.
 */
public class AllureHttpFilter implements OrderedFilter {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    public Response filter(FilterableRequestSpecification request, FilterableResponseSpecification responseSpec,
                           FilterContext context) {
        AllureLifecycle lifecycle = Allure.getLifecycle();
        String call = request.getMethod() + " " + pathOf(request.getURI());
        lifecycle.startStep(new StepResult().setName(call));
        try {
            AllureEvidence.text("Request", describe(request));
            Response response = context.next(request, responseSpec);
            AllureEvidence.text("Response", describe(response));
            String statusLine = response.getStatusLine();
            String status = statusLine.substring(statusLine.indexOf(' ') + 1);
            lifecycle.updateStep(step -> step.setName(call + " -> " + status));
            return response;
        } catch (RuntimeException e) {
            lifecycle.updateStep(step -> step.setStatus(Status.FAILED));
            throw e;
        } finally {
            lifecycle.updateStep(step -> {
                if (step.getStatus() == null) {
                    step.setStatus(Status.PASSED);
                }
            });
            lifecycle.stopStep();
        }
    }

    @Override
    public int getOrder() {
        // Last, so the request is recorded as it is finally sent (after any other filter has changed it).
        return Integer.MAX_VALUE;
    }

    private static String describe(FilterableRequestSpecification request) {
        StringBuilder text = new StringBuilder(request.getMethod()).append(' ').append(request.getURI());
        request.getHeaders().forEach(header -> text.append('\n').append(headerLine(header)));
        request.getCookies().forEach(cookie ->
                text.append("\nCookie: ").append(cookie.getName()).append('=').append(cookie.getValue()));
        Object body = request.getBody();
        if (body != null) {
            text.append("\n\n").append(prettyJson(bodyText(body)));
        }
        return text.toString();
    }

    private static String describe(Response response) {
        StringBuilder text = new StringBuilder(response.getStatusLine());
        response.getHeaders().forEach(header -> text.append('\n').append(headerLine(header)));
        String body = response.getBody().asString();
        if (!body.isEmpty()) {
            text.append("\n\n").append(prettyJson(body));
        }
        return text.toString();
    }

    private static String headerLine(Header header) {
        return header.getName() + ": " + header.getValue();
    }

    private static String bodyText(Object body) {
        if (body instanceof String || body instanceof byte[]) {
            return body instanceof byte[] bytes ? new String(bytes) : (String) body;
        }
        try {
            return JSON.writeValueAsString(body);
        } catch (Exception e) {
            return String.valueOf(body);
        }
    }

    private static String prettyJson(String text) {
        try {
            return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(JSON.readTree(text));
        } catch (Exception e) {
            return text;
        }
    }

    private static String pathOf(String uri) {
        URI parsed = URI.create(uri);
        return parsed.getRawPath() + (parsed.getRawQuery() == null ? "" : "?" + parsed.getRawQuery());
    }
}
