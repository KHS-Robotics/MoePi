package com.moe365.moepi;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class to parse a command line arguments passed to the jar.
 * @author mailmindlin
 */
public class CommandLineParser implements Serializable, Function<String[], ParsedCommandLineArguments> {
	private static final long serialVersionUID = 4501136312997123150L;
	/**
	 * The message object to display how to use the command. Currently not implemented.
	 */
	protected CommandLineUsage usage = new CommandLineUsage();
	/**
	 * The stored option (i.e., flag) signatures
	 */
	protected HashMap<String, CommandLineToken> options;
	/**
	 * A map generated of all of the aliases and their mappings, for faster lookup
	 */
	protected transient HashMap<String, Set<String>> aliases;
	
	/**
	 * Create a new builder for a command line parser with options.
	 * @return
	 */
	public static Builder builder() {
		return new Builder();
	}
	
	/**
	 * Constructor for deserialization
	 */
	protected CommandLineParser() {
		
	}
	
	protected CommandLineParser(HashMap<String, CommandLineToken> options) {
		this.options = options;
	}
	
	public CommandLineParser(Builder builder) {
		this.options = new HashMap<>(builder.options);
	}

	/**
	 * Build a help string with all the aliases and stuff
	 * @return a help string, printable 
	 */
	public String getHelpString() {
		StringBuilder result = new StringBuilder();
		//add usage
		result.append("Usage: ").append(usage).append('\n');
		
		for (Map.Entry<String, CommandLineToken> entry : this.options.entrySet().parallelStream().sorted((a,b)->(a.getKey().compareTo(b.getKey()))).collect(Collectors.toList())) {
			CommandLineToken token = entry.getValue();
			if (token == null) {
				System.err.println("Null under " + entry.getKey());
				continue;
			}
			if (token.getType() == CommandLineTokenType.ALIAS)
				continue;
			Set<String> aliases = getAliasesFor(entry.getKey());
			if (token.getType() == CommandLineTokenType.KV_PAIR) {
				CommandLineKVPair kvToken = (CommandLineKVPair) token;
				for (String alias : aliases)
					result.append("  ").append(alias)
						.append(" [").append(kvToken.getFieldName()).append("]\n");
				result.append("  ").append(entry.getKey()).append(" [").append(kvToken.getFieldName()).append("]\n");
			} else {
				for (String alias : aliases)
					result.append("  ").append(alias).append('\n');
				result.append("  ").append(entry.getKey()).append('\n');
			}
			result.append("    ")
				.append(token.getDescription().replace("\n", "\n    "))
				.append('\n');
		}
		
		return result.toString();
	}
	
	/**
	 * Apply to the argument array
	 * @param args
	 * @return
	 */
	@Override
	public ParsedCommandLineArguments apply(String[] args) {
		HashMap<String, String> data = new HashMap<>();
		for (int i = 0; i < args.length; i++) {
			CommandLineToken token = this.options.get(args[i]);
			if (token == null) {
				System.err.println("Unknown token: " + args[i]);
				data.putIfAbsent(args[i], "");
				continue;
			}
			
			while (token.getType() == CommandLineTokenType.ALIAS)//TODO fix infinite loops
				token = options.get(((CommandLineAlias)token).getTarget());
			
			if (token.getType() == CommandLineTokenType.FLAG)
				data.put(token.getName(), "true");
			if (token.getType() == CommandLineTokenType.KV_PAIR)
				data.put(token.getName(), args[++i]);
		}
		return new ParsedCommandLineArguments(data);
	}
	
