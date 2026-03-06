/**
 * ECMAScript 5.1 - Global functions available in the global scope.
 * These are not on any specific object - call them directly (e.g., parseInt("42")).
 *
 * ## How to Use
 *
 * ### Parsing Numbers
 * ```js
 * parseInt("42");                           // 42
 * parseInt("42px");                         // 42 (stops at non-digit)
 * parseInt("0xFF", 16);                     // 255 (hexadecimal)
 * parseInt("111", 2);                       // 7   (binary)
 * parseFloat("3.14");                       // 3.14
 * parseFloat("3.14abc");                    // 3.14 (stops at invalid char)
 * ```
 *
 * ### Checking Special Values
 * ```js
 * isNaN(NaN);                               // true
 * isNaN(42);                                // false
 * isNaN("hello");                           // true  (cannot parse as number)
 * isNaN("42");                              // false (can parse as number)
 * isFinite(42);                             // true
 * isFinite(Infinity);                       // false
 * isFinite(NaN);                            // false
 * ```
 *
 * ### URI Encoding/Decoding
 * ```js
 * encodeURI("hello world");                 // "hello%20world"
 * decodeURI("hello%20world");               // "hello world"
 * encodeURIComponent("a=1&b=2");            // "a%3D1%26b%3D2" (encodes & and =)
 * decodeURIComponent("a%3D1%26b%3D2");      // "a=1&b=2"
 * ```
 *
 * ### ❌ Common Mistakes
 * ```js
 * parseInt("08");                           // Some engines treat as octal! Always specify radix
 * parseInt("08", 10);                       // 8 ✅ — explicitly base-10
 * parseInt("");                             // NaN — empty string returns NaN, not 0
 * parseFloat("");                           // NaN
 * isNaN(undefined);                         // true — coerces to NaN first
 * ```
 */
export interface GlobalFunctions {
  /** Evaluates JavaScript code in a string (use with caution - slow and potential security issues) */
  static eval(x: any): any;

  /** Parses a string and returns an integer. Stops parsing at the first non-digit character */
  static parseInt(string: string): number;

  /** Parses a string and returns an integer using the specified radix (base). Stops parsing at the first non-digit character */
  static parseInt(string: string, radix: number): number;

  /** Parses a string and returns a floating-point number. Stops parsing at the first invalid character */
  static parseFloat(string: string): number;

  /** Determines whether a value is NaN (not-a-number). The value is first coerced to a number before testing */
  static isNaN(value: any): boolean;

  /** Determines whether a value is a finite number. The value is first coerced to a number. Returns false for Infinity, -Infinity, and NaN */
  static isFinite(value: any): boolean;

  /** Decodes a URI by replacing encoded sequences like %20 with their original characters */
  static decodeURI(encodedURI: string): string;

  /** Decodes URI component by replacing ALL encoded sequences including those for reserved URI characters */
  static decodeURIComponent(encodedURIComponent: string): string;

  /** Encodes a URI by replacing special characters with %HH sequences (but preserves reserved characters like / : ?) */
  static encodeURI(uri: string): string;

  /** Encodes URI component by replacing ALL special characters including reserved ones with %HH sequences */
  static encodeURIComponent(uriComponent: string): string;
}
