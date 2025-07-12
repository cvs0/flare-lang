# Control Flow

This document covers control flow statements in Flare: if, for, and switch.

## If Statements
Conditional execution:

```lang
if (x > 0) {
    print("Positive");
} else {
    print("Non-positive");
}
```

## For Loops
Iterate with initialization, condition, and increment:

```lang
for (int i = 0; i < 10; i += 1) {
    print(i);
}
```

- All parts (init, condition, increment) are optional.
- Loop variables are scoped to the loop.

## Switch Statements
Multi-way branching:

```lang
switch (color) {
    case "red": print("Red"); break;
    case "blue": print("Blue"); break;
    default: print("Other");
}
```

- Use `break;` to exit a case.
- `default` is optional and matches any unmatched value.

