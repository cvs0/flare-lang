package net.cvs0.flare;

import net.cvs0.flare.ast.Assignment;
import net.cvs0.flare.ast.Expression;
import net.cvs0.flare.ast.FunctionCall;
import net.cvs0.flare.ast.Statement;
import net.cvs0.flare.ast.decl.FunctionDeclaration;
import net.cvs0.flare.ast.decl.VariableDeclaration;
import net.cvs0.flare.ast.decl.VariantDeclaration;
import net.cvs0.flare.ast.expr.*;
import net.cvs0.flare.ast.stmt.*;
import net.cvs0.flare.context.ParserContext;
import net.cvs0.flare.tag.Tag;
import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.tokens.TokenType;
import net.cvs0.flare.tokens.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser converts a list of tokens into an AST.
 * Clean, modular, and easily extensible for new language features.
 */
public class Parser
{
    private final ParserContext ctx;

    public Parser(List<Token> tokens)
    {
        this.ctx = new ParserContext(tokens);
    }

    /**
     * Parses the entire input and returns a list of statements (the program AST).
     */
    public List<Statement> parse()
    {
        List<Statement> statements = new ArrayList<>();
        while (!ctx.isAtEnd())
        {
            statements.add(declaration());
        }
        return statements;
    }

    private List<Tag> collectTags()
    {
        List<Tag> tags = new ArrayList<>();
        while (ctx.match(TokenType.TAG))
        {
            Token tagName = consume(TokenType.IDENTIFIER, "Expect tag name after 'tag'.");
            List<Value> args = new ArrayList<>();
            if (ctx.match(TokenType.LEFT_PAREN))
            {
                if (!ctx.check(TokenType.RIGHT_PAREN))
                {
                    do
                    {
                        args.add(parseTagArgument());
                    } while (ctx.match(TokenType.COMMA));
                }
                consume(TokenType.RIGHT_PAREN, "Expect ')' after tag arguments.");
            }
            tags.add(new Tag(tagName.lexeme, args));
        }
        return tags;
    }

    private Value parseTagArgument()
    {
        Token t = ctx.advance();
        switch (t.type)
        {
            case STRING:
                return new Value(Type.STRING, t.literal);
            case NUMBER:
                if (t.lexeme.contains("."))
                    return new Value(Type.FLOAT, ((Number) t.literal).doubleValue());
                else
                    return new Value(Type.INT, ((Number) t.literal).intValue());
            case TRUE:
                return new Value(Type.BOOL, true);
            case FALSE:
                return new Value(Type.BOOL, false);
            default:
                throw error(t, "Invalid tag argument type");
        }
    }

    private Statement declaration() {
        List<Tag> tags = collectTags();

        if (ctx.match(TokenType.IMPORT)) {
            return importStatement();
        }

        if (ctx.match(TokenType.FUNC)) {
            return functionDeclaration(tags);
        }

        if (ctx.check(TokenType.STRING_TYPE, TokenType.INT, TokenType.FLOAT, TokenType.BOOLEAN, TokenType.LIST_TYPE)) {
            Token typeToken = ctx.advance();

            // Handle nullable types (e.g., string?)
            if (ctx.match(TokenType.QUESTION_MARK)) {
                typeToken = new Token(TokenType.IDENTIFIER, typeToken.lexeme + "?", typeToken.literal, typeToken.line, typeToken.column);
            }

            return variableDeclaration(tags, typeToken);
        }

        if (ctx.match(TokenType.VARIANT)) {
            return variantDeclaration();
        }

        return statement();
    }


