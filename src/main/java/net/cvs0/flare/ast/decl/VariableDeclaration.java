package net.cvs0.flare.ast.decl;

import java.util.List;
import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Expression;
import net.cvs0.flare.ast.Statement;
import net.cvs0.flare.tag.Tag;

/**
 * Variable declaration statement: e.g., int x = 5;
 */
public class VariableDeclaration implements Statement
{
    public final Token typeToken;
    public final Token name;
    public final Expression initializer;
    public final List<Tag> tags;

    public VariableDeclaration(Token typeToken, Token name, Expression initializer, List<Tag> tags) {
        this.typeToken = typeToken;
        this.name = name;
        this.initializer = initializer;
        this.tags = tags;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitVariableDeclaration(this);
    }
}
