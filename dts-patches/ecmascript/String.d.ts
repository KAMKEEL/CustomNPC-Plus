/**
 * ECMAScript 5.1 - String type for working with text. Strings are immutable sequences of characters.
 * Use String.fromCharCode() to create a string from character codes.
 *
 * ## How to Use
 *
 * ### Creating Strings
 * ```js
 * var greeting = "Hello, world!";           // String literal (double quotes)
 * var name = 'Steve';                       // String literal (single quotes)
 * var fromCode = String.fromCharCode(65);   // "A" (from char code)
 * ```
 *
 * ### Searching & Checking
 * ```js
 * var str = "Hello, world!";
 * str.indexOf("world");                    // 7
 * str.indexOf("missing");                  // -1 (not found)
 * str.lastIndexOf("l");                    // 10
 * str.search(/world/);                     // 7 (regex search)
 * str.charAt(0);                           // "H"
 * str.charCodeAt(0);                       // 72
 * str.length;                              // 13
 * ```
 *
 * ### Extracting & Transforming
 * ```js
 * var str = "Hello, world!";
 * str.slice(7, 12);                        // "world"
 * str.substring(0, 5);                     // "Hello"
 * str.toUpperCase();                       // "HELLO, WORLD!"
 * str.toLowerCase();                       // "hello, world!"
 * str.trim();                              // Removes leading/trailing whitespace
 * str.replace("world", "NPC");             // "Hello, NPC!"
 * str.replace(/l/g, "L");                  // "HeLLo, worLd!" (regex with global flag)
 * ```
 *
 * ### Splitting & Joining
 * ```js
 * "a,b,c".split(",");                      // ["a", "b", "c"]
 * "hello".split("");                        // ["h", "e", "l", "l", "o"]
 * ["a", "b", "c"].join("-");               // "a-b-c" (Array.join, but related)
 * "one two three".split(" ", 2);            // ["one", "two"] (limit results)
 * ```
 *
 * ### Pattern Matching
 * ```js
 * "abc123".match(/\d+/);                   // ["123"]
 * "no numbers".match(/\d+/);               // null
 * "hello".concat(" ", "world");            // "hello world"
 * ```
 *
 * ### ❌ Common Mistakes
 * ```js
 * str.replace("l", "L");                   // Only replaces FIRST "l" — use /l/g for all
 * String.charAt(0);                        // TypeError — instance method, not static
 * "abc"[0];                                // May not work in all engines — use charAt()
 * ```
 */
export interface String {
  /** The number of characters in the string */
  length: number;

  /** Creates a string from one or more Unicode character codes (e.g., String.fromCharCode(65, 66, 67) returns "ABC") */
  static fromCharCode(...codes: number): string;

  /** Returns the Unicode code point (0–65535) of the character at the specified index. Unlike Java's charAt() + cast, this returns the numeric code directly. */
  charCodeAt(pos: number): number;

  /** Tests the string against a regular expression and returns an array of matches, or null if no match found */
  match(regexp: any): any[];

  /**
   * Replaces a pattern (string or RegExp object) with a replacement string.
   * Unlike Java's replace/replaceAll which only accept String/CharSequence,
   * this accepts a RegExp object as the first argument.
   */
  replace(searchValue: any, replaceValue: any): string;

  /** Returns the index of the first character that matches a regular expression, or -1 if not found */
  search(regexp: any): number;
}
