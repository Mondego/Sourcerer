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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 5, 2010
 *
 */
public class UsedFqn implements Serializable {
	
	private String fqn;
	private int useCount;
	
	public int getUseCount() {
		return useCount;
	}

	

	public EntityType getType() {
		return type;
	}

	public void setType(EntityType type) {
		this.type = type;
	}

	public String getFqn() {
		return fqn;
	}

	public EntityCategory getCategory() {
		return category;
	}

	private List<Long> entityIds;
	private EntityType type;
	private EntityCategory category;
	
	public List<Long> getEntityIdsMatchingFqns(){
		return entityIds;
	}
	
	public UsedFqn(){
		
	}
	
	public UsedFqn(String fqn, EntityCategory category, int useCount){
		this.fqn = fqn;
		this.category = category;
		this.useCount = useCount;
		this.type = EntityType.UNKNOWN;
		this.entityIds = new LinkedList<Long>();
	}
	
	public void addEntityId(Long long1){
		entityIds.add(long1);
	}



	public void setUseCount(int size) {
		this.useCount = size;
		
	}
}
