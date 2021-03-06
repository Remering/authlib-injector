package org.to2mbn.authlibinjector.internal.org.json;

/*
 * Copyright (c) 2002 JSON.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all
 * copies or substantial portions of the Software.
 *
 * The Software shall be used for Good, not Evil.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A JSONArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL object</code>.
 * <p>
 * The constructor can convert a JSON text into a Java object. The
 * <code>toString</code> method converts to JSON text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * JSON syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there is <code>,</code>
 * &nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 * or single quote, and if they do not contain leading or trailing spaces, and
 * if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , #</code> and if they do not look like numbers and if
 * they are not the reserved words <code>true</code>, <code>false</code>, or
 * <code>null</code>.</li>
 * </ul>
 *
 * @author JSON.org
 * @version 2016-05-20
 */
public class JSONArray implements Iterable<Object>, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The arrayList where the JSONArray's properties are kept.
	 */
	private final List<Object> list;

	/**
	 * Construct an empty JSONArray.
	 */
	public JSONArray() {
		list = new ArrayList<>();
	}

	/**
	 * Construct a JSONArray from a JSONTokener.
	 *
	 * @param x
	 *            A JSONTokener
	 * @throws JSONException
	 *             If there is a syntax error.
	 */
	public JSONArray(JSONTokener x) throws JSONException {
		this();
		if (x.nextClean() != '[') {
			throw x.syntaxError("A JSONArray text must start with '['");
		}
		if (x.nextClean() != ']') {
			x.back();
			for (;;) {
				if (x.nextClean() == ',') {
					x.back();
					list.add(JSONObject.NULL);
				} else {
					x.back();
					list.add(x.nextValue());
				}
				switch (x.nextClean()) {
					case ',':
						if (x.nextClean() == ']') {
							return;
						}
						x.back();
						break;
					case ']':
						return;
					default:
						throw x.syntaxError("Expected a ',' or ']'");
				}
			}
		}
	}

	/**
	 * Construct a JSONArray from a source JSON text.
	 *
	 * @param source
	 *            A string that begins with <code>[</code>&nbsp;<small>(left
	 *            bracket)</small> and ends with <code>]</code> &nbsp;
	 *            <small>(right bracket)</small>.
	 * @throws JSONException
	 *             If there is a syntax error.
	 */
	public JSONArray(String source) throws JSONException {
		this(new JSONTokener(source));
	}

	/**
	 * Construct a JSONArray from a Collection.
	 *
	 * @param collection
	 *            A Collection.
	 */
	public JSONArray(Collection<?> collection) {
		list = new ArrayList<>();
		if (collection != null) {
			for (Object o : collection) {
				list.add(JSONObject.wrap(o));
			}
		}
	}

	/**
	 * Construct a JSONArray from an array
	 *
	 * @param array
	 *            The array
	 * @throws JSONException
	 *             If not an array.
	 */
	public <T> JSONArray(T[] array) throws JSONException {
		this();
		for (T element : array) {
			this.put(JSONObject.wrap(element));
		}
	}

	@Override
	public Iterator<Object> iterator() {
		return list.iterator();
	}

	/**
	 * Get the object value associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return An object value.
	 * @throws JSONException
	 *             If there is no value for the index.
	 */
	public Object get(int index) throws JSONException {
		Object object = opt(index);
		if (object == null) {
			throw new JSONException("JSONArray[" + index + "] not found.");
		}
		return object;
	}

	/**
	 * Get the boolean value associated with an index. The string values "true"
	 * and "false" are converted to boolean.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The truth.
	 * @throws JSONException
	 *             If there is no value for the index or if the value
	 *             is not convertible to boolean.
	 */
	public boolean getBoolean(int index) throws JSONException {
		Object object = get(index);
		if (object.equals(Boolean.FALSE)
				|| (object instanceof String && ((String) object)
						.equalsIgnoreCase("false"))) {
			return false;
		} else if (object.equals(Boolean.TRUE)
				|| (object instanceof String && ((String) object)
						.equalsIgnoreCase("true"))) {
			return true;
		}
		throw new JSONException("JSONArray[" + index + "] is not a boolean.");
	}

	/**
	 * Get the double value associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JSONException
	 *             If the key is not found or if the value cannot be
	 *             converted to a number.
	 */
	public double getDouble(int index) throws JSONException {
		Object object = get(index);
		try {
			return object instanceof Number ? ((Number) object).doubleValue()
					: Double.parseDouble((String) object);
		} catch (Exception e) {
			throw new JSONException("JSONArray[" + index + "] is not a number.");
		}
	}

	/**
	 * Get the enum value associated with an index.
	 *
	 * @param clazz
	 *            The type of enum to retrieve.
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param <E>
	 *            The enum to retrieve
	 * @return The enum value at the index location
	 * @throws JSONException
	 *             if the key is not found or if the value cannot be
	 *             converted to an enum.
	 */
	public <E extends Enum<E>> E getEnum(Class<E> clazz, int index) throws JSONException {
		E val = optEnum(clazz, index);
		if (val == null) {
			// JSONException should really take a throwable argument.
			// If it did, I would re-implement this with the Enum.valueOf
			// method and place any thrown exception in the JSONException
			throw new JSONException("JSONObject[" + JSONObject.quote(Integer.toString(index))
					+ "] is not an enum of type " + JSONObject.quote(clazz.getSimpleName())
					+ ".");
		}
		return val;
	}

	/**
	 * Get the BigDecimal value associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JSONException
	 *             If the key is not found or if the value cannot be
	 *             converted to a BigDecimal.
	 */
	public BigDecimal getBigDecimal(int index) throws JSONException {
		Object object = get(index);
		try {
			return new BigDecimal(object.toString());
		} catch (Exception e) {
			throw new JSONException("JSONArray[" + index +
					"] could not convert to BigDecimal.");
		}
	}

	/**
	 * Get the BigInteger value associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JSONException
	 *             If the key is not found or if the value cannot be
	 *             converted to a BigInteger.
	 */
	public BigInteger getBigInteger(int index) throws JSONException {
		Object object = get(index);
		try {
			return new BigInteger(object.toString());
		} catch (Exception e) {
			throw new JSONException("JSONArray[" + index +
					"] could not convert to BigInteger.");
		}
	}

	/**
	 * Get the int value associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JSONException
	 *             If the key is not found or if the value is not a
	 *             number.
	 */
	public int getInt(int index) throws JSONException {
		Object object = get(index);
		try {
			return object instanceof Number ? ((Number) object).intValue()
					: Integer.parseInt((String) object);
		} catch (Exception e) {
			throw new JSONException("JSONArray[" + index + "] is not a number.");
		}
	}

	/**
	 * Get the JSONArray associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A JSONArray value.
	 * @throws JSONException
	 *             If there is no value for the index. or if the value
	 *             is not a JSONArray
	 */
	public JSONArray getJSONArray(int index) throws JSONException {
		Object object = get(index);
		if (object instanceof JSONArray) {
			return (JSONArray) object;
		}
		throw new JSONException("JSONArray[" + index + "] is not a JSONArray.");
	}

	/**
	 * Get the JSONObject associated with an index.
	 *
	 * @param index
	 *            subscript
	 * @return A JSONObject value.
	 * @throws JSONException
	 *             If there is no value for the index or if the value
	 *             is not a JSONObject
	 */
	public JSONObject getJSONObject(int index) throws JSONException {
		Object object = get(index);
		if (object instanceof JSONObject) {
			return (JSONObject) object;
		}
		throw new JSONException("JSONArray[" + index + "] is not a JSONObject.");
	}

	/**
	 * Get the long value associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JSONException
	 *             If the key is not found or if the value cannot be
	 *             converted to a number.
	 */
	public long getLong(int index) throws JSONException {
		Object object = get(index);
		try {
			return object instanceof Number ? ((Number) object).longValue()
					: Long.parseLong((String) object);
		} catch (Exception e) {
			throw new JSONException("JSONArray[" + index + "] is not a number.");
		}
	}

	/**
	 * Get the string associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A string value.
	 * @throws JSONException
	 *             If there is no string value for the index.
	 */
	public String getString(int index) throws JSONException {
		Object object = get(index);
		if (object instanceof String) {
			return (String) object;
		}
		throw new JSONException("JSONArray[" + index + "] not a string.");
	}

	/**
	 * Determine if the value is null.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return true if the value at the index is null, or if there is no value.
	 */
	public boolean isNull(int index) {
		return JSONObject.NULL.equals(opt(index));
	}

	/**
	 * Get the number of elements in the JSONArray, included nulls.
	 *
	 * @return The length (or size).
	 */
	public int length() {
		return list.size();
	}

	/**
	 * Get the optional object value associated with an index.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return An object value, or null if there is no object at that index.
	 */
	public Object opt(int index) {
		return (index < 0 || index >= length()) ? null : list
				.get(index);
	}

	/**
	 * Get the optional boolean value associated with an index. It returns the
	 * defaultValue if there is no value at that index or if it is not a Boolean
	 * or the String "true" or "false" (case insensitive).
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            A boolean default.
	 * @return The truth.
	 */
	public boolean optBoolean(int index, boolean defaultValue) {
		try {
			return getBoolean(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional double value associated with an index. The defaultValue
	 * is returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 *
	 * @param index
	 *            subscript
	 * @param defaultValue
	 *            The default value.
	 * @return The value.
	 */
	public double optDouble(int index, double defaultValue) {
		try {
			return getDouble(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional int value associated with an index. The defaultValue is
	 * returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            The default value.
	 * @return The value.
	 */
	public int optInt(int index, int defaultValue) {
		try {
			return getInt(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the enum value associated with a key.
	 *
	 * @param clazz
	 *            The type of enum to retrieve.
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param <E>
	 *            The enum to retrieve
	 * @return The enum value at the index location or null if not found
	 */
	public <E extends Enum<E>> E optEnum(Class<E> clazz, int index) {
		return this.optEnum(clazz, index, null);
	}

	/**
	 * Get the enum value associated with a key.
	 *
	 * @param clazz
	 *            The type of enum to retrieve.
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            The default in case the value is not found
	 * @param <E>
	 *            The enum to retrieve
	 * @return The enum value at the index location or defaultValue if the value
	 *         is not found or cannot be assigned to clazz
	 */
	public <E extends Enum<E>> E optEnum(Class<E> clazz, int index, E defaultValue) {
		try {
			Object val = opt(index);
			if (JSONObject.NULL.equals(val)) {
				return defaultValue;
			}
			if (clazz.isAssignableFrom(val.getClass())) {
				// we just checked it!
				@SuppressWarnings("unchecked")
				E myE = (E) val;
				return myE;
			}
			return Enum.valueOf(clazz, val.toString());
		} catch (IllegalArgumentException e) {
			return defaultValue;
		} catch (NullPointerException e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional BigInteger value associated with an index. The
	 * defaultValue is returned if there is no value for the index, or if the
	 * value is not a number and cannot be converted to a number.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            The default value.
	 * @return The value.
	 */
	public BigInteger optBigInteger(int index, BigInteger defaultValue) {
		try {
			return getBigInteger(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional BigDecimal value associated with an index. The
	 * defaultValue is returned if there is no value for the index, or if the
	 * value is not a number and cannot be converted to a number.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            The default value.
	 * @return The value.
	 */
	public BigDecimal optBigDecimal(int index, BigDecimal defaultValue) {
		try {
			return getBigDecimal(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional JSONArray associated with an index.
	 *
	 * @param index
	 *            subscript
	 * @return A JSONArray value, or null if the index has no value, or if the
	 *         value is not a JSONArray.
	 */
	public JSONArray optJSONArray(int index) {
		Object o = opt(index);
		return o instanceof JSONArray ? (JSONArray) o : null;
	}

	/**
	 * Get the optional JSONObject associated with an index. Null is returned if
	 * the key is not found, or null if the index has no value, or if the value
	 * is not a JSONObject.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A JSONObject value.
	 */
	public JSONObject optJSONObject(int index) {
		Object o = opt(index);
		return o instanceof JSONObject ? (JSONObject) o : null;
	}

	/**
	 * Get the optional long value associated with an index. The defaultValue is
	 * returned if there is no value for the index, or if the value is not a
	 * number and cannot be converted to a number.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            The default value.
	 * @return The value.
	 */
	public long optLong(int index, long defaultValue) {
		try {
			return getLong(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Get the optional string value associated with an index. It returns null if
	 * there is no value at that index. If the value is not a
	 * string and is not null, then it is coverted to a string.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @return A String value.
	 */
	public String optString(int index) {
		return this.optString(index, null);
	}

	/**
	 * Get the optional string associated with an index. The defaultValue is
	 * returned if the key is not found.
	 *
	 * @param index
	 *            The index must be between 0 and length() - 1.
	 * @param defaultValue
	 *            The default value.
	 * @return A String value.
	 */
	public String optString(int index, String defaultValue) {
		Object object = opt(index);
		return JSONObject.NULL.equals(object) ? defaultValue : object
				.toString();
	}

	/**
	 * Put a value in the JSONArray, where the value will be a JSONArray which
	 * is produced from a Collection.
	 *
	 * @param value
	 *            A Collection value.
	 * @return this.
	 */
	public JSONArray put(Collection<?> value) {
		this.put(new JSONArray(value));
		return this;
	}

	/**
	 * Put a value in the JSONArray, where the value will be a JSONObject which
	 * is produced from a Map.
	 *
	 * @param value
	 *            A Map value.
	 * @return this.
	 */
	public JSONArray put(Map<?, ?> value) {
		this.put(new JSONObject(value));
		return this;
	}

	/**
	 * Append an object value. This increases the array's length by one.
	 *
	 * @param value
	 *            An object value. The value should be a Boolean, Double,
	 *            Integer, JSONArray, JSONObject, Long, or String, or the
	 *            JSONObject.NULL object.
	 * @return this.
	 */
	public JSONArray put(Object value) {
		list.add(value);
		return this;
	}

	/**
	 * Put a value in the JSONArray, where the value will be a JSONArray which
	 * is produced from a Collection.
	 *
	 * @param index
	 *            The subscript.
	 * @param value
	 *            A Collection value.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative or if the value is not
	 *             finite.
	 */
	public JSONArray put(int index, Collection<?> value) throws JSONException {
		this.put(index, new JSONArray(value));
		return this;
	}

	/**
	 * Put a value in the JSONArray, where the value will be a JSONObject that
	 * is produced from a Map.
	 *
	 * @param index
	 *            The subscript.
	 * @param value
	 *            The Map value.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative or if the the value is an
	 *             invalid number.
	 */
	public JSONArray put(int index, Map<?, ?> value) throws JSONException {
		this.put(index, new JSONObject(value));
		return this;
	}

	/**
	 * Put or replace an object value in the JSONArray. If the index is greater
	 * than the length of the JSONArray, then null elements will be added as
	 * necessary to pad it out.
	 *
	 * @param index
	 *            The subscript.
	 * @param value
	 *            The value to put into the array. The value should be a
	 *            Boolean, Double, Integer, JSONArray, JSONObject, Long, or
	 *            String, or the JSONObject.NULL object.
	 * @return this.
	 * @throws JSONException
	 *             If the index is negative or if the the value is an
	 *             invalid number.
	 */
	public JSONArray put(int index, Object value) throws JSONException {
		JSONObject.testValidity(value);
		if (index < 0) {
			throw new JSONException("JSONArray[" + index + "] not found.");
		}
		if (index < length()) {
			list.set(index, value);
		} else {
			while (index != length()) {
				this.put(JSONObject.NULL);
			}
			this.put(value);
		}
		return this;
	}

	/**
	 * Remove an index and close the hole.
	 *
	 * @param index
	 *            The index of the element to be removed.
	 * @return The value that was associated with the index, or null if there
	 *         was no value.
	 */
	public Object remove(int index) {
		return index >= 0 && index < length()
				? list.remove(index)
				: null;
	}

	/**
	 * Make a JSON text of this JSONArray. For compactness, no unnecessary
	 * whitespace is added. If it is not possible to produce a syntactically
	 * correct JSON text then null will be returned instead. This could occur if
	 * the array contains an invalid number.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @return a printable, displayable, transmittable representation of the
	 *         array.
	 */
	@Override
	public String toString() {
		try {
			return this.toString(0);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Make a prettyprinted JSON text of this JSONArray. Warning: This method
	 * assumes that the data structure is acyclical.
	 *
	 * @param indentFactor
	 *            The number of spaces to add to each level of
	 *            indentation.
	 * @return a printable, displayable, transmittable representation of the
	 *         object, beginning with <code>[</code>&nbsp;<small>(left
	 *         bracket)</small> and ending with <code>]</code> &nbsp;
	 *         <small>(right bracket)</small>.
	 * @throws JSONException
	 *             If the data structure is not acyclical
	 */
	public String toString(int indentFactor) throws JSONException {
		StringWriter sw = new StringWriter();
		synchronized (sw.getBuffer()) {
			return this.write(sw, indentFactor, 0).toString();
		}
	}

	/**
	 * Write the contents of the JSONArray as JSON text to a writer. For
	 * compactness, no whitespace is added.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @param writer
	 *            The writer
	 * @return The writer.
	 * @throws JSONException
	 *             If the data structure is not acyclical or an I/O
	 *             error occurs
	 */
	public Writer write(Writer writer) throws JSONException {
		return this.write(writer, 0, 0);
	}

	/**
	 * Write the contents of the JSONArray as JSON text to a writer. For
	 * compactness, no whitespace is added.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @param writer
	 *            Writes the serialized JSON
	 * @param indentFactor
	 *            The number of spaces to add to each level of
	 *            indentation.
	 * @param indent
	 *            The indention of the top level.
	 * @return The writer.
	 * @throws JSONException
	 *             If the data structure is not acyclical or an I/O
	 *             error occurs
	 */
	public Writer write(Writer writer, int indentFactor, int indent)
			throws JSONException {
		try {
			boolean commanate = false;
			int length = length();
			writer.write('[');

			if (length == 1) {
				JSONObject.writeValue(writer, list.get(0),
						indentFactor, indent);
			} else if (length != 0) {
				final int newindent = indent + indentFactor;

				for (int i = 0; i < length; i += 1) {
					if (commanate) {
						writer.write(',');
					}
					if (indentFactor > 0) {
						writer.write('\n');
					}
					JSONObject.indent(writer, newindent);
					JSONObject.writeValue(writer, list.get(i),
							indentFactor, newindent);
					commanate = true;
				}
				if (indentFactor > 0) {
					writer.write('\n');
				}
				JSONObject.indent(writer, indent);
			}
			writer.write(']');
			return writer;
		} catch (IOException e) {
			throw new JSONException(e);
		}
	}

	/**
	 * Returns a java.util.List containing all of the elements in this array. If
	 * an element in the array is a JSONArray or JSONObject it will also be
	 * converted.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 *
	 * @return a java.util.List containing the elements of this array
	 */
	public List<Object> toList() {
		List<Object> results = new ArrayList<>(list.size());
		for (Object element : list) {
			if (element == null || JSONObject.NULL.equals(element)) {
				results.add(null);
			} else if (element instanceof JSONArray) {
				results.add(((JSONArray) element).toList());
			} else if (element instanceof JSONObject) {
				results.add(((JSONObject) element).toMap());
			} else {
				results.add(element);
			}
		}
		return results;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof JSONArray) {
			JSONArray another = (JSONArray) obj;
			return list.equals(another.list);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return list.hashCode();
	}
}
