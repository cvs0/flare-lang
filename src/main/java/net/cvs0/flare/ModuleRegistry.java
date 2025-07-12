package net.cvs0.flare;

import net.cvs0.flare.context.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

/**
 * ModuleRegistry caches loaded modules and their contexts.
 * Ensures modules are only loaded once and provides namespace access.
 */
public class ModuleRegistry {
    private final Map<String, ExecutionContext> modules = new HashMap<>();
    private final Map<String, String> libraryNames = new HashMap<>();

    public boolean isLoaded(String name) {
        return modules.containsKey(name);
    }

    public void register(String name, ExecutionContext context) {
        modules.put(name, context);
    }
    
    public void registerLibrary(String name, String libraryName, ExecutionContext context) {
        modules.put(name, context);
        libraryNames.put(name, libraryName);
    }
    
    public String getLibraryName(String name) {
        return libraryNames.get(name);
    }
    
    public boolean isLibrary(String name) {
        return libraryNames.containsKey(name);
    }

    public ExecutionContext get(String name) {
        return modules.get(name);
    }
}

