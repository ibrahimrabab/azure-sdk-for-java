// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.securityinsights.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Expansion result metadata. */
@Fluent
public final class ExpansionResultsMetadata {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(ExpansionResultsMetadata.class);

    /*
     * Information of the aggregated nodes in the expansion result.
     */
    @JsonProperty(value = "aggregations")
    private List<ExpansionResultAggregation> aggregations;

    /**
     * Get the aggregations property: Information of the aggregated nodes in the expansion result.
     *
     * @return the aggregations value.
     */
    public List<ExpansionResultAggregation> aggregations() {
        return this.aggregations;
    }

    /**
     * Set the aggregations property: Information of the aggregated nodes in the expansion result.
     *
     * @param aggregations the aggregations value to set.
     * @return the ExpansionResultsMetadata object itself.
     */
    public ExpansionResultsMetadata withAggregations(List<ExpansionResultAggregation> aggregations) {
        this.aggregations = aggregations;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (aggregations() != null) {
            aggregations().forEach(e -> e.validate());
        }
    }
}
