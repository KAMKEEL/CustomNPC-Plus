/**
 * ECMAScript 5.1 - Number type for working with numeric values and constants.
 * Access static constants like Number.MAX_VALUE or format numbers with instance methods.
 *
 * ## How to Use
 *
 * ### Number Constants
 * ```js
 * Number.MAX_VALUE;             // ~1.79e+308 (largest representable number)
 * Number.MIN_VALUE;             // ~5e-324    (smallest positive number)
 * Number.NaN;                   // NaN
 * Number.POSITIVE_INFINITY;     // Infinity
 * Number.NEGATIVE_INFINITY;     // -Infinity
 * ```
 *
 * ### Formatting Numbers
 * ```js
 * var n = 3.14159;
 * n.toFixed(2);                 // "3.14"     (fixed decimal places)
 * n.toFixed(0);                 // "3"
 * n.toPrecision(4);             // "3.142"   (significant digits)
 * n.toExponential(2);           // "3.14e+0" (scientific notation)
 * ```
 *
 * ### Converting to String
 * ```js
 * var n = 255;
 * n.toString();                 // "255"     (base 10, default)
 * n.toString(16);               // "ff"      (hexadecimal)
 * n.toString(2);                // "11111111" (binary)
 * n.toString(8);                // "377"     (octal)
 * ```
 *
 * ### ❌ Common Mistakes
 * ```js
 * Number.toFixed(2);            // TypeError — instance method, not static
 * (3).toFixed(2);               // "3.00" ✅ — wrap literal in parentheses
 * 3.toFixed(2);                 // SyntaxError — parser reads 3. as float
 * ```
 */
export interface Number {
  /** The largest representable number (~1.79e+308) */
  MAX_VALUE: number;

  /** The smallest representable positive number (~5e-324) */
  MIN_VALUE: number;

  /** Represents "Not a Number" (NaN) - the result of undefined or invalid arithmetic operations */
  NaN: number;

  /** Represents negative infinity, the result of overflow or division by zero */
  NEGATIVE_INFINITY: number;

  /** Represents positive infinity, the result of overflow or division by zero */
  POSITIVE_INFINITY: number;

  /** Formats the number using exponential notation with optional fractional digits */
  toExponential(fractionDigits: number): string;

  /** Formats the number as a fixed-point string with the specified number of fractional digits (0-20) */
  toFixed(fractionDigits: number): string;

  /** Returns a localized string representation of the number (format depends on locale) */
  toLocaleString(): string;

  /** Formats the number as a string with the specified number of significant digits (1-21) */
  toPrecision(precision: number): string;

  /** Converts the number to a string in the specified radix (base 2-36, default 10) */
  toString(radix: number): string;

  /** Returns the primitive numeric value of this Number object */
  valueOf(): number;
}
