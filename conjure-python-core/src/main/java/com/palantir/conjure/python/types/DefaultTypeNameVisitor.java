/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.python.types;

import com.palantir.conjure.spec.ExternalReference;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeName;
import com.palantir.conjure.visitor.TypeVisitor;
import java.util.Set;


public final class DefaultTypeNameVisitor implements Type.Visitor<String> {

    private Set<TypeName> types;

    public DefaultTypeNameVisitor(Set<TypeName> types) {
        this.types = types;
    }

    @Override
    public String visitList(ListType type) {
        return "ListType(" + type.getItemType().accept(this) + ")";
    }

    @Override
    public String visitMap(MapType type) {
        return "DictType(" + type.getKeyType().accept(this) + ", " + type.getValueType().accept(this) + ")";
    }

    @Override
    public String visitOptional(OptionalType type) {
        return "OptionalType(" + type.getItemType().accept(this) + ")";
    }

    @Override
    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    public String visitPrimitive(PrimitiveType type) {
        switch (type.get()) {
            case STRING:
            case RID:
            case BEARERTOKEN:
            case DATETIME:
            case UUID:
                return "str";
            case BINARY:
                return "BinaryType()";
            case BOOLEAN:
                return "bool";
            case DOUBLE:
                return "float";
            case INTEGER:
            case SAFELONG:
                return "int";
            case ANY:
                return "object";
            default:
                throw new IllegalArgumentException("unknown type: " + type);
        }
    }

    /**
     * We only import the module, not the eponymous type definition inside it, in order to prevent recursive
     * definitions from forming a circular dependency because python cannot resolve that.
     * Therefore, we have to grab the real type out of the module here, when we access it.
     *
     * @see com.palantir.conjure.python.util.ImportsVisitor#visitReference(TypeName)
     */
    @Override
    public String visitReference(TypeName type) {
        if (types.contains(type)) {
            return String.format("%s.%s", type.getName(), type.getName());
        } else {
            throw new IllegalStateException("unknown type: " + type);
        }
    }

    @Override
    public String visitExternal(ExternalReference externalType) {
        if (externalType.getFallback().accept(TypeVisitor.IS_PRIMITIVE)) {
            return visitPrimitive(externalType.getFallback().accept(TypeVisitor.PRIMITIVE));
        } else {
            throw new IllegalStateException("unknown type: " + externalType);
        }
    }

    @Override
    public String visitSet(SetType type) {
        // TODO (bduffield): real sets
        return Type.list(ListType.of(type.getItemType())).accept(this);
    }

    @Override
    public String visitUnknown(String unknownType) {
        throw new IllegalStateException("unknown type: " + unknownType);
    }
}