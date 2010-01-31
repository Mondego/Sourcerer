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

import java.util.HashSet;
import java.util.LinkedList;

import com.smartgwt.client.widgets.layout.VLayout;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Aug 11, 2009
 */
public class VLHit extends VLayout implements
		ITakesSelectionUpdateEvent {
	boolean selected;
	private String entityId;

	ISelectedFqnsProvider fqns;

	IERProvider scs;

	public VLHit(ISelectedFqnsProvider fqns, String entityId, IERProvider scs) {
		this.fqns = fqns;
		this.scs = scs;
		this.entityId = entityId;

		highlightHit(fqns.getSelectedUsedJdkFqns(), fqns
				.getSelectedUsedLibFqns());

	}

	public void process(ApiSelectionUpdateEvent event) {
		
		highlightHit(((VLTopApis) event.getSource()).getSelectedUsedJdkFqns(),
				((VLTopApis) event.getSource()).getSelectedUsedLibFqns());

	}

	private void highlightHit(final HashSet<String> selectedJdkFqns,
			final HashSet<String> selectedLibFqns) {

		ERTables erTables = scs.getErTables();

		if (erTables == null)
			return;

		LinkedList<String> usedJdkFqns = erTables
				.getJdkFqnsUsedByEntityId(entityId);
		LinkedList<String> usedLibFqns = erTables
				.getLibFqnsUsedByEntityId(entityId);

		if (usedJdkFqns == null)
			usedJdkFqns = new LinkedList<String>();
		if (usedLibFqns == null)
			usedLibFqns = new LinkedList<String>();

		boolean _highlight = false;

		boolean _hasAllJdk = false;
		boolean _hasAllLib = false;

		if (selectedJdkFqns.size() > 0) {
			_hasAllJdk = isContained(usedJdkFqns, selectedJdkFqns);
		}

		if (selectedLibFqns.size() > 0) {
			_hasAllLib = isContained(usedLibFqns, selectedLibFqns);
		}

		if (selectedJdkFqns.size() == 0 && selectedLibFqns.size() == 0) {
			_highlight = false;
		} else if (selectedJdkFqns.size() <= 0) {
			_highlight = _hasAllLib;
		} else if (selectedLibFqns.size() <= 0) {
			_highlight = _hasAllJdk;
		} else {
			_highlight = _hasAllJdk && _hasAllLib;
		}

		if (_highlight) {
			this.setBorder("4px solid #C8FCB7");
		} else {
			this.setBorder("0px");
		}

	}

	// empty child set is considered contained in the parent
	private static boolean isContained(LinkedList<String> parent,
			HashSet<String> child) {

		if (parent.size() <= 0)
			return false;

		for (String s : child) {
			if (!isStringInList(parent, s))
				return false;
		}

		return true;
	}

	private static boolean isStringInList(LinkedList<String> list, String s) {
		for (String _s : list) {
			if (_s.equals(s))
				return true;
		}

		return false;
	}

}
