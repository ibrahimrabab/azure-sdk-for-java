// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.analytics.purview.administration;

import com.azure.analytics.purview.administration.implementation.PurviewAccountClientImpl;
import com.azure.core.annotation.Generated;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.serializer.JacksonAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** A builder for creating a new instance of the PurviewAccountClient type. */
@ServiceClientBuilder(
        serviceClients = {
            AccountsClient.class,
            CollectionsClient.class,
            ResourceSetRulesClient.class,
            AccountsAsyncClient.class,
            CollectionsAsyncClient.class,
            ResourceSetRulesAsyncClient.class
        })
public final class PurviewAccountClientBuilder {
    @Generated private static final String SDK_NAME = "name";

    @Generated private static final String SDK_VERSION = "version";

    @Generated static final String[] DEFAULT_SCOPES = new String[] {"https://purview.azure.net/.default"};

    @Generated private final Map<String, String> properties = new HashMap<>();

    /** Create an instance of the PurviewAccountClientBuilder. */
    @Generated
    public PurviewAccountClientBuilder() {
        this.pipelinePolicies = new ArrayList<>();
    }

    /*
     * The account endpoint of your Purview account. Example:
     * https://{accountName}.purview.azure.com/account/
     */
    @Generated private String endpoint;

    /**
     * Sets The account endpoint of your Purview account. Example: https://{accountName}.purview.azure.com/account/.
     *
     * @param endpoint the endpoint value.
     * @return the PurviewAccountClientBuilder.
     */
    @Generated
    public PurviewAccountClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /*
     * Service version
     */
    @Generated private PurviewAccountServiceVersion serviceVersion;

    /**
     * Sets Service version.
     *
     * @param serviceVersion the serviceVersion value.
     * @return the PurviewAccountClientBuilder.
     */
    @Generated
    public PurviewAccountClientBuilder serviceVersion(PurviewAccountServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /*
     * The HTTP pipeline to send requests through
     */
    @Generated private HttpPipeline pipeline;

    /**
     * Sets The HTTP pipeline to send requests through.
     *
     * @param pipeline the pipeline value.
     * @return the PurviewAccountClientBuilder.
     */
    @Generated
    public PurviewAccountClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    /*
     * The HTTP client used to send the request.
     */
    @Generated private HttpClient httpClient;

    /**
     * Sets The HTTP client used to send the request.
     *
     * @param httpClient the httpClient value.
     * @return the PurviewAccountClientBuilder.
     */
    @Generated
    public PurviewAccountClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /*
     * The configuration store that is used during construction of the service
     * client.
     */
    @Generated private Configuration configuration;

    /**
     * Sets The configuration store that is used during construction of the service client.
     *
     * @param configuration the configuration value.
     * @return the PurviewAccountClientBuilder.
     */
    @Generated
    public PurviewAccountClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /*
     * The TokenCredential used for authentication.
     */
    @Generated private TokenCredential tokenCredential;

    /**
     * Sets The TokenCredential used for authentication.
     *
     * @param tokenCredential the tokenCredential value.
     * @return the PurviewAccountClientBuilder.
     */
    @Generated
    public PurviewAccountClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        return this;
    }

    /*
     * The logging configuration for HTTP requests and responses.
     */
    @Generated private HttpLogOptions httpLogOptions;

    /**
     * Sets The logging configuration for HTTP requests and responses.
     *
     * @param httpLogOptions the httpLogOptions value.
     * @return the PurviewAccountClientBuilder.
     */
    @Generated
    public PurviewAccountClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /*
     * The retry policy that will attempt to retry failed requests, if
     * applicable.
     */
    @Generated private RetryPolicy retryPolicy;

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the PurviewAccountClientBuilder.
     */
    @Generated
    public PurviewAccountClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /*
     * The list of Http pipeline policies to add.
     */
    @Generated private final List<HttpPipelinePolicy> pipelinePolicies;

    /*
     * The client options such as application ID and custom headers to set on a
     * request.
     */
    @Generated private ClientOptions clientOptions;

