package net.cvs0.flare.ast.stmt;

import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Statement;

import java.util.List;

/**
 * Represents a region block: region { ... }
 * Executes statements in a fully isolated context.
 */
public class RegionBlock implements Statement
{
    public final List<Statement> statements;

    public RegionBlock(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitRegionBlock(this);
    }
}

