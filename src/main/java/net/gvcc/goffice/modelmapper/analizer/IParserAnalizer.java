package net.gvcc.goffice.modelmapper.analizer;

/**
 * This interface is used by the {@link net.gvcc.goffice.modelmapper.ModelMapperHelper} to evaluate a datamap that contains the keys and values to use to replace placeholders in a string template.
 * <p>
 * Example:
 * <p>
 * 
 * <pre>
 * map:      { "myKey1" : "my value 1", "myKey2" : "my value 2" }
 * template: "This is a text that contains placeholders like this: value1='{myKey1}' and value2='{myKey2}'"
 * result:   "This is a text that contains placeholders like this: value1='my value 1' and value2='my value 2'"
 * </pre>
 * 
 * <p>
 * You can use the method <code>accept()</code> to skip values.
 * <p>
 * Example:
 * 
 * <pre>
 * boolean accept(String key, Object value) {
 * 	return !"myKey2".equalsIgnoreCase(key); // parse all values of map but the key 'myKey2'
 * }
 * 
 * map:      { "myKey1" : "my value 1", "myKey2" : "my value 2" }
 * template: "This is a text that contains placeholders like this: value1='{myKey1}' and value2='{myKey2}'"
 * result:   "This is a text that contains placeholders like this: value1='my value 1' and value2='{myKey2}'"
 * </pre>
 * 
 * Or you can change the data:
 * 
 * <pre>
 * public String key(String key) {
 * 	return "myKey1".equalsIgnoreCase(key) ? "renamedKey" : key; // changing the key to match the placeholder
 * }
 * 
 * public Object value(Object value) {
 * 	return null == value ? "[NO VALUE HAS BEEN SPECIFIED]" : value; // changing the value
 * }

 * map:      { "myKey1" : "my value 1", "myKey2" : "my value 2" }
 * template: "This is a text that contains placeholders like this: changedValue='{renamedKey}' and value2='{my value 2}'"
 * result:   "This is a text that contains placeholders like this: changedValue='[NO VALUE HAS BEEN SPECIFIED]' and value2='my value 2'"
 * </pre>
 * 
 * <p>
 * The <code>IParserAnalizer</code> class
 * </p>
 * <p>
 * Data: Jul 24, 2023
 * </p>
 * 
 * @author Renzo Poli
 */
public interface IParserAnalizer {
	/**
	 * Used to accept or to skip the key-pair data, then the parser uses or avoids to use this data when parsing a template string
	 * 
	 * @param key
	 *            The key that identifies the data to elaborate by the analyzer.
	 * @param value
	 *            The value to elaborate by the analyzer.
	 * @return true if the data needs to elaborate, false otherwise
	 */
	boolean accept(String key, Object value);

	/**
	 * Used to normalize the key of the map
	 * 
	 * @param key
	 *            The value of the placeholder to search for into the text template.
	 *            <p>
	 *            Example:
	 * 
	 *            <pre>
	 *            public String key(String key) {
	 * 	            return key.toLowerCase();
	 *            }
	 *            
	 *            map:      { "PlaceHolder" : "text" }
	 *            template: "This is a {placeholder}";
	 *            result:   "This is a text";
	 *            </pre>
	 * 
	 * @return The key itself or a new key, depending of your code logic
	 */
	String key(String key);

	/**
	 * 
	 * @param value
	 *            The value used to replace the placeholder identified by the key.
	 *            <p>
	 *            Example:
	 * 
	 *            <pre>
	 *            public Object value(Object value) {
	 * 	            return value == null ? "" : value.toString().toUpperCase();
	 *            }
	 *            
	 *            map:      { "placeholder" : "text" }
	 *            template: "This is a {placeholder}";
	 *            result:   "This is a TEXT";
	 *            </pre>
	 * 
	 * @return The value to use to replace the placeholder in the text template
	 */
	Object value(Object value);
}
