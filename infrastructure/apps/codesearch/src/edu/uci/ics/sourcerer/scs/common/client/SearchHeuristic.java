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
package edu.uci.ics.sourcerer.scs.common.client;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 31, 2009
 * 
 */
public enum SearchHeuristic {
	
	TEXT_USEDFQN_FQN_SimSNAME_SNAME("Text + Used FQNs + FQN + sim sname + sname"),
	TEXT_USEDFQN_FQN_JdkLibSimSNAME_SNAME("Text + Used FQNs + FQN + Jdk/Lib sim sname + sname"),
	TEXT_USEDFQN_FQN_SNAME("Text + Used FQNs + FQN + sname"),
	FQN_USEDFQN_SimSNAME_SNAME("Used FQNs + FQN + sim sname + sname"),
	FQN_USEDFQN_JdkLibSimSNAME_SNAME("Used FQNs + FQN + Jdk/Lib sim sname + sname"),
	FQN_USEDFQN_SNAME("Used FQNs + FQN + sname"),
	TEXT_FQN_SNAME("Text + FQN + sname"),
	TEXT_SNAME("Text + sname"),
	FQN_SNAME("FQN + sname"),
	//FQN_USEDFQN_MIN_ONE_TERM_IN_SNAME("Used FQNs + FQN + min one term match in sname"),
	TEXT("Text Only"),
	
	NONE("Use raw lucene query");
	
	private String description;
	
	private SearchHeuristic(String description){
		this.description = description;
	}
	
	public String toString(){
		return this.description;
	}
	
	
}
