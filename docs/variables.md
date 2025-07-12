# Variables and Types

This document explains variable declaration, assignment, and supported types in Flare.

## Declaration
Declare a variable with a type and optional initializer:

```lang
int x = 10;
float y;

string name = "Flare";
```

## Assignment
Assign a value to a variable:

```lang
x = 42;
y = 3.14;
```

## Supported Types
- `int` — Integer
- `float` — Floating-point
- `string` — Text
- `bool` — Boolean (true/false)
- `any` — Dynamic type
- `function`, `module`, `variant`, `tag` — Advanced types
- `boolean` — True/false values
- 
## Type Inference
If no type is specified, it may default to `any` (implementation-dependent).

