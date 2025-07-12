# Built-in Functions

This document lists and explains the built-in functions available in Flare.

---

## 📦 `std` Library

### `print(value)`
Prints the value to the console.

```lang
import std;
std.print("Hello, world!");
```

- Accepts any type as an argument.
- Output is sent to standard output.

---

## 🔢 `math` Library

### `sqrt(number)`
Returns the square root of a number.

```lang
import math;
float root = math.sqrt(9.0);
```

- Accepts one `int` or `float`.
- Returns a `float`.

### `abs(number)`
Returns the absolute value of a number.

```lang
int absVal = math.abs(-5);
```

- Accepts one `int` or `float`.
- Returns the same type.

### `max(a, b)`
Returns the larger of two numbers.

```lang
int m = math.max(5, 9);
```

- Requires two arguments of the same type (`int` or `float`).
- Returns the same type.

### `min(a, b)`
Returns the smaller of two numbers.

```lang
int m = math.min(3, 8);
```

- Requires two arguments of the same type.
- Returns the same type.

### `round(float)`
Rounds a float to the nearest integer.

```lang
int r = math.round(3.6);
```

- Accepts one `float`.
- Returns an `int`.

---

## 🔤 `string` Library

### `length(string)`
Returns the length of a string.

```lang
int len = string.length("hello");
```

### `upper(string)`
Converts the string to uppercase.

```lang
string up = string.upper("flare");
```

### `lower(string)`
Converts the string to lowercase.

```lang
string low = string.lower("FLARE");
```

### `contains(haystack, needle)`
Checks if a string contains a substring.

```lang
bool has = string.contains("hello", "ell");
```

### `substring(string, start[, end])`
Returns a substring from a given string.

```lang
string part = string.substring("hello", 1, 3);  // "el"
```

- If `end` is not provided, extracts to the end.

---

## 📂 `file` Library

### `read(path)`
Reads a file’s contents into a string.

```lang
string text = file.read("data.txt");
```

- Accepts 1 string (path).
- Returns file content as string.
- Throws on error.

---

## 🌐 `net` Library

### `fetch(url)`
Fetches content from a URL.

```lang
string body = net.fetch("https://example.com");
```

- Accepts 1 string (URL).
- Returns page content.

---

## 🕒 `time` Library

### `now()`
Returns the current Unix timestamp in milliseconds.

```lang
int ts = time.now();
```

---

## 🎲 `rand` Library

### `seed(value)`
Sets the seed for random generation.

```lang
rand.seed(1234);
```

### `int(min, max)`
Returns a random integer in range `[min, max]`.

```lang
int r = rand.int(1, 10);
```

### `float()`
Returns a random float between 0.0 and 1.0.

```lang
float r = rand.float();
```

---

## 🖥 `vm` Library

### `maxMemory()`
Returns the maximum memory the VM can use.

```lang
int max = vm.maxMemory();
```

### `freeMemory()`
Returns the amount of free memory.

```lang
int free = vm.freeMemory();
```

### `totalMemory()`
Returns the total allocated memory.

```lang
int total = vm.totalMemory();
```

### `usedMemory()`
Returns the memory currently in use.

```lang
int used = vm.usedMemory();
```

---

## Notes

- Libraries must be imported using `import <name>;`.
- Functions are grouped by module.
- Type safety and argument count are enforced at runtime.
- More libraries may be added in future versions.
