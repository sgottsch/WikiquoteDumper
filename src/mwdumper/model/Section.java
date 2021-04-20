package mwdumper.model;

import java.util.ArrayList;
import java.util.List;

public class Section {

	private int level;

	private Line title;
	private List<Line> lines;

	private List<Section> subSections = new ArrayList<Section>();
	private List<Template> templates = new ArrayList<Template>();

	private Boolean isChronological = null;

	public Section() {

	}

	public Section(int level, Line title) {
		super();
		this.level = level;
		this.title = title;
		this.lines = new ArrayList<Line>();
	}

	public int getLevel() {
		return level;
	}

	public Line getTitle() {
		return title;
	}

	public List<Line> getLines() {
		return lines;
	}

	public List<Section> getSubSections() {
		return subSections;
	}

	public void addSubSection(Section subSection) {
		this.subSections.add(subSection);
	}

	public void addLine(Line line) {
		this.lines.add(line);
	}

	public Boolean getIsChronological() {
		return isChronological;
	}

	public void setIsChronological(Boolean isChronological) {
		this.isChronological = isChronological;
	}

	public List<Template> getTemplates() {
		return templates;
	}

	public void addTemplate(Template template) {
		this.templates.add(template);
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setTitle(Line title) {
		this.title = title;
	}

	public void setLines(List<Line> lines) {
		this.lines = lines;
	}

	public void setSubSections(List<Section> subSections) {
		this.subSections = subSections;
	}

	public void setTemplates(List<Template> templates) {
		this.templates = templates;
	}

}
