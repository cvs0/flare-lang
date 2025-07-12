package net.cvs0.flare.ast;

import net.cvs0.flare.tokens.Token;
import java.util.List;

/**
 * Represents a function call: name(args) or module.name(args)
 */
public class FunctionCall implements Expression, Statement {
    public final Expression callee;
    public final List<Expression> arguments;
    public final Token paren;

    public FunctionCall(Expression callee, Token paren, List<Expression> arguments) {
        this.callee = callee;
        this.paren = paren;
        this.arguments = arguments;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitFunctionCall(this);
    }
}

