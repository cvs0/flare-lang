package net.cvs0.flare;

import net.cvs0.flare.ast.*;
import net.cvs0.flare.ast.decl.FunctionDeclaration;
import net.cvs0.flare.ast.decl.VariableDeclaration;
import net.cvs0.flare.ast.decl.VariantDeclaration;
import net.cvs0.flare.ast.expr.*;
import net.cvs0.flare.ast.stmt.*;
import net.cvs0.flare.context.ExecutionContext;
import net.cvs0.flare.tag.Tag;
import net.cvs0.flare.tag.TagValue;
import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.tokens.TokenType;
import net.cvs0.flare.tokens.Type;
import net.cvs0.flare.tokens.TypedType;
import net.cvs0.flare.utils.InterpreterUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Interpreter traverses and executes the AST.
 * Uses ExecutionContext for variable scopes and Value for runtime values.
 * Extensible for new features.
 */
public class Interpreter implements ASTVisitor<Value> {
    public ExecutionContext globals = new ExecutionContext();
    private final ThreadLocal<ExecutionContext> context = ThreadLocal.withInitial(() -> globals);
    private final ModuleRegistry moduleRegistry = new ModuleRegistry();
    private final FiberManager fiberManager = new FiberManager();

    private final java.util.Deque<String> callStack = new java.util.ArrayDeque<>();

    private Debugger debugger = new Debugger();

    private void registerStandardLibraries() {
        for (Map.Entry<String, Map<String, java.util.function.Function<List<Value>, Value>>> entry :
                StandardLibrary.getLibraries().entrySet()) {
            String libName = entry.getKey();
            Map<String, java.util.function.Function<List<Value>, Value>> functions = entry.getValue();
            
            ExecutionContext libContext = new ExecutionContext();
            
            for (Map.Entry<String, java.util.function.Function<List<Value>, Value>> funcEntry : functions.entrySet()) {
                libContext.define(funcEntry.getKey(), new Value(Type.FUNCTION, funcEntry.getValue()));
            }
            
            moduleRegistry.registerLibrary(libName, libName, libContext);
            
            globals.define(libName, new Value(Type.MODULE, libContext));
        }
    }

    public Interpreter() {
        context.set(globals);
        registerStandardLibraries();
    }
    
    /**
     * Register a custom library with the given name and functions
     * @param libraryName The name of the library to register
     * @param functions Map of function names to their implementations
     */
    public void registerLibrary(String libraryName, Map<String, java.util.function.Function<List<Value>, Value>> functions) {
        ExecutionContext libContext = new ExecutionContext();
        
        for (Map.Entry<String, java.util.function.Function<List<Value>, Value>> funcEntry : functions.entrySet()) {
            libContext.define(funcEntry.getKey(), new Value(Type.FUNCTION, funcEntry.getValue()));
        }
        
        moduleRegistry.registerLibrary(libraryName, libraryName, libContext);
        
        globals.define(libraryName, new Value(Type.MODULE, libContext));
    }

    public void interpret(List<Statement> statements) {
        for (Statement stmt : statements) {
            execute(stmt);
        }
    }

    /**
     * Executes a statement and handles call stack and error reporting.
     */
    private Value execute(Statement stmt) {
        if (stmt == null) return null;
        try {
            pushCallStackFrame(stmt);
            debugger.checkBreakpoint(stmt, callStack);
            if (stmt instanceof ReturnStatement) {
                ReturnStatement returnStmt = (ReturnStatement) stmt;
                return evaluate(returnStmt.value);
            }
            stmt.accept(this);
        } catch (RuntimeException e) {
            InterpreterUtil.printStackTrace(e, callStack);
            throw e;
        } finally {
            popCallStackFrame();
        }
        return null;
    }

    /**
     * Pushes a frame to the call stack for debugging.
     */
    private void pushCallStackFrame(Statement stmt) {
        if (stmt instanceof FunctionDeclaration) {
            FunctionDeclaration func = (FunctionDeclaration) stmt;
            callStack.push("Function: " + func.name.lexeme);
        } else if (stmt instanceof FunctionCall) {
            FunctionCall call = (FunctionCall) stmt;
            callStack.push("Call: " + call.callee.toString());
        }
    }

