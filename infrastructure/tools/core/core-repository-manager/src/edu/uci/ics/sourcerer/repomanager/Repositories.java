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
package edu.uci.ics.sourcerer.repomanager;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public enum Repositories {
	
	SOURCEFORGE("http://sourceforge.net"),
	TIGRIS("http://tigris.net"),
	JAVANET("http://java.net"),
	APACHE("http://archive.apache.org");
	
	private String URL;
	Repositories(String url){
		this.URL = url;
	}
	
	public String getUrl(){
		return URL;
	}
	
	public static boolean isRepositoryName(String name){
		
		for(Repositories repo: Repositories.values()){
			if(repo.name().equals(name))
				return true;
		}
		
		return false;
	}
}
