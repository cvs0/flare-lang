package net.cvs0.flare.ast.expr;

import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Expression;

/**
 * Represents dot access: e.g., module.symbol or object.field
 */
public class DotAccess implements Expression
{
    public final Expression left;
    public final String right;

    public DotAccess(Expression left, String right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitDotAccess(this);
    }
}

