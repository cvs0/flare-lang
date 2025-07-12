# Functions

This document explains function declaration, parameters, return values, and calling functions in Flare.

## Declaration
Define a function with `func`:

```lang
import std;

func add(a: int, b: int): int {
    return a + b;
}

func greet(name: string) {
    std.print("Hello, " + name);
}
```

- Functions can return a value (with `return`) or be void (no return).
- Parameters are typed and comma-separated.

## Calling Functions
Call a function by name with arguments:

```lang
add(2, 3);
greet("Brody");
```

## Recursion
Functions can call themselves:

```lang
func factorial(n: int) {
    if (n <= 1) return 1;
    return n * factorial(n - 1);
}
```

## Function Values
Functions are first-class values and can be assigned to variables or passed as arguments (advanced usage).

