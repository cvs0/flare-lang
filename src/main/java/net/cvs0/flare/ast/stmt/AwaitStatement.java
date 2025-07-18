package net.cvs0.flare.ast.stmt;

import net.cvs0.flare.ast.ASTNode;
import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Statement;
import net.cvs0.flare.ast.Expression;

/**
 * Represents an 'await' statement.
 */
public class AwaitStatement implements Statement {
    public final Expression fiberHandle;

    public AwaitStatement(Expression fiberHandle) {
        this.fiberHandle = fiberHandle;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitAwaitStatement(this);
    }
}
