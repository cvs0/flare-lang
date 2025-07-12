package net.cvs0.flare.ast;

/**
 * Base interface for all AST nodes.
 */
public interface ASTNode {
    <R> R accept(ASTVisitor<R> visitor);
}

