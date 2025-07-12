package net.cvs0.flare.ast.decl;

import net.cvs0.flare.tokens.Token;
import java.util.List;

import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Statement;
import net.cvs0.flare.tag.Tag;

/**
 * Represents a function declaration: func name(params) { ... }
 */
public class FunctionDeclaration implements Statement
{
    public final Token name;
    public final List<Parameter> parameters;
    public final List<Statement> body;
    public final List<Tag> tags;

    public FunctionDeclaration(Token name, List<Parameter> parameters, List<Statement> body, List<Tag> tags) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.tags = tags;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitFunctionDeclaration(this);
    }

    /**
     * Represents a function parameter: name and type.
     */
    public static class Parameter {
        public final Token name;
        public final Token typeToken;
        public Parameter(Token name, Token typeToken) {
            this.name = name;
            this.typeToken = typeToken;
        }
    }
}
