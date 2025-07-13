import std;
import str;

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

    if (str.length(name) > 0) {
        std.print("Name length is greater than 0");
    } else {
        std.print("Name length is 0");
    }

    int i = 0;
    while (i < 3) {
        std.print(i);
        i = i + 1;
    }
}
