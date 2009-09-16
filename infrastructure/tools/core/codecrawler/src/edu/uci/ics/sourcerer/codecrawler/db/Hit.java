/*
 * Sourcerer: An infrastructure for large-scale source code analysis.
 * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package edu.uci.ics.sourcerer.codecrawler.db;

import java.util.Date;

/**
 * @author <a href="mailto:hahrot@yahoo.com">Huy A. Huynh</a>
 */
public class Hit {

	private long id;
	private String checkoutString;
	private Date hitDate;
	
	private String projectName;
	private String projectDescription;
	private String projectCategory;		//multiple entries is possible, separated by semicolons
	private String projectLicense;
	
	private String version;
	private String releaseDate;
	private String description;
	private String sourceCode;
	private String containerUrl;
	
	private String language;		//multiple languages is possible, separated by semicolons
	private String platform;		//multiple platforms is possible, separated by semicolons
	
	private String keywords;
	private String fileExtensions;

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public String getCheckoutString() {
		return checkoutString;
	}

	public void setCheckoutString(String checkoutString) {
		this.checkoutString = checkoutString;
	}

	public Hit(long id, String checkoutString) {
		super();

		this.id = id;
		this.checkoutString = checkoutString;
		hitDate = new Date();
	}

	public String getProjectCategory() {
		return projectCategory;
	}

	public void setProjectCategory(String category) {
		this.projectCategory = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getProjectLicense() {
		return projectLicense;
	}

	public void setProjectLicense(String license) {
		this.projectLicense = license;
	}

	public Date getHitDate() {
		return hitDate;
	}

	public void setHitDate(Date hitDate) {
		this.hitDate = hitDate;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getProjectDescription() {
		return projectDescription;
	}

	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}

	public String getFileExtensions() {
		return fileExtensions;
	}

	public void setFileExtensions(String fileExtensions) {
		this.fileExtensions = fileExtensions;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getContainerUrl() {
		return containerUrl;
	}

	public void setContainerUrl(String containerUrl) {
		this.containerUrl = containerUrl;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	
}
