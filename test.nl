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
    return true;
}

func main() {
    int x = testReturn();
    std.print(x);

    string greeting = returnStr();
    boolean isTrue = returnBool();

    std.print(isTrue);
    std.print(greeting);
}