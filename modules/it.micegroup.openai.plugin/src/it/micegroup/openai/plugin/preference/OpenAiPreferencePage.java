package it.micegroup.openai.plugin.preference;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import it.micegroup.openai.plugin.Activator;
import it.micegroup.openai.plugin.handlers.OpenAiGenerateCodeAction;
import it.micegroup.openai.plugin.util.EncryptionUtil;
import it.micegroup.openai.plugin.util.OpenAiConstants;

public class OpenAiPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private String oldApiKey;

	private String oldModel;

	/**
	 * Constructor for OpenAiPreferencePage.
	 * 
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 */
	public OpenAiPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(OpenAiConstants.PREFERENCE_PAGE_DESCRIPTION);
		oldApiKey = getPreferenceStore().getString(OpenAiConstants.OPENAI_API_KEY);
		oldModel = getPreferenceStore().getString(OpenAiConstants.OPENAI_MODEL);

	}

	/**
	 * Creates the field editors for the preference page.
	 * 
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors() {

		StringFieldEditor apiKeyField = new StringFieldEditor(OpenAiConstants.OPENAI_API_KEY, "OpenAi Secret Key:",
				getFieldEditorParent());
		apiKeyField.getTextControl(getFieldEditorParent()).setEchoChar('*');
		apiKeyField.setEmptyStringAllowed(false);
		apiKeyField.setErrorMessage("API key is required");
		addField(apiKeyField);

		getPreferenceStore().setDefault(OpenAiConstants.OPENAI_LANGUAGE, "java");
		RadioGroupFieldEditor languageField = new RadioGroupFieldEditor(OpenAiConstants.OPENAI_LANGUAGE, "Language:", 1,
				new String[][] { { "Java", "java" }, { "Python", "python" }, { "C++", "c++" }, { "C#", "c#" },
						{ "Javascript", "javascript" }, { "PHP", "php" } },
				getFieldEditorParent(), true);
		addField(languageField);

		IntegerFieldEditor maxTokenField = new IntegerFieldEditor(OpenAiConstants.OPENAI_MAX_TOKEN,
				"Max Token for each request:", getFieldEditorParent());
		maxTokenField.getTextControl(getFieldEditorParent()).setTextLimit(4);
//		maxTokenField.setValidRange(0, 4097);
		addField(maxTokenField);

		StringFieldEditor modelField = new StringFieldEditor(OpenAiConstants.OPENAI_MODEL, "OpenAi Model:",
				getFieldEditorParent());
		addField(modelField);

		GridData gridDataMaxToken = new GridData();
		gridDataMaxToken.widthHint = 50;
		maxTokenField.getTextControl(getFieldEditorParent()).setLayoutData(gridDataMaxToken);

		GridData gridDataModel = new GridData();
		gridDataModel.widthHint = 300;
		modelField.getTextControl(getFieldEditorParent()).setLayoutData(gridDataModel);

		GridData gridDataKey = new GridData();
		gridDataKey.widthHint = 450;
		apiKeyField.getTextControl(getFieldEditorParent()).setLayoutData(gridDataKey);

	}

	/**
	 * This method is used to initialize the preference page with default values
	 *
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 *
	 * @param arg0 IWorkbench
	 */
	@Override
	public void init(IWorkbench arg0) {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		preferenceStore.setDefault(OpenAiConstants.OPENAI_MAX_TOKEN, OpenAiConstants.MAX_TOKEN_DEFAULT);
		preferenceStore.setDefault(OpenAiConstants.OPENAI_MODEL, OpenAiConstants.MODEL_DEFAULT);

	}

	/**
	 * This method is used to perform the 'OK' action when the user clicks the 'OK'
	 * button in the preference page. It encrypts the api key before storing it.
	 *
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 *
	 * @return boolean
	 */
	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		String apiKey = getPreferenceStore().getString(OpenAiConstants.OPENAI_API_KEY);
		String maxToken = getPreferenceStore().getString(OpenAiConstants.OPENAI_MAX_TOKEN);
		String model = getPreferenceStore().getString(OpenAiConstants.OPENAI_MODEL);
		if (!apiKey.isEmpty() && !apiKey.equals(oldApiKey)) {
			try {
				apiKey = EncryptionUtil.encrypt(apiKey);
				getPreferenceStore().setValue(OpenAiConstants.OPENAI_API_KEY, apiKey);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		getPreferenceStore().setValue(OpenAiConstants.OPENAI_MAX_TOKEN, maxToken);
		if (!model.equals(oldModel) && !model.equals(OpenAiConstants.MODEL_DEFAULT)) {
			try {
				if (!OpenAiGenerateCodeAction.isValidModel(model, EncryptionUtil.decrypt(apiKey))) {
					MessageDialog.openError(getShell(), "Error", "Invalid model. Please enter a valid model name.");
					getPreferenceStore().setToDefault(OpenAiConstants.OPENAI_MODEL);
					return false;
				} else {
					getPreferenceStore().setValue(OpenAiConstants.OPENAI_MODEL, model);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return result;
	}

	/**
	 * The performApply method is called when the user presses the Apply button on
	 * the preference page. This method saves the current preferences to the
	 * preference store.
	 * 
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 */
	@Override
	protected void performApply() {
		super.performApply();
		String apiKey = getPreferenceStore().getString(OpenAiConstants.OPENAI_API_KEY);
		if (!apiKey.isEmpty()) {
			try {
				apiKey = EncryptionUtil.encrypt(apiKey);
				getPreferenceStore().setValue(OpenAiConstants.OPENAI_API_KEY, apiKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * The performDefaults method is called when the user presses the Defaults
	 * button on the preference page. This method sets the default values for the
	 * preferences.
	 * 
	 * @author Ciro Dolce <ciro.dolce@micegroup.it>
	 */
	@Override
	protected void performDefaults() {
		super.performDefaults();
		getPreferenceStore().setValue(OpenAiConstants.OPENAI_API_KEY, "");
	}
}
