/**
 * ECMAScript 5.1 - Object type, the base type for all JavaScript objects.
 * Use static methods like Object.keys() and Object.create() to manipulate object structure.
 *
 * ## How to Use
 *
 * ### Creating Objects
 * ```js
 * var obj = {};                              // Empty object literal
 * var person = { name: "Steve", age: 20 };  // Object with properties
 * var child = Object.create(person);         // Create with prototype
 * ```
 *
 * ### Inspecting Objects
 * ```js
 * var obj = { a: 1, b: 2, c: 3 };
 * Object.keys(obj);                         // ["a", "b", "c"]
 * Object.getOwnPropertyNames(obj);          // ["a", "b", "c"] (includes non-enumerable)
 * obj.hasOwnProperty("a");                  // true
 * obj.hasOwnProperty("toString");           // false (inherited, not own)
 * ```
 *
 * ### Freezing & Sealing
 * ```js
 * var config = { debug: false };
 * Object.freeze(config);                    // Cannot modify, add, or delete properties
 * config.debug = true;                      // Silently fails (or throws in strict mode)
 * Object.isFrozen(config);                  // true
 *
 * var settings = { volume: 50 };
 * Object.seal(settings);                    // Can modify values, but cannot add/delete
 * settings.volume = 75;                     // ✅ Works
 * settings.newProp = true;                  // Silently fails
 * ```
 *
 * ### Property Descriptors
 * ```js
 * var obj = {};
 * Object.defineProperty(obj, "id", {
 *   value: 42,
 *   writable: false,                        // Cannot be changed
 *   enumerable: true                        // Shows up in for-in / Object.keys
 * });
 * ```
 *
 * ### ❌ Common Mistakes
 * ```js
 * Object.keys();                            // TypeError — needs an argument
 * obj.keys();                               // TypeError — keys is a static method
 * Object.hasOwnProperty("a");               // Wrong — instance method on the object
 * ```
 */
export interface Object {
  /** Creates a new object with the specified prototype and optional property descriptors */
  create(proto: any, propertiesObject: any): any;

  /** Defines or modifies multiple properties on an object and returns the modified object */
  defineProperties(obj: any, props: any): any;

  /** Defines a new property on an object or modifies an existing property and returns the object */
  defineProperty(obj: any, prop: string, descriptor: any): any;

  /** Prevents all modifications to an object (cannot add, modify, or delete properties) */
  freeze(obj: any): any;

  /** Returns a property descriptor for a named property on an object, or undefined if not found */
  getOwnPropertyDescriptor(obj: any, prop: string): any;

  /** Returns an array of all property names (both enumerable and non-enumerable) on an object */
  getOwnPropertyNames(obj: any): string[];

  /** Returns the prototype object of the specified object */
  getPrototypeOf(obj: any): any;

  /** Determines whether an object is extensible (can have new properties added to it) */
  isExtensible(obj: any): boolean;

  /** Determines whether an object has been frozen (is immutable) */
  isFrozen(obj: any): boolean;

  /** Determines whether an object is sealed (cannot add or delete properties) */
  isSealed(obj: any): boolean;

  /** Returns an array of all enumerable property names on an object */
  keys(obj: any): string[];

  /** Prevents new properties from being added to an object but allows modification of existing ones */
  preventExtensions(obj: any): any;

  /** Prevents all modifications to an object except for reading and writing property values */
  seal(obj: any): any;

  /** Determines whether an object has a property with the specified name (direct property, not inherited) */
  hasOwnProperty(prop: string): boolean;

  /** Determines whether an object is in the prototype chain of another object */
  isPrototypeOf(obj: any): boolean;

  /** Determines whether an object property is enumerable (would be included in a for-in loop) */
  propertyIsEnumerable(prop: string): boolean;

  /** Returns a localized string representation of the object (format depends on locale) */
  toLocaleString(): string;

  /** Returns a string representation of the object, usually "[object Object]" unless overridden */
  toString(): string;

  /** Returns the primitive value of the object (usually the object itself unless overridden) */
  valueOf(): any;
}
