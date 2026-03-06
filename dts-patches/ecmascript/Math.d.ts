/**
 * ECMAScript 5.1 - Math built-in object providing mathematical constants and functions.
 * All methods are static and called as Math.methodName(). No constructor.
 *
 * ## How to Use
 *
 * Math is a **static object** — use its methods directly, never with `new`.
 *
 * ### Basic Math Operations
 * ```js
 * var rounded = Math.floor(3.7);          // 3
 * var ceiling = Math.ceil(3.2);            // 4
 * var nearest = Math.round(3.5);           // 4
 * var absolute = Math.abs(-42);            // 42
 * ```
 *
 * ### Powers, Roots & Logarithms
 * ```js
 * var squared = Math.pow(2, 3);            // 8
 * var root = Math.sqrt(16);                // 4
 * var natural = Math.log(Math.E);          // 1
 * ```
 *
 * ### Random Numbers
 * ```js
 * var rand = Math.random();                // 0.0 to 0.999...
 * // Random integer between 1 and 10:
 * var dice = Math.floor(Math.random() * 10) + 1;
 * ```
 *
 * ### Finding Min/Max
 * ```js
 * var biggest = Math.max(5, 10, 3);        // 10
 * var smallest = Math.min(5, 10, 3);       // 3
 * ```
 *
 * ### Trigonometry
 * ```js
 * var sine = Math.sin(Math.PI / 2);        // 1
 * var cosine = Math.cos(0);                // 1
 * var angle = Math.atan2(1, 1);            // 0.785... (PI/4)
 * ```
 *
 * ### ❌ Common Mistakes
 * ```js
 * new Math()                                // TypeError — Math is not a constructor
 * Math()                                    // TypeError — Math is not a function
 * ```
 */
export interface Math {
  /** Euler's number (~2.718), the base of natural logarithms */
  static readonly E: number;

  /** Natural logarithm of 2 (~0.693) */
  static readonly LN2: number;

  /** Natural logarithm of 10 (~2.303) */
  static readonly LN10: number;

  /** Base-2 logarithm of e (~1.443) */
  static readonly LOG2E: number;

  /** Base-10 logarithm of e (~0.434) */
  static readonly LOG10E: number;

  /** Ratio of a circle's circumference to its diameter (~3.14159) */
  static readonly PI: number;

  /** Square root of 0.5 (~0.707) */
  static readonly SQRT1_2: number;

  /** Square root of 2 (~1.414) */
  static readonly SQRT2: number;

  /** Returns the absolute value of a number */
  static abs(x: number): number;

  /** Returns the arccosine (in radians) of a number */
  static acos(x: number): number;

  /** Returns the arcsine (in radians) of a number */
  static asin(x: number): number;

  /** Returns the arctangent (in radians) of a number */
  static atan(x: number): number;

  /** Returns the arctangent of two numbers (y/x), with proper quadrant handling */
  static atan2(y: number, x: number): number;

  /** Returns the smallest integer greater than or equal to a number */
  static ceil(x: number): number;

  /** Returns the cosine (in radians) of a number */
  static cos(x: number): number;

  /** Returns e raised to the power of a number */
  static exp(x: number): number;

  /** Returns the largest integer less than or equal to a number */
  static floor(x: number): number;

  /** Returns the natural logarithm (base e) of a number */
  static log(x: number): number;

  /** Returns the largest of zero or more numbers */
  static max(...values: number): number;

  /** Returns the smallest of zero or more numbers */
  static min(...values: number): number;

  /** Returns base raised to the power of exponent */
  static pow(x: number, y: number): number;

  /** Returns a random number between 0 (inclusive) and 1 (exclusive) */
  static random(): number;

  /** Returns the value of a number rounded to the nearest integer */
  static round(x: number): number;

  /** Returns the sine (in radians) of a number */
  static sin(x: number): number;

  /** Returns the square root of a number */
  static sqrt(x: number): number;

  /** Returns the tangent (in radians) of a number */
  static tan(x: number): number;
}
