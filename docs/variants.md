# Variants and Tags

This document explains how to use variants and tags in Flare.

## What is a Variant?
A variant is a custom type that can have a fixed set of named tags (similar to enums or algebraic data types).

## Declaring a Variant
Use the `variant` keyword:

```lang
variant Color { RED, GREEN, BLUE }
```

- This defines a type `Color` with possible values `Color.RED`, `Color.GREEN`, and `Color.BLUE`.

## Using Variants
Declare a variable of a variant type and assign a tag:

```lang
var c: Color = Color.RED;
```

## Pattern Matching (with switch)
You can use `switch` to match on variant tags:

```lang
switch (c) {
    case Color.RED: print("Red!"); break;
    case Color.GREEN: print("Green!"); break;
    case Color.BLUE: print("Blue!"); break;
}
```

## Notes
- Variants are useful for modeling finite sets of states or options.
- Tags can be used in type checks and pattern matching.

