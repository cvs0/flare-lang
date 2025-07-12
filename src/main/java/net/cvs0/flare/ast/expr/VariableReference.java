package net.cvs0.flare.ast.expr;

import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Expression;

/**
 * Variable reference expression: e.g., x
 */
public class VariableReference implements Expression
{
    public final Token name;

    public VariableReference(Token name) {
        this.name = name;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitVariableReference(this);
    }
}

