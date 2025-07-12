package net.cvs0.flare.ast.decl;

import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Statement;

import java.util.List;

/**
 * Represents a variant (enum-like) type declaration.
 */
public class VariantDeclaration implements Statement
{
    public final Token name;
    public final List<Token> members;

    public VariantDeclaration(Token name, List<Token> members) {
        this.name = name;
        this.members = members;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitVariantDeclaration(this);
    }
}

