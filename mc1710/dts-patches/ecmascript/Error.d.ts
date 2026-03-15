/**
 * ECMAScript 5.1 - Error base type for runtime errors.
 * Throw Error objects to indicate failure conditions in your code.
 *
 * ## How to Use
 *
 * ### Creating & Throwing Errors
 * ```js
 * throw new Error("Something went wrong");
 * throw new TypeError("Expected a string");
 * throw new RangeError("Value out of range");
 * ```
 *
 * ### Catching Errors
 * ```js
 * try {
 *   var result = riskyOperation();
 * } catch (e) {
 *   log(e.name);                           // "Error", "TypeError", etc.
 *   log(e.message);                        // "Something went wrong"
 *   log(e.toString());                     // "Error: Something went wrong"
 * }
 * ```
 *
 * ### Custom Error Messages
 * ```js
 * function divide(a, b) {
 *   if (b === 0) {
 *     throw new Error("Cannot divide by zero");
 *   }
 *   return a / b;
 * }
 * ```
 *
 * ### ❌ Common Mistakes
 * ```js
 * throw "Something went wrong";             // Works but no .name or .message properties
 * Error("oops");                            // Creates error but doesn't throw it!
 * throw Error("oops");                      // ✅ Works (with or without `new`)
 * ```
 */
export interface Error {
  /** The name of the error type (e.g., "Error", "TypeError", "RangeError") */
  name: string;

  /** A human-readable description of what went wrong */
  message: string;

  /** Returns a string representation of the error (typically "name: message") */
  toString(): string;
}

/**
 * EvalError - Thrown when eval() function is misused (rarely used in modern JavaScript)
 */
export interface EvalError extends Error {}

/**
 * RangeError - Thrown when a numeric value is out of the acceptable range (e.g., array length < 0)
 */
export interface RangeError extends Error {}

/**
 * ReferenceError - Thrown when trying to reference a variable that does not exist or is not in scope
 */
export interface ReferenceError extends Error {}

/**
 * SyntaxError - Thrown when parsing invalid syntax (usually caught during compile/parse, not runtime)
 */
export interface SyntaxError extends Error {}

/**
 * TypeError - Thrown when an operation is performed on a value of the wrong type (e.g., calling a non-function)
 */
export interface TypeError extends Error {}

/**
 * URIError - Thrown by URI encoding/decoding functions when given invalid characters or sequences
 */
export interface URIError extends Error {}
