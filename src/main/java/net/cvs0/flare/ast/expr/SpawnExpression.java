package net.cvs0.flare.ast.expr;

import net.cvs0.flare.ast.Expression;
import net.cvs0.flare.ast.ASTVisitor;

/**
 * Represents a 'spawn' expression in the AST.
 */
public class SpawnExpression implements Expression {
    public final Expression fnOrBlock;

    public SpawnExpression(Expression fnOrBlock) {
        this.fnOrBlock = fnOrBlock;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        // You may need to add a visitSpawnExpression method to ASTVisitor and Interpreter
        return visitor.visitSpawnExpression(this);
    }
}