    /**
     * Sets The client options such as application ID and custom headers to set on a request.
     *
     * @param clientOptions the clientOptions value.
     * @return the PurviewAccountClientBuilder.
     */
    @Generated
    public PurviewAccountClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     *
     * @param customPolicy The custom Http pipeline policy to add.
     * @return the PurviewAccountClientBuilder.
     */
    @Generated
    public PurviewAccountClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        pipelinePolicies.add(customPolicy);
        return this;
    }

    /**
     * Builds an instance of PurviewAccountClientImpl with the provided parameters.
     *
     * @return an instance of PurviewAccountClientImpl.
     */
    @Generated
    private PurviewAccountClientImpl buildInnerClient() {
        if (serviceVersion == null) {
            this.serviceVersion = PurviewAccountServiceVersion.getLatest();
        }
        if (pipeline == null) {
            this.pipeline = createHttpPipeline();
        }
        PurviewAccountClientImpl client =
                new PurviewAccountClientImpl(
                        pipeline, JacksonAdapter.createDefaultSerializerAdapter(), endpoint, serviceVersion);
        return client;
    }

    @Generated
    private HttpPipeline createHttpPipeline() {
        Configuration buildConfiguration =
                (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;
        if (httpLogOptions == null) {
            httpLogOptions = new HttpLogOptions();
        }
        if (clientOptions == null) {
            clientOptions = new ClientOptions();
        }
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");
        String applicationId = CoreUtils.getApplicationId(clientOptions, httpLogOptions);
        policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, buildConfiguration));
        HttpHeaders headers = new HttpHeaders();
        clientOptions.getHeaders().forEach(header -> headers.set(header.getName(), header.getValue()));
        if (headers.getSize() > 0) {
            policies.add(new AddHeadersPolicy(headers));
        }
        policies.addAll(
                this.pipelinePolicies.stream()
                        .filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_CALL)
                        .collect(Collectors.toList()));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);
        policies.add(new CookiePolicy());
        if (tokenCredential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, DEFAULT_SCOPES));
        }
        policies.addAll(
                this.pipelinePolicies.stream()
                        .filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_RETRY)
                        .collect(Collectors.toList()));
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));
        HttpPipeline httpPipeline =
                new HttpPipelineBuilder()
                        .policies(policies.toArray(new HttpPipelinePolicy[0]))
                        .httpClient(httpClient)
                        .clientOptions(clientOptions)
                        .build();
        return httpPipeline;
    }

    /**
     * Builds an instance of AccountsAsyncClient async client.
     *
     * @return an instance of AccountsAsyncClient.
     */
    @Generated
    public AccountsAsyncClient buildAccountsAsyncClient() {
        return new AccountsAsyncClient(buildInnerClient().getAccounts());
    }

    /**
     * Builds an instance of CollectionsAsyncClient async client.
     *
     * @return an instance of CollectionsAsyncClient.
     */
    @Generated
    public CollectionsAsyncClient buildCollectionsAsyncClient() {
        return new CollectionsAsyncClient(buildInnerClient().getCollections());
    }

    /**
     * Builds an instance of ResourceSetRulesAsyncClient async client.
     *
     * @return an instance of ResourceSetRulesAsyncClient.
     */
    @Generated
    public ResourceSetRulesAsyncClient buildResourceSetRulesAsyncClient() {
        return new ResourceSetRulesAsyncClient(buildInnerClient().getResourceSetRules());
    }

    /**
     * Builds an instance of AccountsClient sync client.
     *
     * @return an instance of AccountsClient.
     */
    @Generated
    public AccountsClient buildAccountsClient() {
        return new AccountsClient(buildInnerClient().getAccounts());
    }

    /**
     * Builds an instance of CollectionsClient sync client.
     *
     * @return an instance of CollectionsClient.
     */
    @Generated
    public CollectionsClient buildCollectionsClient() {
        return new CollectionsClient(buildInnerClient().getCollections());
    }

    /**
     * Builds an instance of ResourceSetRulesClient sync client.
     *
     * @return an instance of ResourceSetRulesClient.
     */
    @Generated
    public ResourceSetRulesClient buildResourceSetRulesClient() {
        return new ResourceSetRulesClient(buildInnerClient().getResourceSetRules());
    }
}
