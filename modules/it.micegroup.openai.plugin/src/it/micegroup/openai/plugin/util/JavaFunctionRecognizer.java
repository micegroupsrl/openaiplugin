package it.micegroup.openai.plugin.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaFunctionRecognizer {

	private static final String FUNCTION_PATTERN = "^[\\w\\s]*\\s+[\\w]+\\s*\\(.*\\)\\s*\\{.*\\}$";

	public static boolean isFunction(String input) {
		Pattern pattern = Pattern.compile(FUNCTION_PATTERN);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
}
