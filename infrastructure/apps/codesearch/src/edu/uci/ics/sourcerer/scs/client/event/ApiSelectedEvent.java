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
package edu.uci.ics.sourcerer.scs.client.event;


import com.google.gwt.event.shared.GwtEvent;

import edu.uci.ics.sourcerer.db.adapter.client.EntityCategory;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Sep 10, 2009
 */
public class ApiSelectedEvent extends GwtEvent<ApiSelectedEventHandler> {

	public enum Operation{
		SELECT,
		DESELECT
	}
	
	public String fqn;
	public EntityCategory cat;
	public Operation op;
	
	@Override
	protected void dispatch(ApiSelectedEventHandler handler) {
		handler.onApiSelection(this);
	}

	@Override
	public GwtEvent.Type<ApiSelectedEventHandler> getAssociatedType() {
		return getType();
	}
	

	public static GwtEvent.Type<ApiSelectedEventHandler> getType() {
		return TYPE;
	}

	private static final GwtEvent.Type<ApiSelectedEventHandler> TYPE = new GwtEvent.Type<ApiSelectedEventHandler>();
	
	public ApiSelectedEvent(String fqn, EntityCategory cat, Operation op) {
		this.fqn = fqn;
		this.cat = cat;
		this.op = op;
	}
	

}