	/**
	 * Get the set of aliases that are mapped to a given name.
	 * Mostly used for building the help string
	 * @param name Name (of real command) to get aliases for
	 * @return Set of aliases for command, or empty set if invalid command or no aliases exist
	 */
	@SuppressWarnings("unchecked")
	public Set<String> getAliasesFor(String name) {
		if (this.aliases == null) {
			//build alias map
			synchronized (this) {
				//Check again once lock is acquired
				if (this.aliases == null) {
					//Build alias map
					HashMap<String, Set<String>> tmp = new HashMap<>();
					for (CommandLineToken token : options.values()) {
						if (token == null || token.getType() != CommandLineTokenType.ALIAS)
							continue;
						CommandLineAlias alias = (CommandLineAlias) token; 
						tmp.computeIfAbsent(alias.getTarget(), x->new HashSet<String>()).add(alias.getName());
					}
					this.aliases = tmp;
				}
			}
		}
		return this.aliases.getOrDefault(name, Collections.EMPTY_SET);
	}

	protected static CommandLineParser build() {
		CommandLineParser parser = CommandLineParser.builder()
			// Help
			.addFlag("--help", "Displays the help message and exits")
			.alias("-h", "--help")
			.alias("-?", "--help")
			// Version
			.addFlag("--version", "Print the version string and exits.")
			.alias("-v", "--version")
			// Verbosity
			.addFlag("--verbose", "Enables verbosity (prints image processor results).")
			// Testing
			.addKvPair("--test", "target", "Run test by name. Tests include 'controls', 'client', 'processing' and 'sse'. For 'processing' see --test-images.")
			.addKvPair("--test-images", "images", "Directory of test images to use when running --test processing.")
			// Camera options
			.addKvPair("--camera", "device", "Specify the camera device file to use. Default /dev/video0.")
			.alias("-c", "--camera")
			.addKvPair("--width", "px", "Set the width of image to capture/broadcast")
			.addKvPair("--height", "px", "Set the height of image to capture/broadcast")
			.addKvPair("--target-width", "px", "Minimum target width for a blob to be processed.")
			.addKvPair("--target-height", "px", "Minimum target height for a blob to be processed.")
			.addKvPair("--jpeg-quality", "quality", "Set the JPEG quality to request. Must be 1-100")
			.addKvPair("--fps-num", "numerator", "Set the FPS numerator. If the camera does not support the set framerate, the closest one available is chosen.")
			.addKvPair("--fps-denom", "denom", "Set the FPS denominator. If the camera does not support the set framerate, the closest one available is chosen.")
			.addKvPair("--brightness", "number", "Set the brightness of the camera (30-255).")
			.addKvPair("--contrast", "number", "Contrast camera control (0-10).")
			.addKvPair("--saturation", "number", "Saturation camera control (0-200).")
			.addKvPair("--sharpness", "number", "Sharpness camera control (0-50).")
			.addKvPair("--exposure-absolute", "number", "Absolute Exposure camera control (5-20000).")
			.addKvPair("--exposure-auto", "number", "0 for Auto, 1 for Manual, 2 for Shutter Priority, 3 for Aperture Priority. Defaults 1 when processing, 2 when --no-process.")
			// MOE.js
			.addKvPair("--moejs-dir", "directory", "Directory containing MOE.js. Default is ../ where / is where the MoePi JAR was executed.")
			.alias("--moejs", "--moejs-dir")
			.addKvPair("--moejs-port", "port", "Port for MOE.js.")
			// GPIO options
			.addKvPair("--gpio-pin", "pin number", "Set which GPIO pin to use. Is ignored if --no-gpio is set. Default is 0.")
			.addKvPair("--gpio-delay", "microseconds", "Delay for LED flashing in microseconds. Be mindful when changing this that the LED and Camera On/Off stays synchronized by visiting MOE.js in a browser and ensuring you always see a fully lit target (e.g., no random black blips).")
			// Image processor options
			.addKvPair("--max-zeros", "number", "For rejection math. Max times for x'(y) = 0 in a row to constitute being boxy. Default is 4.")
			.addKvPair("--x-skip", "px", "Number of pixels to skip on the x axis when processing sweep 1 (not implemented)")
			.addKvPair("--y-skip", "px", "Number of pixels to skip on the y axis when processing sweep 1 (not implemented)")
			.addFlag("--save-diff", "Save the diff image to a file (./img/delta[#].png). Requires processor.")
			.addKvPair("--save-dir", "directory", "Directory to save diff images when running with the --save-diff flag or --test processing.")
			.addFlag("--trace-contours", "Enable the (dev) contour tracing algorithm (not complete)")
			// Client options
			.addKvPair("--udp-target", "address", "Specify the address to broadcast UDP packets to.")
			.alias("--rio-addr", "--udp-target")
			.addKvPair("--udp-port", "port", "Specify the port to send UDP packets to. Default 5810; a negative port number is equivalent to --no-udp.")
			.alias("--rio-port", "--udp-port")
			.addFlag("--port-override", "Overrides port range constraint for the UDP client.")
			.addKvPair("--mdns-resolve-retry", "time", "Set the interval to retry to resolve the Rio's address.")
			.alias("--rio-resolve-retry", "--mdns-resolve-retry")
			// Disabling stuff options
			.addFlag("--no-process", "Disable image processing.")
			.addFlag("--no-camera", "Do not specify a camera. This option will cause the program to not invoke v4l4j.")
			.addFlag("--no-udp", "Disable broadcasting UDP.")
			.addFlag("--no-gpio", "Disable attaching to a pin. Invoking this option will not invoke WiringPi. Note that the pin is reqired for image processing.")
			.addFlag("--no-server", "Disables MOE.js WebSocket for the camera feed.")
			.build();
		
		return parser;
	}
	
