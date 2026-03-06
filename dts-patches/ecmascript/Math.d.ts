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
  E: number;

  /** Natural logarithm of 2 (~0.693) */
  LN2: number;

  /** Natural logarithm of 10 (~2.303) */
  LN10: number;

  /** Base-2 logarithm of e (~1.443) */
  LOG2E: number;

  /** Base-10 logarithm of e (~0.434) */
  LOG10E: number;

  /** Ratio of a circle's circumference to its diameter (~3.14159) */
  PI: number;

  /** Square root of 0.5 (~0.707) */
  SQRT1_2: number;

  /** Square root of 2 (~1.414) */
  SQRT2: number;

  /** Returns the absolute value of a number */
  abs(x: number): number;

  /** Returns the arccosine (in radians) of a number */
  acos(x: number): number;

  /** Returns the arcsine (in radians) of a number */
  asin(x: number): number;

  /** Returns the arctangent (in radians) of a number */
  atan(x: number): number;

  /** Returns the arctangent of two numbers (y/x), with proper quadrant handling */
  atan2(y: number, x: number): number;

  /** Returns the smallest integer greater than or equal to a number */
  ceil(x: number): number;

  /** Returns the cosine (in radians) of a number */
  cos(x: number): number;

  /** Returns e raised to the power of a number */
  exp(x: number): number;

  /** Returns the largest integer less than or equal to a number */
  floor(x: number): number;

  /** Returns the natural logarithm (base e) of a number */
  log(x: number): number;

  /** Returns the largest of zero or more numbers */
  max(...values: number): number;

  /** Returns the smallest of zero or more numbers */
  min(...values: number): number;

  /** Returns base raised to the power of exponent */
  pow(x: number, y: number): number;

  /** Returns a random number between 0 (inclusive) and 1 (exclusive) */
  random(): number;

  /** Returns the value of a number rounded to the nearest integer */
  round(x: number): number;

  /** Returns the sine (in radians) of a number */
  sin(x: number): number;

  /** Returns the square root of a number */
  sqrt(x: number): number;

  /** Returns the tangent (in radians) of a number */
  tan(x: number): number;
}
