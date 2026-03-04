/**
 * ECMAScript 5.1 - Date type for working with dates and times.
 * Create with `new Date()` or use static methods like Date.now() to get current timestamp.
 *
 * ## How to Use
 *
 * ### Creating Dates
 * ```js
 * var now = new Date();                    // Current date & time
 * var epoch = new Date(0);                 // Jan 1, 1970
 * var specific = new Date(2024, 0, 15);   // Jan 15, 2024 (months are 0-based!)
 * var full = new Date(2024, 5, 15, 10, 30, 0);  // Jun 15, 2024 10:30:00
 * var fromStr = new Date("2024-01-15");   // Parse from ISO string
 * ```
 *
 * ### Static Methods (no `new` needed)
 * ```js
 * var timestamp = Date.now();              // Current time in milliseconds
 * var parsed = Date.parse("2024-01-15");  // Parse string to timestamp
 * var utc = Date.UTC(2024, 0, 15);        // UTC timestamp for Jan 15, 2024
 * ```
 *
 * ### Common Mistakes
 * ```js
 * Date.now;                                // Returns the function, not the timestamp!
 * Date.getMinutes();                       // TypeError — instance method, not static
 * new Date(2024, 1, 1);                    // Feb 1, not Jan 1 — months are 0-based!
 * ```
 */
export interface Date {
  /** Returns the current time as the number of milliseconds since 1970-01-01 00:00:00 UTC */
  now(): number;

  /** Parses a date string and returns the number of milliseconds since 1970-01-01 00:00:00 UTC */
  parse(s: string): number;

  /** Returns the number of milliseconds since 1970-01-01 00:00:00 UTC for the given UTC date/time components */
  UTC(year: number, month: number, date: number, hours: number, minutes: number, seconds: number, ms: number): number;

  /** Returns the day of the month (1-31) in local time */
  getDate(): number;

  /** Returns the day of the week (0=Sunday, 6=Saturday) in local time */
  getDay(): number;

  /** Returns the year in local time */
  getFullYear(): number;

  /** Returns the hour (0-23) in local time */
  getHours(): number;

  /** Returns the milliseconds (0-999) in local time */
  getMilliseconds(): number;

  /** Returns the minutes (0-59) in local time */
  getMinutes(): number;

  /** Returns the month (0=January, 11=December) in local time */
  getMonth(): number;

  /** Returns the seconds (0-59) in local time */
  getSeconds(): number;

  /** Returns the number of milliseconds since 1970-01-01 00:00:00 UTC */
  getTime(): number;

  /** Returns the time zone offset in minutes between local time and UTC */
  getTimezoneOffset(): number;

  /** Returns the day of the month (1-31) in UTC */
  getUTCDate(): number;

  /** Returns the day of the week (0=Sunday, 6=Saturday) in UTC */
  getUTCDay(): number;

  /** Returns the year in UTC */
  getUTCFullYear(): number;

  /** Returns the hour (0-23) in UTC */
  getUTCHours(): number;

  /** Returns the milliseconds (0-999) in UTC */
  getUTCMilliseconds(): number;

  /** Returns the minutes (0-59) in UTC */
  getUTCMinutes(): number;

  /** Returns the month (0=January, 11=December) in UTC */
  getUTCMonth(): number;

  /** Returns the seconds (0-59) in UTC */
  getUTCSeconds(): number;

  /** Sets the day of the month and returns the new timestamp */
  setDate(date: number): number;

  /** Sets the year, and optionally month and date, then returns the new timestamp */
  setFullYear(year: number, month: number, date: number): number;

  /** Sets the hour, minutes, seconds, and milliseconds, then returns the new timestamp */
  setHours(hour: number, min: number, sec: number, ms: number): number;

  /** Sets the milliseconds and returns the new timestamp */
  setMilliseconds(ms: number): number;

  /** Sets the minutes, seconds, and milliseconds, then returns the new timestamp */
  setMinutes(min: number, sec: number, ms: number): number;

  /** Sets the month, and optionally date, then returns the new timestamp */
  setMonth(month: number, date: number): number;

  /** Sets the seconds and milliseconds, then returns the new timestamp */
  setSeconds(sec: number, ms: number): number;

  /** Sets the timestamp (milliseconds since epoch) and returns it */
  setTime(time: number): number;

  /** Sets the day of the month (UTC) and returns the new timestamp */
  setUTCDate(date: number): number;

  /** Sets the year, month, and date (UTC), then returns the new timestamp */
  setUTCFullYear(year: number, month: number, date: number): number;

  /** Sets the hour, minutes, seconds, and milliseconds (UTC), then returns the new timestamp */
  setUTCHours(hour: number, min: number, sec: number, ms: number): number;

  /** Sets the milliseconds (UTC) and returns the new timestamp */
  setUTCMilliseconds(ms: number): number;

  /** Sets the minutes, seconds, and milliseconds (UTC), then returns the new timestamp */
  setUTCMinutes(min: number, sec: number, ms: number): number;

  /** Sets the month and date (UTC), then returns the new timestamp */
  setUTCMonth(month: number, date: number): number;

  /** Sets the seconds and milliseconds (UTC), then returns the new timestamp */
  setUTCSeconds(sec: number, ms: number): number;

  /** Returns a string representation of the date portion in local time (e.g., "Mon Jan 01 2024") */
  toDateString(): string;

  /** Returns the date as an ISO 8601 string (e.g., "2024-01-01T00:00:00.000Z") */
  toISOString(): string;

  /** Returns a JSON representation of the date (typically an ISO 8601 string) */
  toJSON(key: any): string;

  /** Returns a localized string for the date portion (format depends on locale) */
  toLocaleDateString(): string;

  /** Returns a localized string for the entire date and time (format depends on locale) */
  toLocaleString(): string;

  /** Returns a localized string for the time portion (format depends on locale) */
  toLocaleTimeString(): string;

  /** Returns a string representation of the date and time in local time */
  toString(): string;

  /** Returns a string representation of the time portion in local time (e.g., "00:00:00 GMT+0000") */
  toTimeString(): string;

  /** Returns a string representation of the date and time in UTC (e.g., "Mon, 01 Jan 2024 00:00:00 GMT") */
  toUTCString(): string;

  /** Returns the primitive value of the date object (milliseconds since 1970-01-01 00:00:00 UTC) */
  valueOf(): number;
}