	/**
	 * Builder for CommandLineParser's
	 * @author mailmindlin
	 */
	public static class Builder {
		protected HashMap<String, CommandLineToken> options = new HashMap<>();
		/**
		 * Creates an empty Builder
		 */
		public Builder() {
			
		}
		
		/**
		 * Creates a clone of a given builder
		 * @param src The builder to clone
		 */
		public Builder(Builder src) {
			this.options = new HashMap<>(src.options);
		}
		
		/**
		 * Makes a clone of this Builder, if you want to do that for some reason.
		 * @return self
		 */
		public Builder clone() {
			return new Builder(this);
		}
		
		/**
		 * Add a boolean flag.
		 * @param name The flag's name
		 * @param description A description of what the flag does
		 * @return self
		 */
		public Builder addFlag(String name, String description) {
			options.put(name, new CommandLineFlag(name, description));
			return this;
		}
		
		/**
		 * Add alias.
		 * @param from The name of the alias to create
		 * @param to The name of the flag/option to alias
		 * @return self
		 */
		public Builder alias(String from, String to) {
			options.put(from, new CommandLineAlias(from, to));
			return this;
		}
		
		/**
		 * Register a key-value pair. Key-value pair flags are flags in the format of
		 * <kbd>--flag [value]</kbd>. 
		 * @param name The name of the flag (what is used to set this, including preceding dashes)
		 * @param argName the name of the value (for description only)
		 * @param description A (short) description of what the flag does
		 * @return self
		 */
		public Builder addKvPair(String name, String argName, String description) {
			options.put(name, new CommandLineKVPair(name, argName, description));
			return this;
		}
		
		/**
		 * Builds a CommandLineParser from the data given to this builder
		 * @return built object
		 */
		public CommandLineParser build() {
			return new CommandLineParser(this);
		}
	}
	
	public class CommandLineUsage implements Serializable {
		private static final long serialVersionUID = -1994891773152646790L;
		//TODO finish
		@Override
		public String toString() {
			return "java -Djava.library.path=. -jar MoePi-all.jar [options]";
		}
	}
	
	/**
	 * The type of command line token.
	 * @author mailmindlin
	 */
	public enum CommandLineTokenType {
		/**
		 * Alias token type, which maps directly to another token
		 */
		ALIAS,
		/**
		 * Flag token type, which can be tested for if it is set or not
		 */
		FLAG,
		/**
		 * A key-value pair type, which can be tested if it exists, and what value it
		 * is set to.
		 */
		KV_PAIR
	}
	
	/**
	 * A token to search for in the command line options.
	 * @author mailmindlin
	 *
	 */
	public interface CommandLineToken extends Externalizable {
		/**
		 * 
		 * @return the name of the command
		 */
		String getName();

