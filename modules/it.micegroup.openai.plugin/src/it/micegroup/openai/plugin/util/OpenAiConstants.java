package it.micegroup.openai.plugin.util;

public class OpenAiConstants {

	public static final String PLUGIN_ID = "it.micegroup.openai.plugin";

	// preference key
	public static final String OPENAI_API_KEY = "openai_api_key";
	public static final String OPENAI_MAX_TOKEN = "max_token";
	public static final String OPENAI_MODEL = "openai_model";
	public static final String OPENAI_LANGUAGE = "openai_language";

	public static final String MODEL_DEFAULT = "text-davinci-003";
	public static final String LANGUAGE_DEFAULT = "java";
	public static final int MAX_TOKEN_DEFAULT = 2000;

	public static final String MAX_TOKEN_DESCRIPTION = "Inserire il numero massimo di token consentiti per una richiesta:";
	public static final String API_KEY_LABEL = "API Key:";
	public static final String MAX_TOKEN_LABEL = "Max Token:";
	public static final String PREFERENCE_PAGE_DESCRIPTION = "OpenAI General Preferences:";
	public static final String INSERT_KEY_DIALOG_DESCRIPTION = "Inserisci la tua chiave segreta dell'API di OpenAI:";
	public static final String OPENAI_PREFERENCE_PAGE_PATH = "openaiplugin.preference.openai";
	public static final byte[] KEY = "MySuperSecretKey".getBytes();
	public static final String ALGORITHM = "AES";
	public static final String JAVADOC_STATIC_PROMPT = "Just write me the Java code of a method from the javadoc below: ";
	public static final String DESCRIPTION_STATIC_PROMPT = "Scrivmi soltanto il codice di un metodo in %s, che: ";
	public static final String DESCRIPTION_STATIC_PROMPT_JAVADOC = "Scrivmi soltanto il codice di un metodo Java, compreso di Javadoc che: ";
	public static final String JAVA_REFACTOR_FUNCTION_PROMPT = "Can you refactor this function?: ";
	public static final String ERROR = "Error";
	public static final String GET = "GET";
	public static final String AUTHORIZATION = "Authorization";
	public static final String BEARER = "Bearer ";
	public static final String GET_OPENAI_MODELS_URL = "https://api.openai.com/v1/models/";

}
