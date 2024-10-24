// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.securityinsights.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for AutomationRuleActionType. */
public final class AutomationRuleActionType extends ExpandableStringEnum<AutomationRuleActionType> {
    /** Static value ModifyProperties for AutomationRuleActionType. */
    public static final AutomationRuleActionType MODIFY_PROPERTIES = fromString("ModifyProperties");

    /** Static value RunPlaybook for AutomationRuleActionType. */
    public static final AutomationRuleActionType RUN_PLAYBOOK = fromString("RunPlaybook");

    /**
     * Creates or finds a AutomationRuleActionType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding AutomationRuleActionType.
     */
    @JsonCreator
    public static AutomationRuleActionType fromString(String name) {
        return fromString(name, AutomationRuleActionType.class);
    }

    /** @return known AutomationRuleActionType values. */
    public static Collection<AutomationRuleActionType> values() {
        return values(AutomationRuleActionType.class);
    }
}
