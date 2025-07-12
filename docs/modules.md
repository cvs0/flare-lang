# Modules and Imports

This document explains how to use modules and imports in Flare.

## Importing Modules
Use the `import` keyword to include code from another module:

```lang
import math;
```

- This loads the module from a file named `math.lang`.
- Imported modules expose their functions and variables as properties.

## Accessing Module Members
Use dot notation to access functions or variables from a module:

```lang
import math;
var result = math.add(2, 3);
```

## Module Scope
- Each module has its own variable scope.
- Native functions are available in every module context.

## Example
```lang
import utils;
utils.greet("World");
```

## Notes
- Circular imports are not recommended.
- Module names must match their file names (without extension).

