package net.cvs0.flare.ast;

import net.cvs0.flare.ast.decl.FunctionDeclaration;
import net.cvs0.flare.ast.decl.VariableDeclaration;
import net.cvs0.flare.ast.decl.VariantDeclaration;
import net.cvs0.flare.ast.expr.*;
import net.cvs0.flare.ast.stmt.*;

/**
 * Visitor interface for traversing AST nodes.
 * @param <R> Return type for visitor methods.
 */
public interface ASTVisitor<R> {
    R visitVariableDeclaration(VariableDeclaration node);
    R visitAssignment(Assignment node);
    R visitBlock(Block node);
    R visitIf(If node);
    R visitReturnStatement(ReturnStatement node);
    R visitSwitchStatement(SwitchStatement node);
    R visitForStatement(ForStatement node);
    R visitBinary(Binary node);
    R visitLiteral(Literal node);
    R visitVariableReference(VariableReference node);
    R visitDotAccess(DotAccess node);
    R visitUnary(Unary node);
    R visitFunctionDeclaration(FunctionDeclaration node);
    R visitFunctionCall(FunctionCall node);
    R visitImportStatement(ImportStatement node);
    R visitRegionBlock(RegionBlock node);
    R visitVariantDeclaration(VariantDeclaration node);
    R visitFiberBlock(FiberBlock node);
}
