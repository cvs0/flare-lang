package net.cvs0.flare.ast.expr;

import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Expression;

public class Ternary implements Expression
{
    public final Expression condition;
    public final Expression trueExpr;
    public final Expression falseExpr;

    public Ternary(Expression condition, Expression trueExpr, Expression falseExpr) {
        this.condition = condition;
        this.trueExpr = trueExpr;
        this.falseExpr = falseExpr;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor)
    {
        return visitor.visitTernary(this);
    }
}
