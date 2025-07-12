import std as standard;
import string;

tag deprecated("use greetNew() instead")
func greet(name: string) {
    standard.print("Hello, " + name + "!");
}

func greetNew(name: string, age: int) {
    standard.print("Hello, " + name + "! You are " + age + " years old.");
}

func main() {
    string name = "World";
    boolean deprecated = true;

    standard.print(deprecated);

    greet(name);
    greetNew(name, 30);

    standard.print(string.length(name) + " characters in name.");

    if (deprecated) {
        standard.print("The greet function is deprecated. Use greetNew instead.");
    } else {
        standard.print("The greet function is not deprecated.");
    }
}