    /**
     * Pops a frame from the call stack.
     */
    private void popCallStackFrame() {
        if (!callStack.isEmpty()) callStack.pop();
    }

    @Override
    public Value visitVariableDeclaration(VariableDeclaration node) {
        Value initValue = node.initializer != null
                ? evaluate(node.initializer)
                : defaultValue(node.typeToken);

        if (node.typeToken.type == TokenType.IDENTIFIER && node.typeToken.lexeme.contains("<")) {
            initValue = convertToTypedType(node.typeToken, initValue, node.name.lexeme);
        } else {
            Type expectedType = InterpreterUtil.tokenTypeToType(node.typeToken);

            if (initValue != null && initValue.type == Type.NULL && node.typeToken.lexeme.endsWith("?")) {
                // This is valid - null can be assigned to nullable types
            }
            else if (initValue != null && initValue.type != expectedType) {
                throw new RuntimeException(
                        "Type error: cannot assign " + initValue.type +
                                " to variable '" + node.name.lexeme +
                                "' of type " + expectedType
                );
            }
        }

        InterpreterUtil.defineVariable(
                context.get(),
                node.name,
                node.typeToken,
                initValue
        );
        return null;
    }

    @Override
    public Value visitAssignment(Assignment node) {
        Value value = evaluate(node.value);
        Value currentValue = context.get().get(node.name.lexeme);
        
        // For now, we'll allow NULL assignments to any variable
        
        switch (node.operator.type) {
            case ASSIGN:
                context.get().assign(node.name.lexeme, value);
                break;
            case PLUS_ASSIGN:
                context.get().assign(node.name.lexeme, currentValue.plus(value));
                break;
            default:
                throw new RuntimeException("Unsupported assignment operator: " + node.operator.type);
        }
        return null;
    }

    @Override
    public Value visitBlock(Block node) {
        withNewContext(() -> {
            for (Statement stmt : node.statements) {
                execute(stmt);
            }
        });
        return null;
    }

    @Override
    public Value visitIf(If node) {
        Value cond = evaluate(node.condition);
        if (InterpreterUtil.isTruthy(cond)) {
            execute(node.thenBranch);
        } else if (node.elseBranch != null) {
            execute(node.elseBranch);
        }
        return null;
    }

    @Override
    public Value visitFunctionDeclaration(FunctionDeclaration node) {
        String signature = generateFunctionSignature(node);
        

        if (context.get().contains(signature) && context.get() != globals) {
            throw new RuntimeException("Function '" + node.name.lexeme + "' with signature '" + signature + "' is already defined.");
        }
        
        context.get().define(signature, new Value(Type.FUNCTION, node));
        return null;
    }
    
    private String generateFunctionSignature(FunctionDeclaration node) {
        StringBuilder signature = new StringBuilder(node.name.lexeme);
        signature.append("(");
        
        for (int i = 0; i < node.parameters.size(); i++) {
            FunctionDeclaration.Parameter param = node.parameters.get(i);
            if (param.typeToken != null) {
                signature.append(param.typeToken.lexeme);
            } else {
                signature.append("any");
            }
            if (i < node.parameters.size() - 1) {
                signature.append(",");
            }
        }
        
        signature.append(")");
        return signature.toString();
    }
    
    private String generateCallSignature(String functionName, List<String> argTypes) {
        StringBuilder signature = new StringBuilder(functionName);
        signature.append("(");
        
        for (int i = 0; i < argTypes.size(); i++) {
            signature.append(argTypes.get(i));
            if (i < argTypes.size() - 1) {
                signature.append(",");
            }
        }
        
        signature.append(")");
        return signature.toString();
    }
    
    private String getValueTypeName(Value value) {
        switch (value.type) {
            case INT: return "int";
            case FLOAT: return "float";
            case STRING: return "string";
            case BOOL: return "bool";
            case NULL: return "any"; // null can match any nullable type
            default: return "any";
        }
    }
    
    private Value executeUserFunction(FunctionDeclaration function, List<Expression> arguments) {
        List<Value> argValues = new ArrayList<>();
        for (Expression argExpr : arguments) {
            argValues.add(evaluate(argExpr));
        }
        return executeUserFunctionWithValues(function, argValues);
    }
    
