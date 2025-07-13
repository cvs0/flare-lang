package net.cvs0.flare.context;

import net.cvs0.flare.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * ExecutionContext manages variable scopes and values during interpretation.
 * Supports block scoping and variable resolution.
 */
public class ExecutionContext {
    private final Map<String, Value> variables = new HashMap<>();
    private final ExecutionContext parent;

    public ExecutionContext() {
        this.parent = null;
    }

    public ExecutionContext(ExecutionContext parent) {
        this.parent = parent;
    }

    public void define(String name, Value value) {
        variables.put(name, value);
    }

    public void assign(String name, Value value) {
        if (variables.containsKey(name)) {
            variables.put(name, value);
        } else if (parent != null) {
            parent.assign(name, value);
        } else {
            throw new RuntimeException("Undefined variable '" + name + "'.");
        }
    }

    public Value get(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else if (parent != null) {
            return parent.get(name);
        } else {
            throw new RuntimeException("Undefined variable '" + name + "'.");
        }
    }

    public Map<String, Value> getAll() {
        Map<String, Value> all = (parent != null) ? parent.getAll() : new HashMap<>();
        all.putAll(this.variables);
        return all;
    }

    public boolean contains(String name) {
        return variables.containsKey(name);
    }

    public ExecutionContext snapshot() {
        ExecutionContext snapParent = (parent != null) ? parent.snapshot() : null;
        ExecutionContext snap = new ExecutionContext(snapParent);
        for (Map.Entry<String, Value> entry : variables.entrySet()) {
            snap.variables.put(entry.getKey(), entry.getValue());
        }
        return snap;
    }
}
