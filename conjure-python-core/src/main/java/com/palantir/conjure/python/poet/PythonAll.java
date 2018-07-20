/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.python.poet;

import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
public interface PythonAll extends PythonClass {
    List<String> contents();

    @Override
    @Value.Default
    default String className() {
        return "";
    }

    @Override
    default void emit(PythonPoetWriter poetWriter) {
        poetWriter.maintainingIndent(() -> {
            poetWriter.writeIndentedLine("__all__ = [");
            poetWriter.increaseIndent();
            contents().stream().forEach(a -> poetWriter.writeIndentedLine("'%s',", a));
            poetWriter.decreaseIndent();
            poetWriter.writeIndentedLine("]");
            poetWriter.writeLine();
        });
    }

    class Builder extends ImmutablePythonAll.Builder {}

    static Builder builder() {
        return new Builder();
    }
}