func testReturn() {
    return 42;
}

func getStr(name: string) {
    return "Hello, " + name;
}

func main() {
    int x = testReturn();
    std.print(x);

    string greeting = testReturn();

    std.print(greeting);
}