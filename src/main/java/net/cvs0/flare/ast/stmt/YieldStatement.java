package net.cvs0.flare.ast.stmt;

import net.cvs0.flare.ast.ASTNode;
import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Statement;

/**
 * Represents a 'yield' statement.
 */
public class YieldStatement implements Statement {
    public YieldStatement() {}

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitYieldStatement(this);
    }
}
