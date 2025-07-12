# TODO: Function Return Types & Type-Safe Calls

## ✅ Typed Function Calls in Assignments
- [ ] Support calling functions in typed variable declarations:
  ```lang
  int x = getInt();
  ```

- [ ] During assignment, check that:
    - The function was called correctly
    - The return type matches the declared variable type
    - Throw a type error if mismatched
