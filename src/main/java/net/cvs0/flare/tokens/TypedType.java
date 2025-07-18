package net.cvs0.flare.tokens;

/**
 * Represents a typed type like list<int>, buffer<string>, etc.
 */
public class TypedType {
    public final Type baseType;
    public final Type elementType;
    
    public TypedType(Type baseType, Type elementType) {
        this.baseType = baseType;
        this.elementType = elementType;
    }
    
    @Override
    public String toString() {
        return baseType.toString().toLowerCase() + "<" + elementType.toString().toLowerCase() + ">";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TypedType)) return false;
        TypedType other = (TypedType) obj;
        return baseType == other.baseType && elementType == other.elementType;
    }
    
    @Override
    public int hashCode() {
        return baseType.hashCode() * 31 + elementType.hashCode();
    }
}