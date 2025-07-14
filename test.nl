import std;

func main() {
    // Basic list literals
    list numbers = [1, 2, 3, 4, 5];
    list words = ["hello", "world", "flare"];

    // Range expressions - creates lists from ranges
    list range1 = [1..5];        // Creates [1, 2, 3, 4, 5]
    list range2 = [10..15];      // Creates [10, 11, 12, 13, 14, 15]
    list range3 = [0..3];        // Creates [0, 1, 2, 3]

    // Empty list
    list empty = [];

    // List concatenation using +
    list combined = numbers + range1;

    // Index access
    int first = numbers[0];       // Gets 1
    string second = words[1];     // Gets "world"
    int rangeFirst = range1[2];   // Gets 3

    // Print the results
    std.print("Numbers list:");
    std.print(numbers);

    std.print("Words list:");
    std.print(words);

    std.print("Range 1..5:");
    std.print(range1);

    std.print("Range 10..15:");
    std.print(range2);

    std.print("Combined numbers + range1:");
    std.print(combined);

    std.print("First element of numbers:");
    std.print(first);

    std.print("Second element of words:");
    std.print(second);

    std.print("Third element of range1:");
    std.print(rangeFirst);
}