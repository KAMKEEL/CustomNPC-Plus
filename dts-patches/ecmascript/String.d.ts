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
  /** Creates a string from one or more Unicode character codes (e.g., String.fromCharCode(65, 66, 67) returns "ABC") */
  fromCharCode(...codes: number[]): string;

  /** The number of characters in the string */
  length: number;

  /** Returns the character at the specified index, or an empty string if the index is out of range */
  charAt(pos: number): string;

  /** Returns the Unicode code point (0-65535) of the character at the specified index */
  charCodeAt(pos: number): number;

  /** Combines this string with other strings and returns a new combined string */
  concat(...strings: string[]): string;

  /** Returns the index of the first occurrence of the search string, or -1 if not found */
  indexOf(searchString: string, position: number): number;

  /** Returns the index of the last occurrence of the search string, or -1 if not found */
  lastIndexOf(searchString: string, position: number): number;

  /** Compares this string to another string and returns a number indicating sort order (-1, 0, or 1) */
  localeCompare(that: string): number;

  /** Tests the string against a regular expression and returns an array of matches or null if no match */
  match(regexp: any): any[];

  /** Replaces the first occurrence of a pattern (string or regex) with a replacement string or the result of a function */
  replace(searchValue: any, replaceValue: any): string;

  /** Returns the index of the first character that matches a regular expression, or -1 if not found */
  search(regexp: any): number;

  /** Returns a new string containing the characters from the start index up to (but not including) the end index */
  slice(start: number, end: number): string;

  /** Splits the string into an array of substrings using the separator, optionally limiting the number of splits */
  split(separator: any, limit: number): string[];

  /** Returns a new string containing characters from the start index up to (but not including) the end index */
  substring(start: number, end: number): string;

  /** Returns a new string with all uppercase characters converted to lowercase */
  toLowerCase(): string;

  /** Returns a new string with all uppercase characters converted to lowercase according to locale rules */
  toLocaleLowerCase(): string;

  /** Returns a new string with all lowercase characters converted to uppercase */
  toUpperCase(): string;

  /** Returns a new string with all lowercase characters converted to uppercase according to locale rules */
  toLocaleUpperCase(): string;

  /** Returns a new string with whitespace (spaces, tabs, newlines, etc.) removed from both ends */
  trim(): string;

  /** Returns the string itself */
  toString(): string;

  /** Returns the primitive value of the string object */
  valueOf(): string;
}
