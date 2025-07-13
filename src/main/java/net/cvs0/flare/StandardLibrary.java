package net.cvs0.flare;

import net.cvs0.flare.context.ExecutionContext;
import net.cvs0.flare.tokens.Type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import com.google.gson.*;

/**
 * Standard library functions implemented in Java.
 */
public class StandardLibrary {
    private static final Map<String, Map<String, Function<List<Value>, Value>>> libraries = new HashMap<>();

    private static final Random random = new Random();

    static {
        registerStdLib();
        registerMathLib();
        registerStringLib();
        registerFileLib();
        registerNetLib();
        registerTimeLib();
        registerRandLib();
        registerVmLib();
    }
    
    private static void registerStdLib() {
        Map<String, Function<List<Value>, Value>> stdLib = new HashMap<>();
        stdLib.put("print", StandardLibrary::std_print);
        libraries.put("std", stdLib);
    }
    
    private static void registerMathLib() {
        Map<String, Function<List<Value>, Value>> mathLib = new HashMap<>();
        mathLib.put("sqrt", StandardLibrary::std_sqrt);
        mathLib.put("abs", StandardLibrary::std_abs);
        mathLib.put("max", StandardLibrary::std_max);
        mathLib.put("min", StandardLibrary::std_min);
        mathLib.put("round", StandardLibrary::std_round);
        libraries.put("math", mathLib);
    }
    
    private static void registerStringLib() {
        Map<String, Function<List<Value>, Value>> strLib = new HashMap<>();
        strLib.put("length", StandardLibrary::std_strlen);
        strLib.put("upper", StandardLibrary::std_upper);
        strLib.put("lower", StandardLibrary::std_lower);
        strLib.put("contains", StandardLibrary::std_contains);
        strLib.put("substring", StandardLibrary::std_substring);
        libraries.put("str", strLib);
    }

    private static void registerFileLib() {
        Map<String, Function<List<Value>, Value>> fileLib = new HashMap<>();
        fileLib.put("read", args -> {
            if (args.size() != 1 || args.get(0).type != Type.STRING)
                throw new RuntimeException("file.read expects 1 string (path)");
            try {
                String content = Files.readString(Path.of((String) args.get(0).data));
                return new Value(Type.STRING, content);
            } catch (IOException e) {
                throw new RuntimeException("file.read failed: " + e.getMessage());
            }
        });
        libraries.put("file", fileLib);
    }

    private static void registerNetLib() {
        Map<String, Function<List<Value>, Value>> netLib = new HashMap<>();
        netLib.put("fetch", args -> {
            if (args.size() != 1 || args.get(0).type != Type.STRING)
                throw new RuntimeException("net.fetch expects 1 string (URL)");
            try (java.util.Scanner s = new java.util.Scanner(new java.net.URL((String) args.get(0).data).openStream()).useDelimiter("\\A")) {
                return new Value(Type.STRING, s.hasNext() ? s.next() : "");
            } catch (IOException e) {
                throw new RuntimeException("net.fetch failed: " + e.getMessage());
            }
        });
        libraries.put("net", netLib);
    }

