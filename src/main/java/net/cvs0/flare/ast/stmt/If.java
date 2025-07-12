package net.cvs0.flare.ast.stmt;

import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Expression;
import net.cvs0.flare.ast.Statement;

/**
 * If statement: if (cond) { ... } else { ... }
 */
public class If implements Statement
{
    public final Expression condition;
    public final Statement thenBranch;
    public final Statement elseBranch;

    public If(Expression condition, Statement thenBranch, Statement elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitIf(this);
    }
}

