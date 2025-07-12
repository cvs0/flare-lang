func testReturn() {
    return 42;
}

func getStr(name: string) {
    return "Hello, " + name;
}

func returnStr() {
    return "hi";
}

func returnBool() {
    return false;
}

func main() {
    // Ternary expression added here
    int x = testReturn();
    std.print(x);

    string greeting = returnStr();
    boolean isTrue = returnBool();

    // Example of a ternary operator in an expression
    int y = (isTrue ? 1 : 0);  // Ternary: if isTrue is true, y = 1, else y = 0
    std.print(y);  // This will print 1 since returnBool() returns true

    std.print(isTrue);  // Prints true
    std.print(greeting);  // Prints "hi"
}
