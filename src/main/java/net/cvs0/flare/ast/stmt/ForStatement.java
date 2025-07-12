package net.cvs0.flare.ast.stmt;

import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Expression;
import net.cvs0.flare.ast.Statement;

public class ForStatement implements Statement
{
    public final Statement initializer;
    public final Expression condition;
    public final Expression increment;
    public final Statement body;

    public ForStatement(Statement initializer, Expression condition, Expression increment, Statement body) {
        this.initializer = initializer;
        this.condition = condition;
        this.increment = increment;
        this.body = body;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitForStatement(this);
    }
}