		/**
		 * A description of how the command may be used. Given as part of the
		 * help string.
		 * 
		 * @return description string
		 */
		String getDescription();
		
		/**
		 * @return the type of command
		 * @see CommandLineTokenType
		 */
		CommandLineTokenType getType();
	}
	
	/**
	 * An alias of another token
	 * @author mailmindlin
	 */
	public static class CommandLineAlias implements CommandLineToken {
		/**
		 * Name of alias
		 */
		protected String name;
		/**
		 * Name of target
		 */
		protected String targetName;
		
		/**
		 * Constructor for deserialization
		 */
		protected CommandLineAlias() {
			
		}
		
		/**
		 * Create an alias for the given command
		 * @param name Name of the alias
		 * @param target Name of the command to alias
		 */
		public CommandLineAlias(String name, String target) {
			this.name = name;
			this.targetName = target;
		}
		
		/**
		 * @return name of the alias
		 */
		@Override
		public String getName() {
			return this.name;
		}
		
		/**
		 * @return Name of the command being aliased by this
		 */
		public String getTarget() {
			return this.targetName;
		}

		@Override
		public String getDescription() {
			return "";
		}

		@Override
		public CommandLineTokenType getType() {
			return CommandLineTokenType.ALIAS;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeUTF(name);
			out.writeUTF(targetName);
		}
		
		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			name = in.readUTF();
			targetName = in.readUTF();
		}
	}
	
	/**
	 * A flag type. Can be queried by name to determine if it has been set. Has no value.
	 * @author mailmindlin
	 */
	public static class CommandLineFlag implements CommandLineToken {
		/**
		 * Name of the flag
		 */
		protected String name;
		/**
		 * Description string
		 */
		protected String description;
		
		/**
		 * Constructor for deserialization
		 */
		public CommandLineFlag() {
			
		}
		
		/**
		 * 
		 * @param name
		 *            Name of the flag, as it will appear in the parameters.
		 *            Must include all preceding dashes
		 * @param description
		 *            Optional (short) description string, describing the use of
		 *            this flag
		 */
		public CommandLineFlag(String name, String description) {
			this.name = name;
			this.description = description;
		}
		
		@Override
		public String getName() {
			return this.name;
		}
		
		@Override
		public String getDescription() {
			return this.description;
		}
		
		@Override
		public CommandLineTokenType getType() {
			return CommandLineTokenType.FLAG;
		}
		
		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeUTF(getName());
			out.writeUTF(getDescription());
		}
		
		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			name = in.readUTF();
			description = in.readUTF();
		}
	}
	public static class CommandLineKVPair implements CommandLineToken {
		protected String name;
		protected String fieldName;
		protected String description;
		
		public CommandLineKVPair() {
			
		}
		
		/**
		 * @param name Name of the key (used in the string). Must include all preceding dashes
		 * @param fieldName Name of the field. Optional, but used in the help string
		 * @param description A short description of how to use this option
		 */
		public CommandLineKVPair(String name, String fieldName, String description) {
			this.name = name;
			this.fieldName = fieldName;
			this.description = description;
		}
		
		/**
		 * Get the name of the key
		 */
		@Override
		public String getName() {
			return this.name;
		}
		
		/**
		 * Get the name of the field, mostly used for the help string.
		 * @return name of the field, or "value" if no field name was given
		 * @see #fieldName
		 */
		public String getFieldName() {
			return this.fieldName == null ? "value" : this.fieldName;
		}
		
		@Override
		public String getDescription() {
			return this.description;
		}
		
		@Override
		public CommandLineTokenType getType() {
			return CommandLineTokenType.KV_PAIR;
		}
		
		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			this.name = in.readUTF();
			this.description = in.readUTF();
			this.fieldName = in.readUTF();
		}
		
		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeUTF(name);
			out.writeUTF(description);
			out.writeUTF(fieldName);
		}
		
	}
}