    private static void registerVmLib() {
        Map<String, Function<List<Value>, Value>> vmLib = new HashMap<>();

        vmLib.put("freeMemory", args -> new Value(Type.INT, Runtime.getRuntime().freeMemory()));
        vmLib.put("totalMemory", args -> new Value(Type.INT, Runtime.getRuntime().totalMemory()));
        vmLib.put("maxMemory", args -> new Value(Type.INT, Runtime.getRuntime().maxMemory()));
        vmLib.put("usedMemory", args -> new Value(Type.INT, Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

        libraries.put("vm", vmLib);
    }

    private static void registerTimeLib() {
        Map<String, Function<List<Value>, Value>> timeLib = new HashMap<>();
        timeLib.put("now", args -> new Value(Type.INT, Instant.now().toEpochMilli()));
        libraries.put("time", timeLib);
    }

    private static void registerRandLib() {
        Map<String, Function<List<Value>, Value>> randLib = new HashMap<>();
        randLib.put("seed", args -> {
            if (args.size() != 1 || args.get(0).type != Type.INT)
                throw new RuntimeException("rand.seed expects 1 int");
            random.setSeed((int) args.get(0).data);
            return null;
        });
        randLib.put("int", args -> {
            if (args.size() != 2 || args.get(0).type != Type.INT || args.get(1).type != Type.INT)
                throw new RuntimeException("rand.int expects (min, max)");
            int min = (int) args.get(0).data;
            int max = (int) args.get(1).data;
            return new Value(Type.INT, random.nextInt(max - min + 1) + min);
        });
        randLib.put("float", args -> new Value(Type.FLOAT, random.nextDouble()));
        libraries.put("rand", randLib);
    }
    
    public static Map<String, Map<String, Function<List<Value>, Value>>> getLibraries() {
        return libraries;
    }
    
    /**
     * Register a custom library with functions
     * @param libName The name of the library
     * @param functions Map of function names to implementations
     */
    public static void registerLibrary(String libName, Map<String, Function<List<Value>, Value>> functions) {
        libraries.put(libName, functions);
    }
    
    public static void registerLibraryInContext(String libName, ExecutionContext context) {
        Map<String, Function<List<Value>, Value>> lib = libraries.get(libName);
        if (lib == null) {
            throw new RuntimeException("Library not found: " + libName);
        }
        
        for (Map.Entry<String, Function<List<Value>, Value>> entry : lib.entrySet()) {
            context.define(entry.getKey(), new Value(Type.FUNCTION, entry.getValue()));
        }
    }
    
    public static Value std_print(List<Value> args) {
        for (Value v : args) {
            System.out.print(v.data);
        }
        System.out.println();
        return null;
    }

    public static Value std_strlen(List<Value> args) {
        if (args.size() != 1 || args.get(0).type != Type.STRING) {
            throw new RuntimeException("string.length expects 1 string argument");
        }
        return new Value(Type.INT, ((String) args.get(0).data).length());
    }

    public static Value std_upper(List<Value> args) {
        if (args.size() != 1 || args.get(0).type != Type.STRING) {
            throw new RuntimeException("string.upper expects 1 string argument");
        }
        return new Value(Type.STRING, ((String) args.get(0).data).toUpperCase());
    }

    public static Value std_lower(List<Value> args) {
        if (args.size() != 1 || args.get(0).type != Type.STRING) {
            throw new RuntimeException("string.lower expects 1 string argument");
        }
        return new Value(Type.STRING, ((String) args.get(0).data).toLowerCase());
    }

    public static Value std_contains(List<Value> args) {
        if (args.size() != 2 || args.get(0).type != Type.STRING || args.get(1).type != Type.STRING) {
            throw new RuntimeException("string.contains expects 2 string arguments");
        }
        return new Value(Type.BOOL, ((String) args.get(0).data).contains((String) args.get(1).data));
    }

    public static Value std_substring(List<Value> args) {
        if (args.size() < 2 || args.size() > 3 || args.get(0).type != Type.STRING || args.get(1).type != Type.INT) {
            throw new RuntimeException("string.substring expects (string, start) or (string, start, end)");
        }
        String s = (String) args.get(0).data;
        int start = (int) args.get(1).data;
        int end = args.size() == 3 ? (int) args.get(2).data : s.length();
        return new Value(Type.STRING, s.substring(start, end));
    }

    public static Value std_sqrt(List<Value> args) {
        if (args.size() != 1 || (args.get(0).type != Type.INT && args.get(0).type != Type.FLOAT)) {
            throw new RuntimeException("math.sqrt expects 1 numeric argument");
        }
        double val = args.get(0).type == Type.INT ? (int) args.get(0).data : (double) args.get(0).data;
        return new Value(Type.FLOAT, Math.sqrt(val));
    }

    public static Value std_abs(List<Value> args) {
        if (args.size() != 1 || (args.get(0).type != Type.INT && args.get(0).type != Type.FLOAT)) {
            throw new RuntimeException("math.abs expects 1 numeric argument");
        }
        if (args.get(0).type == Type.INT)
            return new Value(Type.INT, Math.abs((int) args.get(0).data));
        return new Value(Type.FLOAT, Math.abs((double) args.get(0).data));
    }

    public static Value std_max(List<Value> args) {
        if (args.size() != 2 || args.get(0).type != args.get(1).type) {
            throw new RuntimeException("math.max expects 2 arguments of the same numeric type");
        }
        return args.get(0).type == Type.INT
                ? new Value(Type.INT, Math.max((int) args.get(0).data, (int) args.get(1).data))
                : new Value(Type.FLOAT, Math.max((double) args.get(0).data, (double) args.get(1).data));
    }

    public static Value std_min(List<Value> args) {
        if (args.size() != 2 || args.get(0).type != args.get(1).type) {
            throw new RuntimeException("math.min expects 2 arguments of the same numeric type");
        }
        return args.get(0).type == Type.INT
                ? new Value(Type.INT, Math.min((int) args.get(0).data, (int) args.get(1).data))
                : new Value(Type.FLOAT, Math.min((double) args.get(0).data, (double) args.get(1).data));
    }

    public static Value std_round(List<Value> args) {
        if (args.size() != 1 || args.get(0).type != Type.FLOAT) {
            throw new RuntimeException("math.round expects 1 float argument");
        }
        return new Value(Type.INT, (int) Math.round((double) args.get(0).data));
    }
}