    private Value executeUserFunctionWithValues(FunctionDeclaration function, List<Value> argValues) {
        if (function.tags != null) {
            for (Tag tag : function.tags) {
                if ("deprecated".equals(tag.name)) {
                    String msg = (tag.arguments != null && !tag.arguments.isEmpty()) ? String.valueOf(tag.arguments.get(0).data) : "";
                    System.out.println("Warning: function '" + function.name.lexeme + "' is deprecated." + (msg.isEmpty() ? "" : " " + msg));
                }
            }
        }

        if (function.parameters.size() != argValues.size()) {
            throw new RuntimeException("Function '" + function.name.lexeme + "' expects " + function.parameters.size() + " arguments, got " + argValues.size());
        }

        ExecutionContext previous = context.get();
        context.set(new ExecutionContext(previous));

        try {
            for (int i = 0; i < function.parameters.size(); i++) {
                FunctionDeclaration.Parameter param = function.parameters.get(i);
                Value argValue = argValues.get(i);
                context.get().define(param.name.lexeme, argValue);
            }

            for (Statement stmt : function.body) {
                execute(stmt);
            }

        } catch (ReturnException returnEx) {
            return returnEx.value;
        } finally {
            context.set(previous);
        }

        return null;
    }

    private static class ReturnException extends RuntimeException {
        public final Value value;
        public ReturnException(Value value) { this.value = value; }
    }

    @Override
    public Value visitReturnStatement(ReturnStatement node) {
        Value value = node.value != null ? evaluate(node.value) : null;
        throw new ReturnException(value);
    }

