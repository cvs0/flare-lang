package net.cvs0.flare.ast.stmt;

import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Statement;

import java.util.List;

/**
 * Block statement: a sequence of statements in curly braces.
 */
public class Block implements Statement
{
    public final List<Statement> statements;

    public Block(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitBlock(this);
    }
}

