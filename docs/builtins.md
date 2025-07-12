# Built-in Functions

This document lists and explains the built-in functions available in Flare.

## print(value)
Prints the value to the console.

```lang
print("Hello, world!");
```
- Accepts any type as an argument.
- Output is sent to standard output.

## memoryStats()
Displays memory usage statistics for the virtual machine.

```lang
memoryStats();
```
- No arguments.
- Output format: implementation-dependent (typically shows used/total memory).

## vmInfo()
Displays information about the virtual machine.

```lang
vmInfo();
```
- No arguments.
- Output includes VM name and version.

## Notes
- Built-in functions are always available in the global scope.
- Some built-ins may be implemented natively for performance or integration.
- Additional built-ins may be added in future versions.

