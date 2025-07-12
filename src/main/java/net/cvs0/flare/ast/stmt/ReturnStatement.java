package net.cvs0.flare.ast.stmt;

import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Expression;
import net.cvs0.flare.ast.Statement;

/**
 * Represents a return statement: return expr;
 */
public class ReturnStatement implements Statement
{
    public final Token keyword;
    public final Expression value;

    public ReturnStatement(Token keyword, Expression value) {
        this.keyword = keyword;
        this.value = value;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitReturnStatement(this);
    }
}

