import std;
import string as str;

func greet(name: string) {
    std.print("Hello, " + name + "!");
}

func main() {
    string name = "World";

    greet(name);

    std.print(str.length(name) + " characters in name.");
}