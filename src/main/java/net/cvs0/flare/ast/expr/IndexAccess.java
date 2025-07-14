package net.cvs0.flare.ast.expr;

import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Expression;

public class IndexAccess implements Expression {
    public final Expression object;
    public final Expression index;

    public IndexAccess(Expression object, Expression index) {
        this.object = object;
        this.index = index;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitIndexAccess(this);
    }
}