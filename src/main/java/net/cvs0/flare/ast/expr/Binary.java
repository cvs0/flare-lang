package net.cvs0.flare.ast.expr;

import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Expression;

/**
 * Binary expression: e.g., x + 2, x > 3
 */
public class Binary implements Expression
{
    public final Expression left;
    public final Token operator;
    public final Expression right;

    public Binary(Expression left, Token operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitBinary(this);
    }
}

