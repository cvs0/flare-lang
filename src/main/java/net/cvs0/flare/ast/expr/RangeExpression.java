package net.cvs0.flare.ast.expr;

import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Expression;

public class RangeExpression implements Expression {
    public final Expression start;
    public final Expression end;

    public RangeExpression(Expression start, Expression end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitRangeExpression(this);
    }
}