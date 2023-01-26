package it.micegroup.openai.plugin.handlers;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;

import it.micegroup.openai.plugin.util.EncryptionUtil;
import it.micegroup.openai.plugin.util.JavaFunctionRecognizer;
import it.micegroup.openai.plugin.util.OpenAiConstants;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OpenAiGenerateCodeAction extends AbstractHandler {

	private OpenAiService openAiService;

	/**
	 * Execute method of the OpenAiGenerateCodeAction class, handles the code
	 * generation process
	 *
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 *
	 * @param event ExecutionEvent
	 * @return Object
	 */
	@Override
	public Object execute(ExecutionEvent event) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		String apiKey = null;
		try {
			apiKey = getStoredApiKey(window);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (apiKey == null || apiKey.isEmpty()) {
			openPreferencePage(window);
			try {
				apiKey = getStoredApiKey(window);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (apiKey != null) {
			openAiService = new OpenAiService(apiKey, 90);
		} else {
			MessageDialog.openError(window.getShell(), OpenAiConstants.ERROR,
					"Insert OpenAi Secret Key for generate code.");
			return null;
		}

		List<String> selectedTexts = getSelectedTexts(window);

		if (selectedTexts.isEmpty()) {
			MessageDialog.openError(window.getShell(), OpenAiConstants.ERROR,
					"Select a text before generate code with OpenAI.");
			return null;
		}

		ProgressMonitorDialog pmd = new ProgressMonitorDialog(window.getShell());
		pmd.open();
		pmd.getProgressMonitor().beginTask("Code generation with OpenAi...", IProgressMonitor.UNKNOWN);

		String generatedCode = "";
		for (String selectedText : selectedTexts) {
			String result = null;
			try {
				result = generateCodeWithOpenAI(selectedText.trim(), window, pmd, apiKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
			generatedCode += result;
		}
		if (!generatedCode.equals("null") && !generatedCode.equals("")) {
			insertCodeIntoEditor(generatedCode, window);
		}

		pmd.close();
		return null;
	}

	/**
	 * Inserts the given code into the currently active editor at the current
	 * selection.
	 * 
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 * 
	 * @param code   The code to insert
	 * @param window The current workbench window
	 */
	private void insertCodeIntoEditor(String code, IWorkbenchWindow window) {
		IEditorPart editor = window.getActivePage().getActiveEditor();
		if (editor instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) editor;
			IDocumentProvider documentProvider = textEditor.getDocumentProvider();
			IDocument document = documentProvider.getDocument(textEditor.getEditorInput());
			ISelection selection = textEditor.getSelectionProvider().getSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection) selection;
				int offset = textSelection.getOffset();
				int length = textSelection.getLength();
				try {
					document.replace(offset, length, code);
				} catch (org.eclipse.jface.text.BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Returns a list of the selected texts in the currently active editor.
	 * 
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 * 
	 * @param window The current workbench window
	 * @return A list of the selected texts
	 */
	private List<String> getSelectedTexts(IWorkbenchWindow window) {
		List<String> selectedTexts = new ArrayList<String>();
		IEditorPart editor = window.getActivePage().getActiveEditor();
		if (editor instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) editor;
			IDocumentProvider documentProvider = textEditor.getDocumentProvider();
			IDocument document = documentProvider.getDocument(textEditor.getEditorInput());
			ISelection selection = textEditor.getSelectionProvider().getSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection) selection;
				int startLine = textSelection.getStartLine();
				int endLine = textSelection.getEndLine();
				StringBuilder sb = new StringBuilder();
				boolean inJavadoc = false;
				for (int i = startLine; i <= endLine; i++) {
					try {
						String lineText = document.get(document.getLineOffset(i), document.getLineLength(i)).trim();
						if (lineText.startsWith("//")) {
							if (sb.length() > 0) {
								selectedTexts.add(sb.toString());
								sb.setLength(0);
							}
							lineText = lineText.substring(2);
							selectedTexts.add(lineText);
						} else if (lineText.startsWith("/**")) {
							inJavadoc = true;
							sb.append(lineText);
						} else if (inJavadoc) {
							sb.append(lineText);
							if (lineText.endsWith("*/")) {
								selectedTexts.add(sb.toString());
								sb.setLength(0);
								inJavadoc = false;
							} else if (i < endLine) {
								sb.append(System.lineSeparator());
							}
						} else if (!lineText.isBlank()) {
							if (selectedTexts.size() > 0) {
								selectedTexts.set(selectedTexts.size() - 1,
										selectedTexts.get(selectedTexts.size() - 1) + lineText);
							} else {
								selectedTexts.add(lineText);
							}
						}
					} catch (org.eclipse.jface.text.BadLocationException e) {
						e.printStackTrace();
					}
				}
				if (sb.length() > 0) {
					selectedTexts.add(sb.toString());
				}
			}
		}
		return selectedTexts;
	}

	/**
	 * Open the preference page to insert the OpenAI secret key
	 *
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 *
	 * @param window IWorkbenchWindow
	 */
	private void openPreferencePage(IWorkbenchWindow window) {
		PreferenceDialog preferenceDialog = PreferencesUtil.createPreferenceDialogOn(window.getShell(),
				OpenAiConstants.OPENAI_PREFERENCE_PAGE_PATH, null, null);
		preferenceDialog.open();
	}

	/**
	 * Get the stored api key from the preferences
	 *
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 *
	 * @param window IWorkbenchWindow
	 * @return String
	 * @throws Exception
	 */
	private String getStoredApiKey(IWorkbenchWindow window) throws Exception {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(OpenAiConstants.PLUGIN_ID);
		return EncryptionUtil.decrypt(preferences.get(OpenAiConstants.OPENAI_API_KEY, null));
	}

	/**
	 * Generates code using OpenAI API
	 *
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 *
	 * @param selectedText the selected text
	 * @param window       the active workbench window
	 * @param pmd          the progress monitor dialog
	 * @return the generated code
	 * @throws Exception
	 */
	private String generateCodeWithOpenAI(String selectedText, IWorkbenchWindow window, ProgressMonitorDialog pmd,
			String apiKey) throws Exception {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(OpenAiConstants.PLUGIN_ID);

		String model = getModel(window, pmd, apiKey, preferences);
		String maxToken = getMaxToken(preferences);
		String language = getLanguage(window, preferences);
		String prompt = getPrompt(selectedText, language, window);

		CompletionRequest completionRequest = CompletionRequest.builder().prompt(prompt).model(model).echo(false)
				.maxTokens(Integer.parseInt(maxToken)).build();
		try {
			String result = openAiService.createCompletion(completionRequest).getChoices().iterator().next().getText()
					.trim();
			if (isJavaDocComment(selectedText)) {
				return selectedText + "\n" + result;
			}
			return result;
		} catch (Exception e) {
			handleOpenAiError(window, pmd, e);
		}
		return null;
	}

	/**
	 * This method handles the error that is generated when the OpenAI service is
	 * not able to generate the code. It shows an error message to the user with the
	 * specific error message and the status code.
	 * 
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 * 
	 * @param window The active workbench window
	 * @param e      The Exception that was thrown
	 */
	private void handleOpenAiError(IWorkbenchWindow window, ProgressMonitorDialog pmd, Exception e) {
		pmd.close();
		if (e.getMessage().contains("401")) {
			MessageDialog.openError(window.getShell(), OpenAiConstants.ERROR + " 401 - Forbidden", OpenAiConstants.ERROR
					+ " with OpenAi Connection: " + e.getMessage()
					+ ".\nPlease check your OpenAi Secret Key.\nYou can change your OpenAi Secret Key in Eclipse's OpenAi preferences.");
		} else {
			MessageDialog.openError(window.getShell(), OpenAiConstants.ERROR,
					OpenAiConstants.ERROR + " with OpenAi connection: " + e.getMessage());
		}
		e.printStackTrace();
	}

	/**
	 * This method retrieves the prompt for the OpenAI service based on the selected
	 * text and the plugin settings.
	 * 
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 * 
	 * @param selectedText The text that was selected by the user
	 * @return The prompt that will be used for the OpenAI
	 * @throws Exception
	 */
	private String getPrompt(String selectedText, String language, IWorkbenchWindow window) throws Exception {
		if (isJavaDocComment(selectedText)) {
			if (language.equals(OpenAiConstants.LANGUAGE_DEFAULT)) {
				return OpenAiConstants.JAVADOC_STATIC_PROMPT.concat(selectedText);
			} else {
				MessageDialog.openError(window.getShell(), OpenAiConstants.ERROR,
						"Generation via javadoc is only available by selecting Java as the language.\nPlease select Java as the language in the Eclipse preferences");
				throw new Exception();
			}
		} else {
			if (language.equals(OpenAiConstants.LANGUAGE_DEFAULT)) {
				if (!JavaFunctionRecognizer.isFunction(selectedText)) {
					return String.format(OpenAiConstants.DESCRIPTION_STATIC_PROMPT_JAVADOC, language)
							.concat(selectedText);
				} else {
					return OpenAiConstants.JAVA_REFACTOR_FUNCTION_PROMPT.concat(selectedText);
				}
			} else if (!language.isBlank() && !language.isEmpty()) {
				return String.format(OpenAiConstants.DESCRIPTION_STATIC_PROMPT, language).concat(selectedText);
			}
		}
		return null;
	}

	/**
	 * Returns the maximum number of tokens (words) that the OpenAI API should
	 * generate. If the maximum number of tokens is not set in the preferences, the
	 * default value is returned.
	 * 
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 * 
	 * @param preferences The preferences of the plugin
	 * @return The maximum number of tokens (words) that the OpenAI API should
	 *         generate
	 */
	private String getMaxToken(IEclipsePreferences preferences) {
		String maxToken = preferences.get(OpenAiConstants.OPENAI_MAX_TOKEN, null);
		if (maxToken == null || maxToken.isEmpty() || maxToken.isBlank()) {
			maxToken = Integer.toString(OpenAiConstants.MAX_TOKEN_DEFAULT);
		}
		return maxToken;
	}

	/**
	 * Returns the name of the model that should be used for code generation with
	 * OpenAI. If the model is not set in the preferences, the default value is
	 * returned. If the model is set but is not valid, an error message is displayed
	 * and an exception is thrown.
	 * 
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 * 
	 * @param window      The active workbench window
	 * @param pmd         The progress monitor dialog
	 * @param apiKey      The API key for OpenAI
	 * @param preferences The preferences of the plugin
	 * @return The name of the model to be used for code generation
	 * @throws IOException If an error occurs while communicating with the OpenAI
	 *                     API
	 * @throws Exception   If the model is not valid
	 */
	private String getModel(IWorkbenchWindow window, ProgressMonitorDialog pmd, String apiKey,
			IEclipsePreferences preferences) throws IOException, Exception {
		String model = preferences.get(OpenAiConstants.OPENAI_MODEL, null);
		if (model == null || model.isEmpty() || model.isBlank()) {
			model = OpenAiConstants.MODEL_DEFAULT;
		} else {
			if (!isValidModel(model, apiKey)) {
				pmd.close();
				MessageDialog.openError(window.getShell(), OpenAiConstants.ERROR, "Model ".concat(model)
						.concat(" is not valid.\nPlease change your model name or restore default."));
				throw new Exception("Model ".concat(model).concat(" is not valid."));
			}
		}
		return model;
	}

	/**
	 * Returns the name of the language that should be used for code generation with
	 * OpenAI. If the language is not set in the preferences, the default value is
	 * returned and error message is displayed and an exception is thrown.
	 * 
	 * @param window
	 * @param preferences
	 * @return
	 * @throws Exception
	 */
	private String getLanguage(IWorkbenchWindow window, IEclipsePreferences preferences) throws Exception {
		String language = preferences.get(OpenAiConstants.OPENAI_LANGUAGE, null);
		if (language == null || language.isEmpty() || language.isBlank()) {
			return OpenAiConstants.LANGUAGE_DEFAULT;
		}
		return language;

	}

	/**
	 * This method checks if the input string is a JavaDoc comment.
	 *
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 *
	 * @param input the input string to check
	 * @return true if the input string is a JavaDoc comment, false otherwise
	 */

	public static boolean isJavaDocComment(String input) {
		if (input.startsWith("/**") && input.endsWith("*/")) {
			if (input.matches("(?s)/\\*\\*.*\\n\\s*\\*\\s*(.+\\n)*.*\\*/.*"))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * This method is used to check if the provided OpenAI model is valid or not. It
	 * makes a GET request to the OpenAI API and returns true if the response code
	 * is 200.
	 * 
	 * @author Ciro Dolce ciro.dolce@micegroup.it
	 * 
	 * @param model  The name of the model to check
	 * @param apiKey The OpenAI API key to use for the request
	 * @return boolean indicating if the model is valid or not
	 * @throws Exception
	 */
	public static Boolean isValidModel(String model, String apiKey) throws Exception {
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		Request request = new Request.Builder().url(OpenAiConstants.GET_OPENAI_MODELS_URL.concat(model))
				.method(OpenAiConstants.GET, null)
				.addHeader(OpenAiConstants.AUTHORIZATION, OpenAiConstants.BEARER.concat(apiKey)).build();
		Response response = client.newCall(request).execute();
		if (response.code() == 200)
			return true;
		if (response.code() == 404)
			return false;
		throw new Exception("Error: " + response.code());
	}
}