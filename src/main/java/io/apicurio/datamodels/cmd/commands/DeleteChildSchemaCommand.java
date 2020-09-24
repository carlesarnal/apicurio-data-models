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

package io.apicurio.datamodels.cmd.commands;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.asyncapi.models.AaiSchema;
import io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter;
import io.apicurio.datamodels.compat.LoggerCompat;
import io.apicurio.datamodels.compat.MarshallCompat.NullableJsonNodeDeserializer;
import io.apicurio.datamodels.core.models.Document;
import io.apicurio.datamodels.core.models.DocumentType;
import io.apicurio.datamodels.core.models.Node;
import io.apicurio.datamodels.core.models.NodePath;
import io.apicurio.datamodels.core.models.common.Schema;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema.Oas30AnyOfSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema.Oas30OneOfSchema;

/**
 * A command used to delete a single child schema from a parent schema.
 * @author eric.wittmann@gmail.com
 */
public class DeleteChildSchemaCommand extends AbstractSchemaInhCommand {

    public NodePath _schemaPath;
    public NodePath _parentPath;

    @JsonDeserialize(using=NullableJsonNodeDeserializer.class)
    public Object _oldSchema;
    public String _oldSchemaType;
    
    DeleteChildSchemaCommand() {
    }
    
    DeleteChildSchemaCommand(OasSchema schema) {
        this._schemaPath = Library.createNodePath(schema);
        this._parentPath = Library.createNodePath(schema.parent());
    }
    
    /**
     * @see io.apicurio.datamodels.cmd.ICommand#execute(io.apicurio.datamodels.core.models.Document)
     */
    @Override
    public void execute(Document document) {
        LoggerCompat.info("[DeleteChildSchemaCommand] Executing.");
        this._oldSchema = null;

        OasSchema childSchema = (OasSchema) this._schemaPath.resolve(document);
        if (this.isNullOrUndefined(childSchema)) {
            return;
        }

        // Remove the schema from its parent
        SchemaRemoverVisitor schemaRemover = new SchemaRemoverVisitor();
        Library.visitNode(childSchema, schemaRemover);

        this._oldSchema = Library.writeNode(childSchema);
        this._oldSchemaType = schemaRemover.type;
    }
    
    /**
     * @see io.apicurio.datamodels.cmd.ICommand#undo(io.apicurio.datamodels.core.models.Document)
     */
    @Override
    public void undo(Document document) {
        LoggerCompat.info("[DeleteChildSchemaCommand] Reverting.");
        if (this.isNullOrUndefined(this._oldSchema)) {
            return;
        }

        OasSchema parent = (OasSchema) this._parentPath.resolve(document);
        if (this.isNullOrUndefined(parent)) {
            return;
        }

        // Create the schema and unmarshal it from the saved JSON data
        OasSchema schema = this.createSchema(parent, this._oldSchemaType);
        Library.readNode(this._oldSchema, schema);
        
        // Add the schema back to the parent
        SchemaAdderVisitor schemaAdder = new SchemaAdderVisitor();
        Library.visitNode(schema, schemaAdder);
    }

    /**
     * Visitor used to remove a child schema from its parent.
     * @author eric.wittmann@gmail.com
     */
    private static class SchemaRemoverVisitor extends CombinedVisitorAdapter {
        
        public String type;
        
        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitAllOfSchema(io.apicurio.datamodels.openapi.models.OasSchema)
         */
        @Override
        public void visitAllOfSchema(Schema node) {
            Schema parentSchema = (Schema) node.parent();
            if (isOai(parentSchema)) {
                ((OasSchema) parentSchema).removeAllOfSchema((OasSchema) node);
            } else if (isAai(parentSchema)) {
                ((AaiSchema) parentSchema).removeAllOfSchema((AaiSchema) node);
            }
            this.type = TYPE_ALL_OF;
        }
        
        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitAnyOfSchema(io.apicurio.datamodels.openapi.v3.models.Oas30Schema.Oas30AnyOfSchema)
         */
        @Override
        public void visitAnyOfSchema(Schema node) {
            Schema parentSchema = (Schema) node.parent();
            if (isOai(parentSchema)) {
                ((Oas30Schema) parentSchema).removeAnyOfSchema((Oas30AnyOfSchema) node);
            } else if (isAai(parentSchema)) {
                ((AaiSchema) parentSchema).removeAnyOfSchema((AaiSchema) node);
            }
            this.type = TYPE_ANY_OF;
        }
        
        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitOneOfSchema(io.apicurio.datamodels.openapi.v3.models.Oas30Schema.Oas30OneOfSchema)
         */
        @Override
        public void visitOneOfSchema(Schema node) {
            Schema parentSchema = (Schema) node.parent();
            if (isOai(parentSchema)) {
                ((Oas30Schema) parentSchema).removeOneOfSchema((Oas30OneOfSchema) node);
            } else if (isAai(parentSchema)) {
                ((AaiSchema) parentSchema).removeOneOfSchema((AaiSchema) node);
            }
            this.type = TYPE_ONE_OF;
        }
        
    }


    /**
     * Visitor used to add a child schema to its parent.
     * @author eric.wittmann@gmail.com
     */
    private static class SchemaAdderVisitor extends CombinedVisitorAdapter {
        
        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitAllOfSchema(io.apicurio.datamodels.openapi.models.OasSchema)
         */
        @Override
        public void visitAllOfSchema(Schema node) {
            Schema parentSchema = (Schema) node.parent();
            if (isOai(parentSchema)) {
                ((OasSchema) parentSchema).addAllOfSchema((OasSchema) node);
            } else if (isAai(parentSchema)) {
                ((AaiSchema) parentSchema).addAllOfSchema((AaiSchema) node);
            }
        }
        
        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitAnyOfSchema(io.apicurio.datamodels.openapi.v3.models.Oas30Schema.Oas30AnyOfSchema)
         */
        @Override
        public void visitAnyOfSchema(Schema node) {
            Schema parentSchema = (Schema) node.parent();
            if (isOai(parentSchema)) {
                ((Oas30Schema) parentSchema).addAnyOfSchema((Oas30AnyOfSchema) node);
            } else if (isAai(parentSchema)) {
                ((AaiSchema) parentSchema).addAnyOfSchema((AaiSchema) node);
            }
        }
        
        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitOneOfSchema(io.apicurio.datamodels.openapi.v3.models.Oas30Schema.Oas30OneOfSchema)
         */
        @Override
        public void visitOneOfSchema(Schema node) {
            Schema parentSchema = (Schema) node.parent();
            if (isOai(parentSchema)) {
                ((Oas30Schema) parentSchema).addOneOfSchema((Oas30OneOfSchema) node);
            } else if (isAai(parentSchema)) {
                ((AaiSchema) parentSchema).addOneOfSchema((AaiSchema) node);
            }
        }
        
    }

    private static final boolean isOai(Node node) {
        return node.ownerDocument().getDocumentType() != DocumentType.asyncapi2;
    }
    private static final boolean isAai(Node node) {
        return node.ownerDocument().getDocumentType() == DocumentType.asyncapi2;
    }
    
}
