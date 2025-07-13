package net.cvs0.flare.ast.expr;

import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Expression;

public class NullCoalescing implements Expression
{
    public final Expression left;
    public final Expression right;

    public NullCoalescing(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitNullCoalescing(this);
    }
}
