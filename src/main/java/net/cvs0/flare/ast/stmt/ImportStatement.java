package net.cvs0.flare.ast.stmt;

import net.cvs0.flare.tokens.Token;
import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Statement;

/**
 * Represents an import statement: import moduleName; or import moduleName as alias;
 */
public class ImportStatement implements Statement
{
    public final Token moduleName;
    public final Token alias;

    public ImportStatement(Token moduleName) {
        this.moduleName = moduleName;
        this.alias = null;
    }

    public ImportStatement(Token moduleName, Token alias) {
        this.moduleName = moduleName;
        this.alias = alias;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitImportStatement(this);
    }
}

