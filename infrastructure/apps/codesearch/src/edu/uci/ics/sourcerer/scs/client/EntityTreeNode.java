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

import static edu.uci.ics.sourcerer.scs.client.JavaNameUtil.extractShortName;

import com.smartgwt.client.widgets.tree.TreeNode;

import edu.uci.ics.sourcerer.db.adapter.client.Entity;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 31, 2009
 */
public class EntityTreeNode extends TreeNode{

	public EntityTreeNode(Entity e, long parentId){
		String sname = extractShortName(e.fqn);
		if(sname.trim().equals("<init>")) sname = "&lt;init&gt";
		//else if(sname.trim().equals("<clinit>")) sname = "&lt;clinit&gt";
		
		setAttribute("Short Name", "<b>" + sname + "</b> :: " + "<small>" + e.fqn + "</small>");
		setAttribute("parentId", e.parentId + "");
		setAttribute("entityId", e.entityId + "");
		setAttribute("Uses", e.useCount);
		
	}
}
