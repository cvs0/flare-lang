import std;

func main() {
    string? name = null;
    if (name == null) {
        std.print("name is null");
    }

    name = "Alice";

    if (name != null) {
        std.print("name is not null");
    }

    string greeting = name ?? "Guest";
    std.print("Hello, " + greeting);

    int i = 0;
    while (i < 3) {
        std.print(i);
        i = i + 1;
    }
}
