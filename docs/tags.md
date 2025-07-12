# Tags

This document explains tags in Flare, which are used for metadata.

## What is a Tag?
A tag is to annotate code with metadata.

## Tags as Metadata

```lang
tag deprecated("Use newFunc instead")
func oldFunc() {
    // ...
}
```

- Metadata tags are not required for basic usage, but can be used for documentation, warnings, or tooling.

## Tags as Metadata for Functions

Tags can be used to annotate functions with metadata. This is useful for marking functions as deprecated, experimental, or for documentation purposes.

### Syntax
A tag is written before a function (or type) using the `tag` prefix, followed by the tag name and optional arguments in parentheses:

```lang
tag deprecated("Use newFunc instead")
func oldFunc() {
    // ...
}
```

- The tag name is `deprecated`.
- The argument is a string explaining the reason or alternative.
- Multiple tags can be stacked above a function.

### Supported Use Cases
- `tag deprecated(reason)` — Marks a function as deprecated. The interpreter may print a warning when the function is called.
- `tag experimental` — Marks a function as experimental (no arguments needed).
- Custom tags can be defined for documentation or tooling.

### Example: Multiple Tags
```lang
tag deprecated("Use newFunc instead")
tag experimental
func oldFunc() {
    // ...
}
```

### How Tags Work
- Tags are attached to the function's AST node and can be accessed by the interpreter or tools.
- Tags do not affect runtime behavior unless the interpreter is programmed to recognize them (e.g., print a warning for `tag deprecated`).

### Notes
- Arguments to tags can be strings, numbers, or other literals.
- Tags are not variables and do not affect the function's logic directly.

## Notes
- Tags are always associated with a parent variant or as metadata.
- Tags are not standalone types.
