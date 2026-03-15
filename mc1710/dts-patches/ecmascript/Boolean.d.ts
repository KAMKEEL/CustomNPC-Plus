/**
 * ECMAScript 5.1 - Boolean type for true/false values.
 * Booleans are the result of comparison and logical operations.
 *
 * ## How to Use
 *
 * ### Creating Booleans
 * ```js
 * var yes = true;                           // Boolean literal
 * var no = false;                           // Boolean literal
 * var fromExpr = (5 > 3);                   // true (comparison result)
 * ```
 *
 * ### Boolean Methods
 * ```js
 * var b = true;
 * b.toString();                             // "true"
 * b.valueOf();                              // true (primitive value)
 * ```
 *
 * ### Truthy & Falsy Values
 * ```js
 * // These are FALSY (evaluate to false):
 * // false, 0, "", null, undefined, NaN
 *
 * // These are TRUTHY (evaluate to true):
 * // true, any non-zero number, any non-empty string, objects, arrays
 *
 */
export interface Boolean {
  /** Returns the string "true" or "false" */
  toString(): string;

  /** Returns the primitive boolean value */
  valueOf(): boolean;
}
