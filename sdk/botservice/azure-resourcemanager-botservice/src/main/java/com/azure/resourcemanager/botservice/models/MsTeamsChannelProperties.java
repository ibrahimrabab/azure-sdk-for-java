// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.botservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The parameters to provide for the Microsoft Teams channel. */
@Fluent
public final class MsTeamsChannelProperties {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(MsTeamsChannelProperties.class);

    /*
     * Enable calling for Microsoft Teams channel
     */
    @JsonProperty(value = "enableCalling")
    private Boolean enableCalling;

    /*
     * Webhook for Microsoft Teams channel calls
     */
    @JsonProperty(value = "callingWebHook")
    private String callingWebhook;

    /*
     * Whether this channel is enabled for the bot
     */
    @JsonProperty(value = "isEnabled", required = true)
    private boolean isEnabled;

    /*
     * Webhook for Microsoft Teams channel calls
     */
    @JsonProperty(value = "incomingCallRoute")
    private String incomingCallRoute;

    /*
     * Deployment environment for Microsoft Teams channel calls
     */
    @JsonProperty(value = "deploymentEnvironment")
    private String deploymentEnvironment;

    /*
     * Whether this channel accepted terms
     */
    @JsonProperty(value = "acceptedTerms")
    private Boolean acceptedTerms;

    /**
     * Get the enableCalling property: Enable calling for Microsoft Teams channel.
     *
     * @return the enableCalling value.
     */
    public Boolean enableCalling() {
        return this.enableCalling;
    }

    /**
     * Set the enableCalling property: Enable calling for Microsoft Teams channel.
     *
     * @param enableCalling the enableCalling value to set.
     * @return the MsTeamsChannelProperties object itself.
     */
    public MsTeamsChannelProperties withEnableCalling(Boolean enableCalling) {
        this.enableCalling = enableCalling;
        return this;
    }

    /**
     * Get the callingWebhook property: Webhook for Microsoft Teams channel calls.
     *
     * @return the callingWebhook value.
     */
    public String callingWebhook() {
        return this.callingWebhook;
    }

    /**
     * Set the callingWebhook property: Webhook for Microsoft Teams channel calls.
     *
     * @param callingWebhook the callingWebhook value to set.
     * @return the MsTeamsChannelProperties object itself.
     */
    public MsTeamsChannelProperties withCallingWebhook(String callingWebhook) {
        this.callingWebhook = callingWebhook;
        return this;
    }

    /**
     * Get the isEnabled property: Whether this channel is enabled for the bot.
     *
     * @return the isEnabled value.
     */
    public boolean isEnabled() {
        return this.isEnabled;
    }

    /**
     * Set the isEnabled property: Whether this channel is enabled for the bot.
     *
     * @param isEnabled the isEnabled value to set.
     * @return the MsTeamsChannelProperties object itself.
     */
    public MsTeamsChannelProperties withIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        return this;
    }

    /**
     * Get the incomingCallRoute property: Webhook for Microsoft Teams channel calls.
     *
     * @return the incomingCallRoute value.
     */
    public String incomingCallRoute() {
        return this.incomingCallRoute;
    }

    /**
     * Set the incomingCallRoute property: Webhook for Microsoft Teams channel calls.
     *
     * @param incomingCallRoute the incomingCallRoute value to set.
     * @return the MsTeamsChannelProperties object itself.
     */
    public MsTeamsChannelProperties withIncomingCallRoute(String incomingCallRoute) {
        this.incomingCallRoute = incomingCallRoute;
        return this;
    }

    /**
     * Get the deploymentEnvironment property: Deployment environment for Microsoft Teams channel calls.
     *
     * @return the deploymentEnvironment value.
     */
    public String deploymentEnvironment() {
        return this.deploymentEnvironment;
    }

    /**
     * Set the deploymentEnvironment property: Deployment environment for Microsoft Teams channel calls.
     *
     * @param deploymentEnvironment the deploymentEnvironment value to set.
     * @return the MsTeamsChannelProperties object itself.
     */
    public MsTeamsChannelProperties withDeploymentEnvironment(String deploymentEnvironment) {
        this.deploymentEnvironment = deploymentEnvironment;
        return this;
    }

    /**
     * Get the acceptedTerms property: Whether this channel accepted terms.
     *
     * @return the acceptedTerms value.
     */
    public Boolean acceptedTerms() {
        return this.acceptedTerms;
    }

    /**
     * Set the acceptedTerms property: Whether this channel accepted terms.
     *
     * @param acceptedTerms the acceptedTerms value to set.
     * @return the MsTeamsChannelProperties object itself.
     */
    public MsTeamsChannelProperties withAcceptedTerms(Boolean acceptedTerms) {
        this.acceptedTerms = acceptedTerms;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }
}
