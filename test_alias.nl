import std as io;
import string as str;

func greet(name: string) {
    io.print("Hello, " + name + "!");
}

func main() {
    string name = "World";

    greet(name);

    io.print(str.length(name) + " characters in name.");
}