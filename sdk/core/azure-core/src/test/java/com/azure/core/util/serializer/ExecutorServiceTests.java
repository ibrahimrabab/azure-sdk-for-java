package com.azure.core.util.serializer;

import com.azure.storage.common.implementation.StorageImplUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.net.URISyntaxException;
import java.security.URIParameter;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        System.setProperty("codebase.azure-core", StorageImplUtils.class.getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .toString());

        // Same goes for Jackson Databind.
        System.setProperty("codebase.jackson-databind", ObjectMapper.class.getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .toString());
    }

    public void revertDefaultConfigurations() throws NoSuchMethodException, NoSuchFieldException {
        JacksonAdapter.setUseAccessHelper(originalUseAccessHelper);
        System.setSecurityManager(originalManager);
        java.security.Policy.setPolicy(originalPolicy);

        // Now that the properties have been used, clear them.
        System.clearProperty("codebase.azure-core");
        System.clearProperty("codebase.jackson-databind");

        // Reset accessibility after each test, otherwise they may pass unexpectedly.
        ExecutorServiceTests.SimplePojo.class.getDeclaredConstructor(String.class)
            .setAccessible(false);
        ExecutorServiceTests.SimplePojo.class.getDeclaredField("aProperty")
            .setAccessible(false);
        ExecutorServiceTests.SimplePojo.class.getDeclaredMethod("getAProperty")
            .setAccessible(false);
    }

    @Test
    public void securityPreventsSerialization() throws Exception {
        captureDefaultConfigurations();

        try {
            StorageImplUtils storageImplUtils = new StorageImplUtils();
            JacksonAdapter adapter = new JacksonAdapter();

            java.security.Policy.setPolicy(java.security.Policy
                .getInstance("JavaPolicy", getUriParameter("basic-permissions.policy")));
            System.setSecurityManager(new SecurityManager());

            assertThrows(InvalidDefinitionException.class, () ->
                adapter.deserialize(A_PROPERTY_JSON, ExecutorServiceTests.SimplePojo.class, SerializerEncoding.JSON));
        } finally {
            revertDefaultConfigurations();
        }
    }

    @Test
    public void securityAndAccessHelperNotMatchingPreventsSerialization() throws Exception {
        captureDefaultConfigurations();

        try {
            StorageImplUtils storageImplUtils = new StorageImplUtils();
            JacksonAdapter adapter = new JacksonAdapter();
            JacksonAdapter.setUseAccessHelper(true);

            java.security.Policy.setPolicy(java.security.Policy
                .getInstance("JavaPolicy", getUriParameter("basic-permissions.policy")));
            System.setSecurityManager(new SecurityManager());

            assertThrows(InvalidDefinitionException.class, () ->
                adapter.deserialize(A_PROPERTY_JSON, ExecutorServiceTests.SimplePojo.class, SerializerEncoding.JSON));
        } finally {
            revertDefaultConfigurations();
        }
    }

    @Test
    public void securityAndAccessHelperWorks() throws Exception {
        captureDefaultConfigurations();

        try {
            StorageImplUtils storageImplUtils = new StorageImplUtils();
            JacksonAdapter adapter = new JacksonAdapter();
            JacksonAdapter.setUseAccessHelper(true);

            java.security.Policy.setPolicy(java.security.Policy
                .getInstance("JavaPolicy", getUriParameter("access-helper-succeeds.policy")));
            System.setSecurityManager(new SecurityManager());

            ExecutorServiceTests.SimplePojo actual = assertDoesNotThrow(() ->
                adapter.deserialize(A_PROPERTY_JSON, ExecutorServiceTests.SimplePojo.class, SerializerEncoding.JSON));

            assertEquals(EXPECTED_SIMPLE_POJO.getAProperty(), actual.getAProperty());
        } finally {
            revertDefaultConfigurations();
        }
    }

    @Test
    public void noSecurityRestrictionsWorks() throws Exception {
        captureDefaultConfigurations();

        try {
            StorageImplUtils storageImplUtils = new StorageImplUtils();
            JacksonAdapter adapter = new JacksonAdapter();
            ExecutorServiceTests.SimplePojo actual = assertDoesNotThrow(() ->
                adapter.deserialize(A_PROPERTY_JSON, ExecutorServiceTests.SimplePojo.class, SerializerEncoding.JSON));

            assertEquals(EXPECTED_SIMPLE_POJO.getAProperty(), actual.getAProperty());
        } finally {
            revertDefaultConfigurations();
        }
    }

    private static URIParameter getUriParameter(String policyFile) throws URISyntaxException {
        return new URIParameter(StorageImplUtils.class
            .getResource("/JacksonAdapterSecurityPolicies/" + policyFile)
            .toURI());
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