    @Override
    public Value visitFunctionCall(FunctionCall node) {
        if (node.callee instanceof VariableReference) {
            VariableReference var = (VariableReference) node.callee;
            String functionName = var.name.lexeme;
            
            List<Value> argValues = new ArrayList<>();
            List<String> argTypes = new ArrayList<>();
            for (Expression argExpr : node.arguments) {
                Value argValue = evaluate(argExpr);
                argValues.add(argValue);
                argTypes.add(getValueTypeName(argValue));
            }
            
            String signature = generateCallSignature(functionName, argTypes);
            
            Value functionValue = null;
            if (context.get().contains(signature)) {
                functionValue = context.get().get(signature);
            } else if (globals.contains(signature)) {
                functionValue = globals.get(signature);
            }
            
            if (functionValue != null && functionValue.type == Type.FUNCTION && functionValue.data instanceof FunctionDeclaration) {
                FunctionDeclaration function = (FunctionDeclaration) functionValue.data;
                return executeUserFunctionWithValues(function, argValues);
            }
            
            throw new RuntimeException("No matching function found for '" + functionName + "' with signature '" + signature + "'");
        }
        
        try {
            Value calleeValue = evaluate(node.callee);

            if (calleeValue.type == Type.FUNCTION && calleeValue.data instanceof java.util.function.Function) {
                java.util.function.Function<List<Value>, Value> func = (java.util.function.Function<List<Value>, Value>) calleeValue.data;
                List<Value> argValues = new java.util.ArrayList<>();
                for (Expression argExpr : node.arguments) {
                    argValues.add(evaluate(argExpr));
                }
                return func.apply(argValues);
            }

            if (calleeValue.type == Type.FUNCTION && calleeValue.data instanceof FunctionDeclaration) {
                FunctionDeclaration function = (FunctionDeclaration) calleeValue.data;
                return executeUserFunction(function, node.arguments);
            }

            throw new RuntimeException("Attempted to call a non-function");
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @Override
    public Value visitBinary(Binary node) {
        Value left = evaluate(node.left);
        Value right = evaluate(node.right);

        if (left == null || right == null) {
            return new Value(Type.NULL, null);
        }

        switch (node.operator.type) {
            case PLUS:
                if (left.type == Type.INT && right.type == Type.INT)
                    return new Value(Type.INT, (int)left.data + (int)right.data);
                if (left.type == Type.FLOAT || right.type == Type.FLOAT)
                    return new Value(Type.FLOAT, toFloat(left) + toFloat(right));
                if (left.type == Type.STRING || right.type == Type.STRING)
                    return new Value(Type.STRING, left.data.toString() + right.data.toString());
                if (left.type == Type.LIST && right.type == Type.LIST) {
                    @SuppressWarnings("unchecked")
                    List<Value> leftList = (List<Value>) left.data;
                    @SuppressWarnings("unchecked")
                    List<Value> rightList = (List<Value>) right.data;
                    List<Value> result = new ArrayList<>(leftList);
                    result.addAll(rightList);
                    return new Value(Type.LIST, result);
                }
                break;
            case MINUS:
                if (left.type == Type.INT && right.type == Type.INT)
                    return new Value(Type.INT, (int)left.data - (int)right.data);
                if (left.type == Type.FLOAT || right.type == Type.FLOAT)
                    return new Value(Type.FLOAT, toFloat(left) - toFloat(right));
                break;
            case STAR:
                if (left.type == Type.INT && right.type == Type.INT)
                    return new Value(Type.INT, (int)left.data * (int)right.data);
                if (left.type == Type.FLOAT || right.type == Type.FLOAT)
                    return new Value(Type.FLOAT, toFloat(left) * toFloat(right));
                break;
            case SLASH:
                if (left.type == Type.INT && right.type == Type.INT)
                    return new Value(Type.INT, (int)left.data / (int)right.data);
                if (left.type == Type.FLOAT || right.type == Type.FLOAT)
                    return new Value(Type.FLOAT, toFloat(left) / toFloat(right));
                break;
            case GREATER:
                return new Value(Type.BOOL, toFloat(left) > toFloat(right));
            case GREATER_EQUAL:
                return new Value(Type.BOOL, toFloat(left) >= toFloat(right));
            case LESS:
                return new Value(Type.BOOL, toFloat(left) < toFloat(right));
            case LESS_EQUAL:
                return new Value(Type.BOOL, toFloat(left) <= toFloat(right));
            case EQUAL_EQUAL:
                return new Value(Type.BOOL, left.data == null ? right.data == null : left.data.equals(right.data));
            case BANG_EQUAL:
                return new Value(Type.BOOL, left.data == null ? right.data != null : !left.data.equals(right.data));
            case TYPEOF:
                String typeName = null;
                if (node.right instanceof Literal) {
                    Object lit = ((Literal) node.right).value;
                    if (lit instanceof String) {
                        typeName = (String) lit;
                    }
                } else if (node.right instanceof VariableReference) {
                    typeName = ((VariableReference) node.right).name.lexeme;
                }
                if (typeName == null) {
                    throw new RuntimeException("typeof right operand must be a type name (string or identifier)");
                }

                boolean result = false;
                switch (typeName) {
                    case "int":
                        result = left.type == Type.INT;
                        break;
                    case "float":
                        result = left.type == Type.FLOAT;
                        break;
                    case "string":
                        result = left.type == Type.STRING;
                        break;
                    case "bool":
                        result = left.type == Type.BOOL;
                        break;
                    case "function":
                        result = left.type == Type.FUNCTION;
                        break;
                    case "module":
                        result = left.type == Type.MODULE;
                        break;
                    case "variant":
                        result = left.type == Type.VARIANT;
                        break;
                    case "tag":
                        result = left.type == Type.TAG;
                        break;
                    case "any":
                        result = true;
                        break;
                    default:
                        if (left.type == Type.VARIANT && left.data instanceof VariantDeclaration && ((VariantDeclaration) left.data).name.lexeme.equals(typeName)) {
                            result = true;
                        } else if (left.type == Type.TAG && left.data instanceof TagValue) {
                            TagValue tag = (TagValue) left.data;
                            if (tag.variantName.equals(typeName)) {
                                result = true;
                            }
                        }
                        break;
                }
                return new Value(Type.BOOL, result);
            case AND_AND:
                return new Value(Type.BOOL, isTruthy(left) && isTruthy(right));
            case OR_OR:
                return new Value(Type.BOOL, isTruthy(left) || isTruthy(right));
            default:
                throw new RuntimeException("Unknown binary operator: " + node.operator.type);
        }
        throw new RuntimeException("Type error in binary expression");
    }

    @Override
    public Value visitLiteral(Literal node) {
        if (node.token.type == TokenType.TRUE)
            return new Value(Type.BOOL, true);
        if (node.token.type == TokenType.FALSE)
            return new Value(Type.BOOL, false);
        if (node.token.type == TokenType.NUMBER) {
            if (node.token.lexeme.contains("."))
                return new Value(Type.FLOAT, ((Number)node.value).doubleValue());
            else
                return new Value(Type.INT, ((Number)node.value).intValue());
        }
        if (node.token.type == TokenType.STRING)
            return new Value(Type.STRING, node.value);
        if (node.token.type == TokenType.NULL)
            return Value.NULL;
        throw new RuntimeException("Unknown literal type");
    }

    private ExecutionContext createModuleContext() {
        ExecutionContext ctx = new ExecutionContext();
        return ctx;
    }

    @Override
    public Value visitImportStatement(ImportStatement node) {
        String moduleName = node.moduleName.lexeme;
        String contextName = node.alias != null ? node.alias.lexeme : moduleName;
        
        if (StandardLibrary.getLibraries().containsKey(moduleName)) {
            if (!moduleRegistry.isLoaded(moduleName)) {
                ExecutionContext libContext = new ExecutionContext();
                
                Map<String, java.util.function.Function<List<Value>, Value>> functions =
                    StandardLibrary.getLibraries().get(moduleName);
                
                for (Map.Entry<String, java.util.function.Function<List<Value>, Value>> funcEntry : functions.entrySet()) {
                    libContext.define(funcEntry.getKey(), new Value(Type.FUNCTION, funcEntry.getValue()));
                }
                
                moduleRegistry.registerLibrary(moduleName, moduleName, libContext);
            }
            
            context.get().define(contextName, new Value(Type.MODULE, moduleRegistry.get(moduleName)));
            return null;
        }
        
        if (moduleRegistry.isLoaded(moduleName)) {
            context.get().define(contextName, new Value(Type.MODULE, moduleRegistry.get(moduleName)));
            return null;
        }
        
        String fileName = moduleName + ".lang";
        try {
            Path path = Paths.get(fileName);
            String source = Files.readString(path);
            Lexer lexer = new Lexer(source);
            Parser parser = new Parser(lexer.tokenize());
            List<Statement> statements = parser.parse();
            ExecutionContext moduleContext = createModuleContext();
            ExecutionContext previous = context.get();
            context.set(moduleContext);
            try {
                for (Statement stmt : statements) {
                    execute(stmt);
                }
            } finally {
                context.set(previous);
            }
            moduleRegistry.register(moduleName, moduleContext);
            context.get().define(contextName, new Value(Type.MODULE, moduleContext));
        } catch (Exception e) {
            throw new RuntimeException("Failed to import module '" + moduleName + "': " + e.getMessage());
        }
        return null;
    }

    @Override
    public Value visitVariableReference(VariableReference node) {
        return context.get().get(node.name.lexeme);
    }

    @Override
    public Value visitDotAccess(DotAccess node) {
        Value left = evaluate(node.left);
        if (left.type == Type.MODULE) {
            ExecutionContext moduleCtx = (ExecutionContext) left.data;
            
            if (node.left instanceof VariableReference) {
                String moduleName = ((VariableReference) node.left).name.lexeme;
                if (moduleRegistry.isLibrary(moduleName)) {
                    String libraryName = moduleRegistry.getLibraryName(moduleName);
                    if (!moduleCtx.contains(node.right)) {
                        throw new RuntimeException("Library '" + libraryName + "' does not contain function: " + node.right);
                    }
                    return moduleCtx.get(node.right);
                }
            }
            
            // Regular module access
            if (!moduleCtx.contains(node.right)) {
                if (globals.contains(node.right)) {
                    return globals.get(node.right);
                }
                throw new RuntimeException("Module does not contain symbol: " + node.right);
            }
            return moduleCtx.get(node.right);
        }
        if (left.type == Type.VARIANT) {
            VariantDeclaration variantDecl = (VariantDeclaration) left.data;
            boolean found = false;
            for (Token t : variantDecl.members) {
                if (t.lexeme.equals(node.right)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException("Variant does not contain tag: " + node.right);
            }
            return new Value(Type.TAG, new TagValue(variantDecl.name.lexeme, node.right));
        }
        
        return handleBuiltInMethod(left, node.right);
    }

    @Override
    public Value visitVariantDeclaration(VariantDeclaration node) {
        globals.define(node.name.lexeme, new Value(Type.VARIANT, node));
        return null;
    }

    @Override
    public Value visitSwitchStatement(SwitchStatement node) {
        Value exprValue = evaluate(node.expression);
        String label = exprValue.data != null ? exprValue.data.toString() : null;
        for (SwitchStatement.SwitchCase c : node.cases) {
            if (c.label.equals(label)) {
                for (Statement stmt : c.body) {
                    execute(stmt);
                }
                break;
            }
        }
        return null;
    }

    @Override
    public Value visitFiberBlock(FiberBlock node) {
        final ExecutionContext capturedContext = globals;
        Runnable fiberTask = () -> {
            ExecutionContext previous = context.get();
            context.set(capturedContext);
            try {
                for (Statement stmt : node.statements) {
                    execute(stmt);
                }
            } finally {
                context.set(previous);
            }
        };
        Thread fiberThread = new Thread(fiberTask, node.name.lexeme);
        fiberManager.register(node.name.lexeme, fiberThread);
        fiberThread.start();
        return null;
    }

    @Override
    public Value visitForStatement(ForStatement node) {
        withNewContext(() -> {
            if (node.initializer != null) {
                execute(node.initializer);
            }
            while (true) {
                if (node.condition != null && !isTruthy(evaluate(node.condition))) {
                    break;
                }
                execute(node.body);
                if (node.increment != null) {
                    evaluate(node.increment);
                }
            }
        });
        return null;
    }

    @Override
    public Value visitWhileStatement(WhileStatement node) {
        while (true) {
            Value condition = evaluate(node.getCondition());

            if (!InterpreterUtil.isTruthy(condition)) {
                break;
            }

            execute(node.getBody());
        }
        return null;
    }

    @Override
    public Value visitNullCoalescing(NullCoalescing node) {
        Value leftValue = evaluate(node.left);
        if (leftValue == Value.NULL) {
            return evaluate(node.right);
        }
        return leftValue;
    }

    @Override
    public Value visitSpawnStatement(SpawnStatement node) {
        // Evaluate the function or block to run in a new fiber
        Runnable fiberTask = () -> {
            try {
                if (node.functionOrBlock instanceof FunctionCall) {
                    evaluate((FunctionCall) node.functionOrBlock);
                } else if (node.functionOrBlock instanceof FunctionDeclaration) {
                    execute((FunctionDeclaration) node.functionOrBlock);
                } else {
                    evaluate(node.functionOrBlock);
                }
            } catch (Exception e) {
                InterpreterUtil.printStackTrace(new RuntimeException(e), callStack);
            }
        };
        Thread fiberThread = new Thread(fiberTask, "fiber-" + System.nanoTime());
        fiberThread.start();
        return new Value(Type.FHANDLE, new FiberFHandle(fiberThread));
    }

    @Override
    public Value visitSpawnExpression(SpawnExpression node) {
        Runnable fiberTask = () -> {
            try {
                if (node.fnOrBlock instanceof FunctionCall) {
                    evaluate((FunctionCall) node.fnOrBlock);
                } else if (node.fnOrBlock instanceof FunctionDeclaration) {
                    execute((FunctionDeclaration) node.fnOrBlock);
                } else {
                    evaluate(node.fnOrBlock);
                }
            } catch (Exception e) {
                InterpreterUtil.printStackTrace(new RuntimeException(e), callStack);
            }
        };
        Thread fiberThread = new Thread(fiberTask, "fiber-" + System.nanoTime());
        fiberThread.start();
        return new Value(Type.FHANDLE, new FiberFHandle(fiberThread));
    }

    @Override
    public Value visitYieldStatement(YieldStatement node) {
        Thread.yield();
        return null;
    }

    @Override
    public Value visitAwaitStatement(AwaitStatement node) {
        Value handleValue = evaluate(node.fiberHandle);
        if (handleValue != null && handleValue.data instanceof FiberFHandle) {
            FiberFHandle handle = (FiberFHandle) handleValue.data;
            try {
                handle.await();
            } catch (InterruptedException e) {
                throw new RuntimeException("Fiber await interrupted");
            }
            return null;
        } else {
            throw new RuntimeException("await expects a FiberFHandle");
        }
    }

    private Value evaluate(Expression expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Value value) {
        return InterpreterUtil.isTruthy(value);
    }
    
    private double toFloat(Value value) {
        if (value.type == Type.INT) return (int) value.data;
        if (value.type == Type.FLOAT) return (double) value.data;
        throw new RuntimeException("Cannot convert " + value.type + " to float");
    }

    private Value defaultValue(Token typeToken) {
        return InterpreterUtil.defaultValue(typeToken);
    }
    
    private Value convertToTypedType(Token typeToken, Value value, String variableName) {
        if (value == null) return null;
        
        TypedType expectedTypedType = InterpreterUtil.parseTypedType(typeToken);

        if (value.type == Type.NULL && typeToken.lexeme.endsWith("?")) {
            return value;
        }
        
        if ((value.type == Type.LIST && expectedTypedType.baseType == Type.BUFFER) ||
            (value.type == Type.BUFFER && expectedTypedType.baseType == Type.LIST) ||
            (value.type == expectedTypedType.baseType)) {
            
            if (expectedTypedType.baseType == Type.LIST || expectedTypedType.baseType == Type.BUFFER) {
                @SuppressWarnings("unchecked")
                List<Value> list = (List<Value>) value.data;
                for (Value element : list) {
                    if (element.type != expectedTypedType.elementType) {
                        throw new RuntimeException(
                            "Type error: list element of type " + element.type + 
                            " does not match expected element type " + expectedTypedType.elementType +
                            " for variable '" + variableName + "'"
                        );
                    }
                }
                
                return new Value(expectedTypedType, value.data);
            }
        }
        
        if (value.type != expectedTypedType.baseType) {
            throw new RuntimeException(
                "Type error: cannot assign " + value.type + 
                " to variable '" + variableName + 
                "' of type " + expectedTypedType
            );
        }
        
        return value;
    }

    private void withNewContext(Runnable block) {
        ExecutionContext previous = context.get();
        context.set(new ExecutionContext(previous));
        try {
            block.run();
        } finally {
            context.set(previous);
        }
    }

    @Override
    public Value visitRegionBlock(RegionBlock node) {
        withNewContext(() -> {
            for (Statement stmt : node.statements) {
                execute(stmt);
            }
        });
        return null;
    }

    @Override
    public Value visitUnary(Unary node) {
        Value right = evaluate(node.right);
        switch (node.operator.type) {
            case BANG:
                return new Value(Type.BOOL, !isTruthy(right));
            default:
                throw new RuntimeException("Unknown unary operator: " + node.operator.type);
        }
    }

    @Override
    public Value visitTernary(Ternary node) {
        Value conditionValue = evaluate(node.condition);

        if (InterpreterUtil.isTruthy(conditionValue)) {
            return evaluate(node.trueExpr);
        } else {
            return evaluate(node.falseExpr);
        }
    }

    @Override
    public Value visitListLiteral(ListLiteral node) {
        List<Value> elements = new ArrayList<>();
        for (Expression element : node.elements) {
            Value evaluated = evaluate(element);
            if (element instanceof RangeExpression && evaluated.type == Type.LIST) {
                @SuppressWarnings("unchecked")
                List<Value> rangeElements = (List<Value>) evaluated.data;
                elements.addAll(rangeElements);
            } else {
                elements.add(evaluated);
            }
        }
        return new Value(Type.LIST, elements);
    }

    @Override
    public Value visitBytesLiteral(BytesLiteral node) {
        byte[] bytes = new byte[node.elements.size()];
        for (int i = 0; i < node.elements.size(); i++) {
            Value element = evaluate(node.elements.get(i));
            if (element.type != Type.INT) {
                throw new RuntimeException("Bytes literal elements must be integers");
            }
            int value = (int) element.data;
            if (value < 0 || value > 255) {
                throw new RuntimeException("Byte value must be between 0 and 255, got: " + value);
            }
            bytes[i] = (byte) value;
        }
        return new Value(Type.BYTES, bytes);
    }

    @Override
    public Value visitRangeExpression(RangeExpression node) {
        Value startValue = evaluate(node.start);
        Value endValue = evaluate(node.end);
        
        if (startValue.type != Type.INT || endValue.type != Type.INT) {
            throw new RuntimeException("Range expressions require integer bounds");
        }
        
        int start = (int) startValue.data;
        int end = (int) endValue.data;
        List<Value> elements = new ArrayList<>();
        
        for (int i = start; i <= end; i++) {
            elements.add(new Value(Type.INT, i));
        }
        
        return new Value(Type.LIST, elements);
    }

    @Override
    public Value visitIndexAccess(IndexAccess node) {
        Value objectValue = evaluate(node.object);
        Value indexValue = evaluate(node.index);
        
        if (objectValue.type == Type.LIST || objectValue.type == Type.BUFFER) {
            if (indexValue.type != Type.INT) {
                throw new RuntimeException("List/Buffer index must be an integer");
            }
            
            @SuppressWarnings("unchecked")
            List<Value> list = (List<Value>) objectValue.data;
            int index = (int) indexValue.data;
            
            if (index < 0 || index >= list.size()) {
                throw new RuntimeException("List/Buffer index out of bounds: " + index);
            }
            
            return list.get(index);
        } else if (objectValue.type == Type.BYTES) {
            if (indexValue.type != Type.INT) {
                throw new RuntimeException("Bytes index must be an integer");
            }
            
            byte[] bytes = (byte[]) objectValue.data;
            int index = (int) indexValue.data;
            
            if (index < 0 || index >= bytes.length) {
                throw new RuntimeException("Bytes index out of bounds: " + index);
            }
            
            return new Value(Type.INT, bytes[index] & 0xFF);
        } else {
            throw new RuntimeException("Index access only supported on lists, buffers, and bytes");
        }
    }

    public FiberManager getFiberManager() {
        return fiberManager;
    }
    
    private Value handleBuiltInMethod(Value object, String methodName) {
        switch (methodName) {
            case "toString":
                return new Value(Type.FUNCTION, (java.util.function.Function<List<Value>, Value>) arguments -> {
                    if (!arguments.isEmpty()) {
                        throw new RuntimeException("toString() takes no arguments");
                    }
                    
                    // Special handling for bytes - convert to string
                    if (object.type == Type.BYTES) {
                        byte[] bytes = (byte[]) object.data;
                        return new Value(Type.STRING, new String(bytes));
                    }
                    
                    return new Value(Type.STRING, object.toString());
                });
            case "length":
                if (object.type == Type.STRING) {
                    return new Value(Type.FUNCTION, (java.util.function.Function<List<Value>, Value>) arguments -> {
                        if (!arguments.isEmpty()) {
                            throw new RuntimeException("length() takes no arguments");
                        }
                        String str = (String) object.data;
                        return new Value(Type.INT, str.length());
                    });
                } else if (object.type == Type.LIST || object.type == Type.BUFFER) {
                    return new Value(Type.FUNCTION, (java.util.function.Function<List<Value>, Value>) arguments -> {
                        if (!arguments.isEmpty()) {
                            throw new RuntimeException("length() takes no arguments");
                        }
                        @SuppressWarnings("unchecked")
                        List<Value> list = (List<Value>) object.data;
                        return new Value(Type.INT, list.size());
                    });
                } else if (object.type == Type.BYTES) {
                    return new Value(Type.FUNCTION, (java.util.function.Function<List<Value>, Value>) arguments -> {
                        if (!arguments.isEmpty()) {
                            throw new RuntimeException("length() takes no arguments");
                        }
                        byte[] bytes = (byte[]) object.data;
                        return new Value(Type.INT, bytes.length);
                    });
                }
                break;
            case "status":
                if (object.data instanceof FiberFHandle) {
                    return new Value(Type.STRING, ((FiberFHandle) object.data).getStatus());
                }
                break;
        }
        throw new RuntimeException("Unknown method '" + methodName + "' on type " + object.type);
    }
}
