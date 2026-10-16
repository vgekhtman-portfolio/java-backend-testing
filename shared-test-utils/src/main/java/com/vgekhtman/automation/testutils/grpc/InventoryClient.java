package com.vgekhtman.automation.testutils.grpc;

import com.vgekhtman.automation.inventory.grpc.CheckStockRequest;
import com.vgekhtman.automation.inventory.grpc.CheckStockResponse;
import com.vgekhtman.automation.inventory.grpc.InventoryServiceGrpc;
import com.vgekhtman.automation.inventory.grpc.ReserveStockRequest;
import com.vgekhtman.automation.inventory.grpc.ReserveStockResponse;
import com.vgekhtman.automation.testsupport.AllureEvidence;
import com.vgekhtman.automation.testutils.CustomSut;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Blocking client for the custom SUT's InventoryService; every call carries a deadline so a hang fails fast.
 * The stub and message classes are generated from {@code proto/.../inventory.proto} at build time (into
 * {@code proto/build/generated}), so they are not in the source tree.
 */
public final class InventoryClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);
    private static final long DEADLINE_SECONDS = 10;

    private final ManagedChannel channel;
    private final InventoryServiceGrpc.InventoryServiceBlockingStub stub;

    private InventoryClient(ManagedChannel channel) {
        this.channel = channel;
        this.stub = InventoryServiceGrpc.newBlockingStub(channel);
    }

    public static InventoryClient connect() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(CustomSut.grpcHost(), CustomSut.grpcPort())
                .usePlaintext()
                .build();
        return new InventoryClient(channel);
    }

    public CheckStockResponse checkStock(CheckStockRequest request) {
        return call("CheckStock", request,
                () -> stub.withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS).checkStock(request));
    }

    public ReserveStockResponse reserveStock(ReserveStockRequest request) {
        return call("ReserveStock", request,
                () -> stub.withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS).reserveStock(request));
    }

    /**
     * Records the call in the Allure report as a step ("gRPC ReserveStock -> OK") holding the request and the
     * response (or gRPC status) as inline text attachments, and logs it.
     */
    private static <T> T call(String method, Object request, Supplier<T> invocation) {
        AllureLifecycle lifecycle = Allure.getLifecycle();
        lifecycle.startStep(new StepResult().setName("gRPC " + method));
        log.info("gRPC {}", method);
        log.debug("gRPC {} request: {}", method, request);
        try {
            AllureEvidence.text("Request", request.toString());
            T response = invocation.get();
            log.info("gRPC {} -> OK", method);
            log.debug("gRPC {} response: {}", method, response);
            AllureEvidence.text("Response", response.toString());
            lifecycle.updateStep(step -> step.setName("gRPC " + method + " -> OK"));
            return response;
        } catch (StatusRuntimeException e) {
            log.info("gRPC {} -> {}", method, e.getStatus().getCode());
            AllureEvidence.text("Response", "status: " + e.getStatus());
            // A gRPC error status is a legitimate answer (tests assert on it), not a failure of the step.
            lifecycle.updateStep(step -> step.setName("gRPC " + method + " -> " + e.getStatus().getCode()));
            throw e;
        } finally {
            lifecycle.updateStep(step -> step.setStatus(Status.PASSED));
            lifecycle.stopStep();
        }
    }

    @Override
    public void close() {
        channel.shutdownNow();
    }
}
