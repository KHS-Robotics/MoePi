package com.moe365.moepi;

import java.util.HashMap;

/**
 * A map of the command line arguments to their given values, with features such
 * as conversion between primitive types
 */
public class ParsedCommandLineArguments {
    protected final HashMap<String, String> data;
    
    protected ParsedCommandLineArguments(HashMap<String, String> data) {
        this.data = data;
    }
    
    /**
     * Returns whether the specified flag or option has been set. Does not
     * support aliases such that if <code>foo</code> is an alias for
     * <code>bar</code>, and <code>foo</code> is set in the arguments,
     * <code>isFlagSet("bar")==true</code>, while
     * <code>isFlagSet("foo")==false</code>
     * 
     * @param name
     *            Name of the flag or option to detect
     * @return Whether the specified flag or an option has been set
     */
    public boolean isFlagSet(String name) {
        return data.containsKey(name);
    }
    
    /**
     * Get the value of the given option. Returns null if the queried name is a flag.
     * @param name The name of the option
     * @return The value of the option, or null if the option is not set
     */
    public String get(String name) {
        return data.get(name);
    }
    
    /**
     * Get the value of an option, or 
     * @param name
     * @param def
     * @return
     */
    public String getOrDefault(String name, String def) {
        if (isFlagSet(name))
            return get(name);
        return def;
    }
    /**
     * Get the value for the given key if set, or the default value if not set,
     * or the value cannot be parsed as an integer.
     * @param name The name of the option
     * @param def A default value if the option is not set, or is not an integer
     * @return The integer value of the given key, or the default value
     */
    public int getOrDefault(String name, int def) {
        if (isFlagSet(name)) {
            try {
                return Integer.parseInt(get(name));
            } catch (NumberFormatException e){
                //It's ok. Fallback to default value
            }
        }
        return def;
    }
    
    /**
     * Combine the two ParsedCommandLineArguments objects. Is similar to combining
     * maps via {@link Map#putAll(Map)}. Not sure why one would want this feature, but
     * here it is.
     * @param t Other values to append
     * @return self
     */
    public ParsedCommandLineArguments add(ParsedCommandLineArguments t) {
        this.data.putAll(t.data);
        return this;
    }
    
}