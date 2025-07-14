package net.cvs0.flare;

import net.cvs0.flare.tokens.Type;
import java.util.List;
import java.util.ArrayList;

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
        if (type == Type.LIST) {
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
            return new Value(Type.LIST, result);
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
