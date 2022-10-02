
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.6.3
//DEPS org.apache.commons:commons-lang3:3.12.0
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.IExitCodeExceptionMapper;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;

@Command(name = "jmorsec", mixinStandardHelpOptions = true, version = "jmorsec 0.1", //
		description = "With that scipt you can encode some text to corresponding morse code, " //
				+ "but also you can decode some morse code to corresponding text.")
class jmorsec implements Callable<Integer> {

	@Option(names = { "-e", "--encode" }, description = "encode input to morse code")
	boolean encode;

	@Option(names = { "-d", "--decode" }, description = "decode input to morse code")
	boolean decode;

	@Parameters(paramLabel = "VALUE", description = "input to encode or decode")
	String input;

	public static void main(String... args) {
		jmorsec jmorsec = new jmorsec();
		int exitCode = new CommandLine(jmorsec) //
				.setExecutionExceptionHandler(jmorsec.new PrintExceptionMessageHandler()) // handels printing error
																							// messages
				.setExitCodeExceptionMapper(jmorsec.new ExitCodeMapper()) // maps the exit code like RuntimeException ->
																			// 34
				.execute(args);
		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception { // your business logic goes here...

		if (isTrue(encode) || isFalse(decode)) {
			encode();
		} else {
			decode();
		}

		return 0;
	}

	private void decode() {
		String decodedString = new MorseDecodeParser().decode(input);
		System.out.printf("The input '%s' decoded is : '%s'", input, decodedString);
	}

	private void encode() {
		String encodedString = new MorseEncodeParser().encode(input);
		System.out.printf("The input '%s' encoded is : '%s'", input, encodedString);
	}

	enum MorseCode {

		ALPHA('A', "*-"), //
		BRAVO('B', "-***"), //
		CHARLIE('C', "-*-*"), //
		DELTA('D', "-**"), //
		ECHO('E', "*"), //
		FOXTROT('F', "**-*"), //
		GOLF('G', "--*"), //
		HOTEL('H', "****"), //
		INDIA('I', "**"), //
		JULIETT('J', "*---"), //
		KILO('K', "-*-"), //
		LIMA('L', "*-**"), //
		MIKE('M', "--"), //
		NOVEMBER('N', "-*"), //
		OSCAR('O', "---"), //
		PAPA('P', "*--*"), //
		QUEBEC('Q', "--*-"), //
		ROMEO('R', "*-*"), //
		SIERRA('S', "***"), //
		TANGO('T', "-"), //
		UNIFORM('U', "**-"), //
		VICTOR('V', "***-"), //
		WHISKEY('W', "*--"), //
		X_RAY('X', "-**-"), //
		YANKEE('Y', "-*--"), //
		ZULU('Z', "--**");

		private char letter;
		private String morse;

		private MorseCode(char letter, String morse) {
			this.letter = letter;
			this.morse = morse;
		}

		public char getLetter() {
			return letter;
		}

		public String getMorse() {
			return morse;
		}
	}

	class MorseEncodeParser {

		public String encode(char symbol) {
			var upperCaseSymbol = Character.toUpperCase(symbol);

			if (isValid(upperCaseSymbol)) {
				return getMorseCode(upperCaseSymbol);
			} else {
				throw new IllegalArgumentException("Symbol can not be parsed.");
			}
		}

		public String encode(String word) {
			return word.chars() //
					.mapToObj(c -> (char) c) //
					.map(this::encode) //
					.collect(Collectors.joining(" "));
		}

		// ----------------------------------------------

		private boolean isValid(char symbol) {
			return Stream.of(MorseCode.values()) //
					.map(MorseCode::getLetter) //
					.anyMatch(letter -> letter.equals(symbol));
		}

		private String getMorseCode(char symbol) {
			return Stream.of(MorseCode.values()) //
					.collect(Collectors.toMap(MorseCode::getLetter, MorseCode::getMorse)) //
					.get(symbol);
		}

	}

	class MorseDecodeParser {

		public String decode(String morseCode) {

			if (isMorseCodeInvalid(morseCode)) {
				throw new IllegalArgumentException("Morsecode could not be parsed");
			}

			return Arrays.asList(morseCode.split(" ")).stream() //
					.map(this::parseMorseCodeToChar) //
					.collect(Collector.of(StringBuilder::new, StringBuilder::append, StringBuilder::append,
							StringBuilder::toString));
		}

		// ----------------------------------------------
		private boolean isMorseCodeInvalid(String morseCode) {
			return Arrays.asList(morseCode.split(" ")).stream() //
					.map(this::isValid) //
					.anyMatch(e -> !e);
		}

		private boolean isValid(String morseCodeSymbol) {
			return Stream.of(MorseCode.values()) //
					.map(MorseCode::getMorse) //
					.anyMatch(morse -> morse.equals(morseCodeSymbol));
		}

		private Character parseMorseCodeToChar(String morseCodeSymbol) {
			return Stream.of(MorseCode.values()) //
					.collect(Collectors.toMap(MorseCode::getMorse, MorseCode::getLetter)) //
					.get(morseCodeSymbol);
		}
	}

	class PrintExceptionMessageHandler implements IExecutionExceptionHandler {
		public int handleExecutionException(Exception ex, CommandLine cmd, ParseResult parseResult) {

			// bold red error message
			cmd.getErr().println(cmd.getColorScheme().errorText(ex.getMessage()));

			return cmd.getExitCodeExceptionMapper() != null ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
					: cmd.getCommandSpec().exitCodeOnExecutionException();
		}
	}

	class ExitCodeMapper implements IExitCodeExceptionMapper {

		@Override
		public int getExitCode(Throwable t) {
			if (t instanceof IllegalArgumentException) {
				return 72;
			}
			return 1;
		}

	}
}
