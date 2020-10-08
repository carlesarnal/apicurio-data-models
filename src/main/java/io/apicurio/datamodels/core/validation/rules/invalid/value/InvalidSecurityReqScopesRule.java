/*
 * Copyright 2019 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.datamodels.core.validation.rules.invalid.value;

import java.util.List;

import io.apicurio.datamodels.compat.NodeCompat;
import io.apicurio.datamodels.core.models.common.SecurityRequirement;
import io.apicurio.datamodels.core.validation.ValidationRuleMetaData;

/**
 * @author eric.wittmann@gmail.com
 */
public class InvalidSecurityReqScopesRule extends InvalidPropertyValueRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public InvalidSecurityReqScopesRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }

    /**
     * @see io.apicurio.datamodels.combined.visitors.CombinedAllNodeVisitor#visitSecurityRequirement(io.apicurio.datamodels.core.models.common.SecurityRequirement)
     */
    @Override
    public void visitSecurityRequirement(SecurityRequirement node) {
        List<String> snames = node.getSecurityRequirementNames();
        snames.forEach( sname -> {
            List<String> scopes = node.getScopes(sname);
            this.reportIfInvalid(hasValue(scopes) && NodeCompat.isList(scopes), node, sname, map("name", sname));
        });
    }

}