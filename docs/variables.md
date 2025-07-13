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
- `null` — Represents the absence of a value. Can be assigned to variables of any type, but the variable must be checked for nullability before use.

## Null Handling
Flare introduces `null` as a special value to indicate that a variable does not hold any valid data. This value is usable with any type but requires special handling:
- **Null Coalescing** (`??`) can be used to provide a fallback value if a variable is `null`. For example:
  ```lang
  string name = null ?? "Default Name";
  ```
- **Nullable Types**: Types can be declared as nullable by appending a `?` to the type, for instance, `string?` or `int?`, which means the variable can hold either a value of that type or `null`.

## Type Inference
If no type is specified, it may default to `any` (implementation-dependent). Additionally, if a variable is assigned `null`, it may implicitly take the `any` type, unless explicitly defined as nullable.
