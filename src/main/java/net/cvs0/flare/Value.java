package net.cvs0.flare;

import net.cvs0.flare.tokens.Type;

/**
 * Represents a runtime value with a type and data.
 */
public class Value {
    public final Type type;
    public final Object data;

    public Value(Type type, Object data) {
        this.type = type;
        this.data = data;
    }

    public static final Value NULL = new Value(Type.NULL, null);

    @Override
    public String toString() {
        return String.valueOf(data);
    }

    public Value plus(Value other) {
        if (this.type == Type.INT && other.type == Type.INT) {
            return new Value(Type.INT, (int)this.data + (int)other.data);
        }
        if (this.type == Type.FLOAT || other.type == Type.FLOAT) {
            double left = this.type == Type.FLOAT ? (double)this.data : (int)this.data;
            double right = other.type == Type.FLOAT ? (double)other.data : (int)other.data;
            return new Value(Type.FLOAT, left + right);
        }
        if (this.type == Type.STRING || other.type == Type.STRING) {
            return new Value(Type.STRING, this.data.toString() + other.data.toString());
        }
        throw new RuntimeException("Unsupported types for plus: " + this.type + ", " + other.type);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Value)) return false;
        Value other = (Value) o;
        if (this.type != other.type) return false;
        return this.data == null ? other.data == null : this.data.equals(other.data);
    }
}
