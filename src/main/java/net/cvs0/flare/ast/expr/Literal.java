package net.cvs0.flare.ast.expr;

import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Expression;

/**
 * Literal value: numbers, strings, booleans.
 */
public class Literal implements Expression
{
    public final Object value;
    public final Token token;

    public Literal(Object value, Token token) {
        this.value = value;
        this.token = token;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitLiteral(this);
    }
}

