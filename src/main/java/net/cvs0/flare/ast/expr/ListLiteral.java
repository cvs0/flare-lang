package net.cvs0.flare.ast.expr;

import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Expression;

import java.util.List;

public class ListLiteral implements Expression {
    public final List<Expression> elements;

    public ListLiteral(List<Expression> elements) {
        this.elements = elements;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitListLiteral(this);
    }
}