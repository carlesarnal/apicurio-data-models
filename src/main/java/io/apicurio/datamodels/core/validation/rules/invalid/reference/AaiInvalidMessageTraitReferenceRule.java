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

package io.apicurio.datamodels.core.validation.rules.invalid.reference;

import io.apicurio.datamodels.asyncapi.models.AaiMessageTrait;
import io.apicurio.datamodels.asyncapi.models.AaiMessageTraitDefinition;
import io.apicurio.datamodels.core.Constants;
import io.apicurio.datamodels.core.util.ReferenceUtil;
import io.apicurio.datamodels.core.validation.ValidationRule;
import io.apicurio.datamodels.core.validation.ValidationRuleMetaData;

/**
 * Implements the Invalid Message Trait Reference rule.
 * @author eric.wittmann@gmail.com
 */
public class AaiInvalidMessageTraitReferenceRule extends ValidationRule {

    /**
     * Constructor.
     * @param ruleInfo
     */
    public AaiInvalidMessageTraitReferenceRule(ValidationRuleMetaData ruleInfo) {
        super(ruleInfo);
    }
    
    /**
     * @see io.apicurio.datamodels.combined.visitors.CombinedAllNodeVisitor#visitMessageTrait(io.apicurio.datamodels.asyncapi.models.AaiMessageTrait)
     */
    @Override
    public void visitMessageTrait(AaiMessageTrait node) {
        if (hasValue(node.$ref)) {
            this.reportIfInvalid(ReferenceUtil.canResolveRef(node.$ref, node), node, Constants.PROP_$REF, map());
        }
    }
    
    /**
     * @see io.apicurio.datamodels.combined.visitors.CombinedAllNodeVisitor#visitMessageTraitDefinition(io.apicurio.datamodels.asyncapi.models.AaiMessageTraitDefinition)
     */
    @Override
    public void visitMessageTraitDefinition(AaiMessageTraitDefinition node) {
        visitMessageTrait(node);
    }

}
