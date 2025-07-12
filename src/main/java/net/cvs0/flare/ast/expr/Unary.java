package net.cvs0.flare.ast.expr;

import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Expression;

/**
 * Unary expression: e.g., !x
 */
public class Unary implements Expression
{
    public final Token operator;
    public final Expression right;

    public Unary(Token operator, Expression right) {
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitUnary(this);
    }
}

