# TODO: Function Return Types & Type-Safe Calls

## ✅ Explicit Return Types
- [ ] Support `func name(): type {}` syntax in the parser
    - Parse the return type after the parameter list
    - Store it in the function AST node

- [ ] At runtime, enforce return type in `return` statements
    - If return type is declared, check that returned value matches
    - Throw a type error if mismatched

## ✅ Typed Function Calls in Assignments
- [ ] Support calling functions in typed variable declarations:
  ```lang
  int x = getInt();
  ```

- [ ] During assignment, check that:
    - The function was called correctly
    - The return type matches the declared variable type
    - Throw a type error if mismatched
