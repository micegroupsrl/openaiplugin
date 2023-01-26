# OpenAI plugin For Eclipse
This plugin allows developers to easily generate code using OpenAI's GPT-3 technology, directly from within the Eclipse IDE.

## Requirements
- OpenAI API Secret Key
- Eclipse 4.17 or later
- Java 8 or later

## Installation
- Download the plugin from the update site
- Install the plugin in your Eclipse installation

## Features
- Generate code from text selections in your editor, whether it be method descriptions or javadocs
- Multiple text selections are supported, allowing you to generate code for multiple items at once
- Activation through resource context menu or shortcut 
- Choose from multiple models, with text-davinci-003 as the default
- Set the maximum number of tokens to use for each code generation request, with a default of 2000

## Usage
- Select a piece of text in your editor
- Right click and select "OpenAI Code Generator" in the resource context menu
- Alternatively, use the shortcut (default is Ctrl+Alt+O) to activate the plugin
- The generated code will be inserted at the location of your text selection
- Choose the desired model and set the maximum number of tokens from the plugin's preferences page

## Note
- The OpenAI API key is encrypted before being saved in the Eclipse preferences store for security reasons.
- The plugin uses the AES algorithm for encrypting the API key.
- The maximum number of tokens used for code generation can be set from the plugin's preferences page.
- On the first use of the plugin, the OpenAI API key will need to be entered in the preferences page.

## Known Issues
- Formatting of generated code may not always be perfect
- Large amount of text may cause the plugin to slow down
- Plugin may not work with all languages

## Future Improvements
- Improve code formatting
- Add support for more languages
- Add option for user to choose the formatting style
- Add option for user to fine-tune the code generation settings

## Contribution
The source code for this plugin is available on GitHub and all pull requests are welcome.

## Authors
[micegroup.it](https://www.micegroup.it)

[comment]: <> (## Licenza
Questo progetto è rilasciato sotto la licenza [<nome licenza>].)
