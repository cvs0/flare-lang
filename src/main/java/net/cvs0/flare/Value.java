package net.cvs0.flare;

import net.cvs0.flare.tokens.Type;
import net.cvs0.flare.tokens.TypedType;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a runtime value with a type and data.
 */
public class Value {
    public final Type type;
    public final Object data;
    public final TypedType typedType;

    public Value(Type type, Object data) {
        this.type = type;
        this.data = data;
        this.typedType = null;
    }
    
    public Value(TypedType typedType, Object data) {
        this.type = typedType.baseType;
        this.data = data;
        this.typedType = typedType;
    }

    public static final Value NULL = new Value(Type.NULL, null);

    @Override
    public String toString() {
        if (type == Type.LIST || type == Type.BUFFER) {
            @SuppressWarnings("unchecked")
            List<Value> list = (List<Value>) data;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(list.get(i).toString());
            }
            sb.append("]");
            return sb.toString();
        }
        if (type == Type.BYTES) {
            byte[] bytes = (byte[]) data;
            StringBuilder sb = new StringBuilder("bytes[");
            for (int i = 0; i < bytes.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(bytes[i] & 0xFF);
            }
            sb.append("]");
            return sb.toString();
        }
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
        if (this.type == Type.LIST && other.type == Type.LIST) {
            @SuppressWarnings("unchecked")
            List<Value> leftList = (List<Value>) this.data;
            @SuppressWarnings("unchecked")
            List<Value> rightList = (List<Value>) other.data;
            List<Value> result = new ArrayList<>(leftList);
            result.addAll(rightList);
            if (this.typedType != null) {
                return new Value(this.typedType, result);
            }
            return new Value(Type.LIST, result);
        }
        if (this.type == Type.BUFFER && other.type == Type.BUFFER) {
            @SuppressWarnings("unchecked")
            List<Value> leftList = (List<Value>) this.data;
            @SuppressWarnings("unchecked")
            List<Value> rightList = (List<Value>) other.data;
            List<Value> result = new ArrayList<>(leftList);
            result.addAll(rightList);
            if (this.typedType != null) {
                return new Value(this.typedType, result);
            }
            return new Value(Type.BUFFER, result);
        }
        if (this.type == Type.BYTES && other.type == Type.BYTES) {
            byte[] leftBytes = (byte[]) this.data;
            byte[] rightBytes = (byte[]) other.data;
            byte[] result = new byte[leftBytes.length + rightBytes.length];
            System.arraycopy(leftBytes, 0, result, 0, leftBytes.length);
            System.arraycopy(rightBytes, 0, result, leftBytes.length, rightBytes.length);
            return new Value(Type.BYTES, result);
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
