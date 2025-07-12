package net.cvs0.flare.tag;

public class TagValue {
    public final String variantName;
    public final String tagName;

    public TagValue(String variantName, String tagName) {
        this.variantName = variantName;
        this.tagName = tagName;
    }

    @Override
    public String toString() {
        return variantName + "." + tagName;
    }
}

