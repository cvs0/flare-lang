package net.cvs0.flare.ast.stmt;

import net.cvs0.flare.ast.ASTVisitor;
import net.cvs0.flare.ast.Expression;
import net.cvs0.flare.ast.Statement;

import java.util.List;

public class SwitchStatement implements Statement
{
    public final Expression expression;
    public final List<SwitchCase> cases;

    public SwitchStatement(Expression expression, List<SwitchCase> cases) {
        this.expression = expression;
        this.cases = cases;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visitSwitchStatement(this);
    }

    public static class SwitchCase {
        public final String label;
        public final List<Statement> body;
        public SwitchCase(String label, List<Statement> body) {
            this.label = label;
            this.body = body;
        }
    }
}
