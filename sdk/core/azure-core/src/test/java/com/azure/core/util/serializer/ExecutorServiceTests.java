package com.azure.core.util.serializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.concurrent.ExecutorService;

/**
 * Tests {@link java.util.concurrent.ExecutorService} functionality when there is a SecurityManager.
 */
@SuppressWarnings("removal")
@Execution(ExecutionMode.SAME_THREAD)
@Isolated("Mutates the global SecurityManager")
public class ExecutorServiceTests {
    private static final String A_PROPERTY_JSON = "{\"aProperty\":\"aValue\"}";
    private static final ExecutorServiceTests.SimplePojo EXPECTED_SIMPLE_POJO = new ExecutorServiceTests.SimplePojo("aValue");

    private boolean originalUseAccessHelper;
    private SecurityManager originalManager;
    private java.security.Policy originalPolicy;

    public void captureDefaultConfigurations() {
        originalUseAccessHelper = JacksonAdapter.isUseAccessHelper();
        originalManager = System.getSecurityManager();
        originalPolicy = java.security.Policy.getPolicy();

        // Set the System property codebase.azure-core to the location of JacksonAdapter's codebase.
        // This gets picked up by the policy setting to prevent needing to hardcode the code base location.
        System.setProperty("codebase.azure-core", JacksonAdapter.class.getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .toString());

        // Same goes for Jackson Databind.
        System.setProperty("codebase.jackson-databind", ObjectMapper.class.getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .toString());
    }

    private static final class SimplePojo {
        @JsonProperty("aProperty")
        private final String aProperty;

        @JsonCreator
        private SimplePojo(@JsonProperty("aProperty") String aProperty) {
            this.aProperty = aProperty;
        }

        public String getAProperty() {
            return aProperty;
        }
    }

}
