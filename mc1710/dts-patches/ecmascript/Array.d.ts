/**
 * ECMAScript 5.1 - Array type for working with ordered collections of elements.
 * Supports static methods like Array.isArray() and instance methods like push(), map(), filter().
 *
 * ## How to Use
 *
 * ### Creating Arrays
 * ```js
 * var empty = [];                           // Empty array
 * var nums = [1, 2, 3];                    // Array with values
 * var mixed = ["hello", 42, true];         // Mixed types
 * var nested = [[1, 2], [3, 4]];           // Nested arrays
 * ```
 *
 * ### Static Methods
 * ```js
 * Array.isArray([1, 2, 3]);                // true
 * Array.isArray("hello");                  // false
 * Array.isArray({length: 3});              // false
 * ```
 *
 * ### Adding & Removing Elements
 * ```js
 * var arr = [1, 2, 3];
 * arr.push(4);                             // [1, 2, 3, 4] — add to end
 * arr.pop();                               // [1, 2, 3]   — remove from end
 * arr.unshift(0);                          // [0, 1, 2, 3] — add to start
 * arr.shift();                             // [1, 2, 3]   — remove from start
 * arr.splice(1, 1, 99);                    // [1, 99, 3]  — replace at index 1
 * ```
 *
 * ### Searching & Iterating
 * ```js
 * var arr = [10, 20, 30, 20];
 * arr.indexOf(20);                         // 1  (first match)
 * arr.lastIndexOf(20);                     // 3  (last match)
 * arr.forEach(function(val) { log(val); });
 * var doubled = arr.map(function(v) { return v * 2; });   // [20, 40, 60, 40]
 * var big = arr.filter(function(v) { return v > 15; });   // [20, 30, 20]
 * ```
 *
 * ### Reducing & Testing
 * ```js
 * var sum = [1, 2, 3].reduce(function(acc, v) { return acc + v; }, 0);  // 6
 * var allPositive = [1, 2, 3].every(function(v) { return v > 0; });     // true
 * var hasNeg = [1, -2, 3].some(function(v) { return v < 0; });          // true
 * ```
 *
 * ### ❌ Common Mistakes
 * ```js
 * Array.push(1);                           // TypeError — push is an instance method
 * var a = [3,1,2]; a.sort();               // Sorts as strings! Use compareFn for numbers
 * a.sort(function(a, b) { return a - b; }); // Correct numeric sort: [1, 2, 3]
 * ```
 */
export interface Array<T> {
  /** Returns true if the given object is an array, false otherwise */
  static isArray(arg: any): boolean;

  /** The number of elements in the array */
  length: number;

  /** Combines two or more arrays and returns a new array without modifying the original */
  concat(...items: T[]): T[];

  /** Tests whether all elements in the array pass the provided test function */
  every(callback: Java.java.util.function.Function<T, boolean>): boolean;

  /** Creates a new array with all elements that pass the test implemented by the provided function */
  filter(callback: Java.java.util.function.Function<T, boolean>): T[];

  /** Executes a provided function once for each array element. Returns undefined */
  forEach(callback: Java.java.util.function.Consumer<T>): void;

  /** Returns the first index at which an element is found, or -1 if not present */
  indexOf(searchElement: T): number;
  indexOf(searchElement: T, fromIndex: number): number;

  /** Joins all elements of an array into a string separated by the given separator */
  join(separator: string): string;

  /** Returns the last index at which an element is found, or -1 if not present */
  lastIndexOf(searchElement: T): number;
  lastIndexOf(searchElement: T, fromIndex: number): number;

  /** Creates a new array with the results of calling a function on every element */
  map(callback: Java.java.util.function.Function<T, any>): any[];

  /** Removes and returns the last element from the array, modifying the original array */
  pop(): T;

  /** Adds one or more elements to the end of an array and returns the new length */
  push(...items: T[]): number;

  /** Applies a function against an accumulator and each element to reduce the array to a single value */
  reduce(callback: Java.java.util.function.BiFunction<any, T, any>): any;
  reduce(callback: Java.java.util.function.BiFunction<any, T, any>, initialValue: any): any;

  /** Same as reduce() but processes the array from right to left */
  reduceRight(callback: Java.java.util.function.BiFunction<any, T, any>): any;
  reduceRight(callback: Java.java.util.function.BiFunction<any, T, any>, initialValue: any): any;

  /** Reverses an array in place. The first element becomes the last, and vice versa */
  reverse(): T[];

  /** Removes and returns the first element from the array, modifying the original array */
  shift(): T;

  /** Returns a shallow copy of a portion of an array as a new array object */
  slice(start: number, end: number): T[];

  /** Tests whether at least one element in the array passes the test implemented by the provided function */
  some(callback: Java.java.util.function.Function<T, boolean>): boolean;

  /** Sorts the elements of an array in place and returns the sorted array */
  sort(compareFn: Java.java.util.function.BiFunction<T, T, number>): T[];

  /** Changes the contents of an array by removing or replacing existing elements and/or adding new ones */
  splice(start: number, deleteCount: number, ...items: T[]): T[];

  /** Returns a string representation of the array */
  toString(): string;

  /** Adds one or more elements to the beginning of an array and returns the new length */
  unshift(...items: T[]): number;
}
