/**
 * ECMAScript 5.1 - RegExp type for pattern matching and string manipulation.
 * Create with the RegExp constructor or literal syntax: /pattern/flags
 *
 * ## How to Use
 *
 * ### Creating Regular Expressions
 * ```js
 * // Method 1: Regex literal (preferred)
 * var re = /hello/;
 * var reFlags = /hello/gi;                  // g=global, i=ignore case, m=multiline
 *
 * // Method 2: Constructor (useful for dynamic patterns)
 * var re2 = new RegExp("hello");
 * var re3 = new RegExp("hello", "gi");
 * var dynamic = new RegExp(playerName);     // Build pattern from variable
 * ```
 *
 * ### Testing for Matches
 * ```js
 * /\d+/.test("abc123");                     // true  — contains digits
 * /^\d+$/.test("abc123");                   // false — not ALL digits
 * /^\d+$/.test("12345");                    // true  — all digits
 * /hello/i.test("Hello World");             // true  — case insensitive
 * ```
 *
 * ### Extracting Matches
 * ```js
 * var match = /\d+/.exec("abc123def");      // ["123"]
 * var noMatch = /\d+/.exec("no digits");    // null
 *
 * // With capturing groups
 * var result = /(\w+)@(\w+)/.exec("user@domain");
 * // result[0] = "user@domain"  (full match)
 * // result[1] = "user"         (first group)
 * // result[2] = "domain"       (second group)
 * ```
 *
 * ### Using with String Methods
 * ```js
 * "abc123".match(/\d+/);                    // ["123"]
 * "abc123".search(/\d/);                    // 3 (index of first digit)
 * "abc123".replace(/\d+/, "NUM");           // "abcNUMdef"
 * "a,b,,c".split(/,+/);                     // ["a", "b", "c"]
 * ```
 *
 * ### RegExp Properties
 * ```js
 * var re = /hello/gi;
 * re.source;                                // "hello"
 * re.global;                                // true
 * re.ignoreCase;                            // true
 * re.multiline;                             // false
 * re.lastIndex;                             // 0 (changes with global exec/test)
 * ```
 *
 * ### ❌ Common Mistakes
 * ```js
 * RegExp.test("string");                    // TypeError — test is an INSTANCE method!
 * new RegExp.test("string");                // TypeError — incorrect syntax
 * // ✅ CORRECT:
 * /pattern/.test("string");                 // Use on a regex instance
 * new RegExp("pattern").test("string");     // Or create instance first
 *
 * // Double-escape backslashes in constructor strings:
 * new RegExp("\d+");                        // Wrong — \d becomes just d
 * new RegExp("\\d+");                       // ✅ Correct — \\d becomes \d
 * ```
 */
export interface RegExp {
  /** The pattern string that was used to create this regular expression */
  source: string;

  /** True if the 'g' (global) flag is set, affecting match behavior in exec() and test() */
  global: boolean;

  /** True if the 'i' (ignore case) flag is set, making pattern matching case-insensitive */
  ignoreCase: boolean;

  /** True if the 'm' (multiline) flag is set, affecting ^ and $ anchor behavior */
  multiline: boolean;

  /** The index at which to start searching in the string (used with 'g' flag in exec() loops) */
  lastIndex: number;

  /** Searches the string for a match and returns an array of matches or null if no match found */
  exec(string: string): any[];

  /** Tests whether the pattern matches the string, returning true or false */
  test(string: string): boolean;

  /** Returns a string representation of the regular expression (e.g., "/pattern/flags") */
  toString(): string;
}
