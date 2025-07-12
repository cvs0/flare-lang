package net.cvs0.flare.ast.stmt;

import java.util.List;
import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Statement;

public class FiberBlock implements Statement
{
    public final Token name;
    public final List<Statement> statements;

    public FiberBlock(Token name, List<Statement> statements) {
        this.name = name;
        this.statements = statements;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitFiberBlock(this);
    }
}
