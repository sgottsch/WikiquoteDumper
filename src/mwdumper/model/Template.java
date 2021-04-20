package mwdumper.model;

import java.util.HashMap;
import java.util.Map;

public class Template {

	private String type;
	private String text = "";

	private String id;

	private Map<String, String> values = new HashMap<String, String>();
	private Map<String, Line> parsedValues = new HashMap<String, Line>();
	private Map<String, Template> templateValues = new HashMap<String, Template>();

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void addText(String text) {
		this.text += text;
	}

	public Map<String, String> getValues() {
		return values;
	}

	public void addValues(String key, String value) {
		this.values.put(key, value);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}

	public Map<String, Line> getParsedValues() {
		return parsedValues;
	}

	public void setParsedValues(Map<String, Line> parsedValues) {
		this.parsedValues = parsedValues;
	}

	public void addParsedValue(String key, Line parsedValue) {
		this.parsedValues.put(key, parsedValue);
	}

	public Map<String, Template> getTemplateValues() {
		return templateValues;
	}

	public void setTemplateValues(Map<String, Template> templateValues) {
		this.templateValues = templateValues;
	}

	public void addTemplateValue(String key, Template template) {
		this.templateValues.put(key, template);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
