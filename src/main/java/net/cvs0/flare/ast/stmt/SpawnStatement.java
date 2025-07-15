package net.cvs0.flare.ast.stmt;

import net.cvs0.flare.ast.ASTNode;
import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Statement;
import net.cvs0.flare.ast.Expression;

/**
 * Represents a 'spawn' statement or expression.
 */
public class SpawnStatement implements Statement {
    public final Expression functionOrBlock;

    public SpawnStatement(Expression functionOrBlock) {
        this.functionOrBlock = functionOrBlock;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitSpawnStatement(this);
    }
}
