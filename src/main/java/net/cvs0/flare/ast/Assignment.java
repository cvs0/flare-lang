package net.cvs0.flare.ast;

import net.cvs0.flare.tokens.Token;

/**
 * Assignment statement: e.g., x = x + 1;
 */
public class Assignment implements Statement, Expression {
    public final Token name;
    public final Token operator;
    public final Expression value;

    public Assignment(Token name, Token operator, Expression value) {
        this.name = name;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitAssignment(this);
    }
}
