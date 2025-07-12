# Fibers (Concurrency)

This document explains fibers and concurrent execution in Flare.

## What is a Fiber?
A fiber is a lightweight thread of execution. Fibers allow code to run concurrently with the main program and with other fibers.

## Declaring a Fiber
Use the `fiber` keyword and a name:

```lang
fiber worker {
    printNumbers("Worker");
}
```

- The code inside the fiber block runs in a new thread.
- Fibers start automatically when declared.

## Example: Multiple Fibers
```lang
fiber alpha {
    printNumbers("Alpha");
}

fiber beta {
    printNumbers("Beta");
}

func printNumbers(name: string) {
    for (int i = 1; i <= 5; i += 1) {
        print(name + ": " + i);
    }
}
```

## Fiber Scope
- Each fiber has its own variable context.
- Function calls inside fibers have their own local variables.

## Use Cases
- Parallel tasks
- Background processing
- Asynchronous workflows

