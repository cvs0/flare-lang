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
import net.cvs0.flare.utils.InterpreterUtil;

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

    private Value execute(Statement stmt) {
        if (stmt == null) return null;
        try {
            if (stmt instanceof FunctionDeclaration) {
                FunctionDeclaration func = (FunctionDeclaration) stmt;
                callStack.push("Function: " + func.name.lexeme);
            } else if (stmt instanceof FunctionCall) {
                FunctionCall call = (FunctionCall) stmt;
                callStack.push("Call: " + call.callee.toString());
            }

            debugger.checkBreakpoint(stmt, callStack);

            if (stmt instanceof ReturnStatement) {
                ReturnStatement returnStmt = (ReturnStatement) stmt;
                return evaluate(returnStmt.value);
            }

            stmt.accept(this);
        } catch (RuntimeException e) {
            printStackTrace(e);
            throw e;
        } finally {
            if (!callStack.isEmpty()) callStack.pop();
        }
        return null;
    }

    private void printStackTrace(RuntimeException e) {
        System.err.println("Error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        System.err.println("Stack trace:");
        if (callStack.isEmpty()) {
            System.err.println("  (empty)");
        } else {
            for (String frame : callStack) {
                System.err.println("  at " + frame);
            }
        }
    }

    @Override
    public Value visitVariableDeclaration(VariableDeclaration node) {
        Value initValue = node.initializer != null
                ? evaluate(node.initializer)
                : defaultValue(node.typeToken);

        Type expectedType = InterpreterUtil.tokenTypeToType(node.typeToken);

        if (initValue != null && initValue.type != expectedType) {
            throw new RuntimeException(
                    "Type error: cannot assign " + initValue.type +
                            " to variable '" + node.name.lexeme +
                            "' of type " + expectedType
            );
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
        switch (node.operator.type) {
            case ASSIGN:
                context.get().assign(node.name.lexeme, evaluate(node.value));
                break;
            case PLUS_ASSIGN:
                context.get().assign(node.name.lexeme, context.get().get(node.name.lexeme).plus(evaluate(node.value)));
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
        context.get().define(node.name.lexeme, new Value(Type.FUNCTION, node));
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
        Value calleeValue = evaluate(node.callee);

        if (calleeValue.type == Type.FUNCTION && calleeValue.data instanceof java.util.function.Function) {
            java.util.function.Function<List<Value>, Value> func = (java.util.function.Function<List<Value>, Value>) calleeValue.data;
            List<Value> argValues = new java.util.ArrayList<>();
            for (Expression argExpr : node.arguments) {
                argValues.add(evaluate(argExpr));
            }
            return func.apply(argValues);
        }

        if (calleeValue.type != Type.FUNCTION) {
            throw new RuntimeException("Attempted to call a non-function");
        }

        FunctionDeclaration function = (FunctionDeclaration) calleeValue.data;

        if (function.tags != null) {
            for (Tag tag : function.tags) {
                if ("deprecated".equals(tag.name)) {
                    String msg = (tag.arguments != null && !tag.arguments.isEmpty()) ? String.valueOf(tag.arguments.get(0).data) : "";
                    System.out.println("Warning: function '" + function.name.lexeme + "' is deprecated." + (msg.isEmpty() ? "" : " " + msg));
                }
            }
        }

        if (function.parameters.size() != node.arguments.size()) {
            throw new RuntimeException("Function '" + function.name.lexeme + "' expects " + function.parameters.size() + " arguments, got " + node.arguments.size());
        }

        ExecutionContext previous = context.get();
        context.set(new ExecutionContext(previous));

        try {
            for (int i = 0; i < function.parameters.size(); i++) {
                FunctionDeclaration.Parameter param = function.parameters.get(i);
                Value argValue = evaluate(node.arguments.get(i));
                context.get().define(param.name.lexeme, argValue);
            }

            for (Statement stmt : function.body) {
                Value result = execute(stmt);
                if (result != null) {
                    return result;
                }
            }

        } finally {
            context.set(previous);
        }

        return null;
    }

    @Override
    public Value visitBinary(Binary node) {
        Value left = evaluate(node.left);
        Value right = evaluate(node.right);
        switch (node.operator.type) {
            case PLUS:
                if (left.type == Type.INT && right.type == Type.INT)
                    return new Value(Type.INT, (int)left.data + (int)right.data);
                if (left.type == Type.FLOAT || right.type == Type.FLOAT)
                    return new Value(Type.FLOAT, toFloat(left) + toFloat(right));
                if (left.type == Type.STRING || right.type == Type.STRING)
                    return new Value(Type.STRING, left.data.toString() + right.data.toString());
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
                return new Value(Type.BOOL, left.data.equals(right.data));
            case BANG_EQUAL:
                return new Value(Type.BOOL, !left.data.equals(right.data));
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
        throw new RuntimeException("Dot access only supported on modules and variants");
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

    public FiberManager getFiberManager() {
        return fiberManager;
    }
}
