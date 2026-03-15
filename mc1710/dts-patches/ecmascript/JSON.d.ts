/**
 * ECMAScript 5.1 - JSON built-in object for parsing and serializing JSON data.
 *
 * ## How to Use
 *
 * JSON is a **static object** — use its methods directly, never with `new`.
 *
 * ### Parsing JSON Strings to Objects
 * ```js
 * var obj = JSON.parse('{"name": "Steve", "level": 5}');
 * log(obj.name);                            // "Steve"
 * log(obj.level);                           // 5
 *
 * var arr = JSON.parse('[1, 2, 3]');
 * log(arr[0]);                              // 1
 * ```
 *
 * ### Converting Objects to JSON Strings
 * ```js
 * var data = { name: "Steve", hp: 100 };
 * var json = JSON.stringify(data);           // '{"name":"Steve","hp":100}'
 *
 * // Pretty-printed with 2-space indent:
 * var pretty = JSON.stringify(data, null, 2);
 * // '{
 * //   "name": "Steve",
 * //   "hp": 100
 * // }'
 * ```
 *
 * ### Storing & Loading Data
 * ```js
 * // Save to NPC stored data:
 * var inventory = { sword: 1, potion: 5 };
 * npc.getStoredData().put("inv", JSON.stringify(inventory));
 *
 * // Load from NPC stored data:
 * var raw = npc.getStoredData().get("inv");
 * var loaded = JSON.parse(raw);
 * log(loaded.potion);                       // 5
 * ```
 *
 * ### ❌ Common Mistakes
 * ```js
 * new JSON();                               // TypeError — JSON is not a constructor
 * JSON.parse('{name: "Steve"}');            // SyntaxError — keys must be quoted
 * JSON.parse("{'name': 'Steve'}");          // SyntaxError — must use double quotes
 * JSON.parse('{"name": "Steve"}');          // ✅ Correct — double-quoted keys & values
 * ```
 */
export interface JSON {
  /**
   * Parses a JSON string and returns the parsed value (object, array, string, number, boolean, or null).
   * @param text - A valid JSON string to parse
   * @returns The parsed JavaScript value
   */
  static parse(text: string): any;

  /**
   * Parses a JSON string with a reviver function that transforms each parsed key-value pair.
   * @param text - A valid JSON string to parse
   * @param reviver - Function called for each key-value pair; receives the key and parsed value, returns the transformed value
   * @returns The parsed JavaScript value after applying the reviver
   */
  static parse(text: string, reviver: Java.java.util.function.BiFunction<string, any, any>): any;

  /**
   * Converts a JavaScript value to a JSON string.
   * @param value - The value to serialize (object, array, string, number, boolean, or null)
   * @returns A JSON-formatted string representation of the value
   */
  static stringify(value: any): string;

  /**
   * Converts a JavaScript value to a JSON string, using a replacer function to filter or transform properties.
   * @param value - The value to serialize
   * @param replacer - Function called for each key-value pair; receives the key and value, returns the value to include (or undefined to omit)
   * @param space - Optional indentation: a number of spaces (up to 10), or a string prefix per level
   * @returns A JSON-formatted string representation of the value
   */
  static stringify(value: any, replacer: Java.java.util.function.BiFunction<string, any, any>, space?: any): string;

  /**
   * Converts a JavaScript value to a JSON string, including only the specified properties.
   * @param value - The value to serialize
   * @param replacer - Array of property names to include in the output
   * @param space - Optional indentation: a number of spaces (up to 10), or a string prefix per level
   * @returns A JSON-formatted string representation of the value
   */
  static stringify(value: any, replacer: string[], space?: any): string;

  /**
   * Converts a JavaScript value to a JSON string with optional pretty-printing.
   * @param value - The value to serialize
   * @param replacer - Pass null to include all properties
   * @param space - Indentation: a number of spaces (up to 10), or a string prefix per level
   * @returns A JSON-formatted string representation of the value
   */
  static stringify(value: any, replacer: null, space: any): string;
}