    private Statement importStatement()
    {
        Token moduleName;
        if (ctx.match(TokenType.IDENTIFIER, TokenType.STRING_TYPE, TokenType.INT, TokenType.FLOAT, TokenType.BOOLEAN, TokenType.LIST_TYPE))
        {
            moduleName = ctx.previous();
        }
        else
        {
            throw error(ctx.peek(), "Expect module name after 'import'.");
        }

        Token alias = null;
        if (ctx.match(TokenType.AS))
        {
            if (ctx.match(TokenType.IDENTIFIER))
            {
                alias = ctx.previous();
            }
            else
            {
                throw error(ctx.peek(), "Expect alias name after 'as'.");
            }
        }

        consume(TokenType.SEMICOLON, "Expect ';' after import statement.");
        return alias != null ? new ImportStatement(moduleName, alias) : new ImportStatement(moduleName);
    }

    private Statement whileStatement()
    {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");
        Expression condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after while condition.");
        Statement body = statement();
        return new WhileStatement(condition, body);
    }

    private Statement functionDeclaration(List<Tag> tags)
    {
        Token name = consume(TokenType.IDENTIFIER, "Expect function name.");
        consume(TokenType.LEFT_PAREN, "Expect '(' after function name.");
        List<FunctionDeclaration.Parameter> parameters = new ArrayList<>();
        if (!ctx.check(TokenType.RIGHT_PAREN))
        {
            do
            {
                Token paramName = consume(TokenType.IDENTIFIER, "Expect parameter name.");
                Token typeToken = null;
                if (ctx.match(TokenType.COLON))
                {
                    Token baseType = ctx.match(TokenType.IDENTIFIER, TokenType.STRING_TYPE, TokenType.INT, TokenType.FLOAT, TokenType.BOOLEAN, TokenType.LIST_TYPE)
                            ? ctx.previous()
                            : null;

                    if (baseType == null) {
                        throw error(ctx.peek(), "Expect parameter type.");
                    }

                    if (ctx.match(TokenType.QUESTION_MARK))
                    {
                        typeToken = new Token(TokenType.IDENTIFIER, baseType.lexeme + "?", baseType.literal, baseType.line, baseType.column);
                    }
                    else
                    {
                        typeToken = baseType;
                    }
                }
                parameters.add(new FunctionDeclaration.Parameter(paramName, typeToken));
            } while (ctx.match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");
        consume(TokenType.LEFT_BRACE, "Expect '{' before function body.");
        List<Statement> body = new ArrayList<>();
        while (!ctx.check(TokenType.RIGHT_BRACE) && !ctx.isAtEnd())
        {
            body.add(declaration());
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after function body.");
        return new FunctionDeclaration(name, parameters, body, tags);
    }

    private Statement variableDeclaration(List<Tag> tags, Token typeToken)
    {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

        Expression initializer = null;
        if (ctx.match(TokenType.ASSIGN))
        {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");

        return new VariableDeclaration(typeToken, name, initializer, tags);
    }

    private Statement statement()
    {
        if (ctx.match(TokenType.IF)) return ifStatement();
        if (ctx.match(TokenType.RETURN)) return returnStatement();
        if (ctx.match(TokenType.FOR)) return forStatement();
        if (ctx.match(TokenType.FIBER)) return fiberBlock();
        if (ctx.match(TokenType.LEFT_BRACE)) return block();
        if (ctx.match(TokenType.REGION)) return regionBlock();
        if (ctx.match(TokenType.SWITCH)) return switchStatement();
        if (ctx.match(TokenType.WHILE)) return whileStatement();
        return assignmentOrExpressionStatement();
    }

    private Statement forStatement()
    {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.");
        Statement initializer;
        if (ctx.match(TokenType.SEMICOLON))
        {
            initializer = null;
        }
        else if (ctx.match(TokenType.INT, TokenType.FLOAT, TokenType.STRING_TYPE, TokenType.BOOLEAN, TokenType.LIST_TYPE, TokenType.VAR))
        {
            Token typeToken = ctx.previous();
            if (ctx.match(TokenType.QUESTION_MARK))
            {
                typeToken = new Token(TokenType.IDENTIFIER, typeToken.lexeme + "?", typeToken.literal, typeToken.line, typeToken.column);
            }
            initializer = variableDeclaration(new ArrayList<>(), typeToken);
        }
        else
        {
            initializer = assignmentOrExpressionStatement();
        }
        Expression condition = null;
        if (!ctx.check(TokenType.SEMICOLON))
        {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");
        Expression increment = null;
        if (!ctx.check(TokenType.RIGHT_PAREN))
        {
            increment = assignmentOrExpressionNoSemicolon();
            while (ctx.match(TokenType.COMMA))
            {
                assignmentOrExpressionNoSemicolon();
            }
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.");
        Statement body = statement();
        return new ForStatement(initializer, condition, increment, body);
    }

    private Statement returnStatement()
    {
        Token keyword = ctx.previous();
        Expression value = null;
        if (!ctx.check(TokenType.SEMICOLON))
        {
            value = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after return value.");
        return new ReturnStatement(keyword, value);
    }

    private Statement block()
    {
        List<Statement> statements = new ArrayList<>();
        while (!ctx.check(TokenType.RIGHT_BRACE) && !ctx.isAtEnd())
        {
            statements.add(declaration());
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return new Block(statements);
    }

    private Statement ifStatement()
    {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
        Expression condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");
        Statement thenBranch = statement();
        Statement elseBranch = null;
        if (ctx.match(TokenType.ELSE))
        {
            elseBranch = statement();
        }
        return new If(condition, thenBranch, elseBranch);
    }

    private Statement regionBlock()
    {
        consume(TokenType.LEFT_BRACE, "Expect '{' after 'region'.");
        List<Statement> statements = new ArrayList<>();
        while (!ctx.check(TokenType.RIGHT_BRACE) && !ctx.isAtEnd())
        {
            statements.add(declaration());
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after region block.");
        return new RegionBlock(statements);
    }

    private Statement fiberBlock()
    {
        Token name = consume(TokenType.IDENTIFIER, "Expect fiber name after 'fiber'.");
        consume(TokenType.LEFT_BRACE, "Expect '{' before fiber body.");
        List<Statement> statements = new ArrayList<>();
        while (!ctx.check(TokenType.RIGHT_BRACE) && !ctx.isAtEnd())
        {
            statements.add(declaration());
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after fiber body.");
        return new FiberBlock(name, statements);
    }

    private Statement variantDeclaration()
    {
        Token name = consume(TokenType.IDENTIFIER, "Expect variant name after 'variant'.");
        consume(TokenType.LEFT_BRACE, "Expect '{' after variant name.");
        List<Token> members = new ArrayList<>();
        do
        {
            members.add(consume(TokenType.IDENTIFIER, "Expect member name in variant."));
        } while (ctx.match(TokenType.COMMA));
        consume(TokenType.RIGHT_BRACE, "Expect '}' after variant members.");
        consume(TokenType.SEMICOLON, "Expect ';' after variant declaration.");
        return new VariantDeclaration(name, members);
    }

    private Statement switchStatement()
    {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'switch'.");
        Expression expr = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after switch expression.");
        consume(TokenType.LEFT_BRACE, "Expect '{' after switch expression.");
        List<SwitchStatement.SwitchCase> cases = new ArrayList<>();
        while (!ctx.check(TokenType.RIGHT_BRACE) && !ctx.isAtEnd())
        {
            cases.add(switchCase());
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after switch cases.");
        return new SwitchStatement(expr, cases);
    }

    private SwitchStatement.SwitchCase switchCase()
    {
        if (!ctx.match(TokenType.CASE))
        {
            throw error(ctx.peek(), "Expect 'case' before case label in switch statement.");
        }
        StringBuilder label = new StringBuilder();
        if (ctx.match(TokenType.IDENTIFIER))
        {
            label.append(ctx.previous().lexeme);
            while (ctx.match(TokenType.DOT))
            {
                label.append(".");
                Token next = consume(TokenType.IDENTIFIER, "Expect identifier after '.' in case label.");
                label.append(next.lexeme);
            }
        }
        else if (ctx.match(TokenType.STRING))
        {
            label.append(ctx.previous().lexeme);
        }
        else
        {
            throw error(ctx.peek(), "Expect case label (identifier, qualified identifier, or string) after 'case'.");
        }
        consume(TokenType.COLON, "Expect ':' after case label.");
        List<Statement> body = new ArrayList<>();
        while (true)
        {
            if (ctx.check(TokenType.RIGHT_BRACE) || ctx.isAtEnd()) break;
            if (ctx.check(TokenType.CASE)) break;
            body.add(declaration());
        }
        return new SwitchStatement.SwitchCase(label.toString(), body);
    }

    private Statement assignmentOrExpressionStatement()
    {
        Expression expr = expression();
        if (ctx.match(TokenType.ASSIGN, TokenType.PLUS_ASSIGN))
        {
            if (!(expr instanceof VariableReference))
            {
                throw error(ctx.previous(), "Invalid assignment target.");
            }
            Token operator = ctx.previous();
            Expression value = expression();
            consume(TokenType.SEMICOLON, "Expect ';' after assignment.");
            return new Assignment(((VariableReference) expr).name, operator, value);
        }
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return expr instanceof Statement ? (Statement) expr : null;
    }

    private Expression expression()
    {
        return logicalOr();
    }

    private Expression logicalOr() {
        Expression expr = logicalAnd();
        while (ctx.match(TokenType.OR_OR)) {
            Token operator = ctx.previous();
            Expression right = logicalAnd();
            expr = new Binary(expr, operator, right);
        }
        return ternary(expr);
    }

    private Expression ternary(Expression condition) {
        if (ctx.match(TokenType.QUESTION_MARK)) {
            Expression trueExpr = expression();
            consume(TokenType.COLON, "Expect ':' after true expression in ternary.");
            Expression falseExpr = expression();
            return new Ternary(condition, trueExpr, falseExpr);
        }
        return nullCoalescing(condition);
    }

    private Expression nullCoalescing(Expression left) {
        if (ctx.match(TokenType.QUESTION_QUESTION)) {
            Expression right = expression();
            return new NullCoalescing(left, right);
        }
        return left;
    }

    private Expression logicalAnd()
    {
        Expression expr = equality();
        while (ctx.match(TokenType.AND_AND))
        {
            Token operator = ctx.previous();
            Expression right = equality();
            expr = new Binary(expr, operator, right);
        }
        return expr;
    }

    private Expression equality()
    {
        Expression expr = comparison();
        while (ctx.match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL, TokenType.TYPEOF))
        {
            Token operator = ctx.previous();
            Expression right = comparison();
            expr = new Binary(expr, operator, right);
        }
        return expr;
    }

    private Expression comparison()
    {
        Expression expr = term();
        while (ctx.match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL))
        {
            Token operator = ctx.previous();
            Expression right = term();
            expr = new Binary(expr, operator, right);
        }
        return expr;
    }

    private Expression term()
    {
        Expression expr = factor();
        while (ctx.match(TokenType.PLUS, TokenType.MINUS))
        {
            Token operator = ctx.previous();
            Expression right = factor();
            expr = new Binary(expr, operator, right);
        }
        return expr;
    }

    private Expression factor()
    {
        Expression expr = unary();
        while (ctx.match(TokenType.STAR, TokenType.SLASH))
        {
            Token operator = ctx.previous();
            Expression right = unary();
            expr = new Binary(expr, operator, right);
        }
        return expr;
    }

    private Expression unary()
    {
        if (ctx.match(TokenType.BANG))
        {
            Token operator = ctx.previous();
            Expression right = unary();
            return new Unary(operator, right);
        }
        return primary();
    }

    private Expression primary()
    {
        if (ctx.match(TokenType.NUMBER, TokenType.STRING, TokenType.TRUE, TokenType.FALSE))
        {
            return new Literal(ctx.previous().literal, ctx.previous());
        }
        if (ctx.match(TokenType.NULL))
        {
            return new Literal(null, ctx.previous());
        }
        if (ctx.match(TokenType.IDENTIFIER))
        {
            Expression expr = new VariableReference(ctx.previous());
            while (true)
            {
                if (ctx.match(TokenType.LEFT_PAREN))
                {
                    expr = finishFunctionCall(expr);
                }
                else if (ctx.match(TokenType.DOT))
                {
                    Token name;
                    if (ctx.match(TokenType.IDENTIFIER, TokenType.STRING_TYPE, TokenType.INT, TokenType.FLOAT, TokenType.BOOLEAN, TokenType.LIST_TYPE))
                    {
                        name = ctx.previous();
                    }
                    else
                    {
                        throw error(ctx.peek(), "Expect property or method name after '.'");
                    }
                    expr = new DotAccess(expr, name.lexeme);
                }
                else if (ctx.match(TokenType.LEFT_BRACKET))
                {
                    Expression index = expression();
                    consume(TokenType.RIGHT_BRACKET, "Expect ']' after index.");
                    expr = new IndexAccess(expr, index);
                }
                else
                {
                    break;
                }
            }
            return expr;
        }
        if (ctx.match(TokenType.LEFT_PAREN))
        {
            Expression expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return expr;
        }
        if (ctx.match(TokenType.LEFT_BRACKET))
        {
            List<Expression> elements = new ArrayList<>();
            if (!ctx.check(TokenType.RIGHT_BRACKET))
            {
                do
                {
                    Expression element = expression();
                    if (ctx.match(TokenType.DOT_DOT))
                    {
                        Expression end = expression();
                        elements.add(new RangeExpression(element, end));
                    }
                    else
                    {
                        elements.add(element);
                    }
                } while (ctx.match(TokenType.COMMA));
            }
            consume(TokenType.RIGHT_BRACKET, "Expect ']' after list elements.");
            return new ListLiteral(elements);
        }
        throw error(ctx.peek(), "Expect expression.");
    }

    private Expression finishFunctionCall(Expression callee)
    {
        List<Expression> arguments = new ArrayList<>();
        if (!ctx.check(TokenType.RIGHT_PAREN))
        {
            do
            {
                arguments.add(expression());
            } while (ctx.match(TokenType.COMMA));
        }
        Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");
        return new FunctionCall(callee, paren, arguments);
    }

    private Expression assignmentOrExpressionNoSemicolon()
    {
        Expression expr = expression();
        if (ctx.match(TokenType.ASSIGN, TokenType.PLUS_ASSIGN))
        {
            if (!(expr instanceof VariableReference))
            {
                throw error(ctx.previous(), "Invalid assignment target.");
            }
            Token operator = ctx.previous();
            Expression value = expression();
            return new Assignment(((VariableReference) expr).name, operator, value);
        }
        return expr;
    }

    private Token consume(TokenType type, String message)
    {
        if (ctx.check(type)) return ctx.advance();
        Token token = ctx.peek();
        StringBuilder errorMsg = new StringBuilder(message);
        errorMsg.append(" (found '").append(token.lexeme).append("' of type ").append(token.type).append(")");
        errorMsg.append(" at line ").append(token.line).append(", col ").append(token.column).append(".");
        errorMsg.append(" Next tokens: ");
        Token t1 = ctx.peekNext();
        if (t1 != null) errorMsg.append("['").append(t1.lexeme).append("' (").append(t1.type).append(")]");
        int idx = ctx.current + 2;
        for (int i = 0; i < 2; i++, idx++)
        {
            if (idx < ctx.tokens.size())
            {
                Token t = ctx.tokens.get(idx);
                if (t != null) errorMsg.append("['").append(t.lexeme).append("' (").append(t.type).append(")]");
            }
        }
        throw error(token, errorMsg.toString());
    }

    private ParseError error(Token token, String message)
    {
        throw new ParseError("[line " + token.line + ", col " + token.column + "] Error at '" + token.lexeme + "': " + message);
    }

    private static class ParseError extends RuntimeException
    {
        public ParseError(String message)
        {
            super(message);
        }
    }
}
