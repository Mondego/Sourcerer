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
package edu.uci.ics.sourcerer.scs.client;

import edu.uci.ics.sourcerer.scs.common.client.SearchHeuristic;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 31, 2009
 */
public interface ScsSearcher{
	public void sendQueryToServer(boolean isNewServer);
	public void setSearchHeuristic(SearchHeuristic sh);
	
	public void setMode(ScsClientMode mode);
	
	public void addFqnFilter(String fqn);
	public void removeFqnFilter(String fqn);
	public void clearAllFilters();
	public void enableFilter();
	public void disableFilter();
	public boolean isFilterEnabled();
	
}