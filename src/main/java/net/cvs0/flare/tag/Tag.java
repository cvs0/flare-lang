package net.cvs0.flare.tag;

import net.cvs0.flare.Value;

import java.util.List;

public class Tag {
    public final String name;
    public final List<Value> arguments;

    public Tag(String name, List<Value> arguments) {
        this.name = name;
        this.arguments = arguments;
    }
}
