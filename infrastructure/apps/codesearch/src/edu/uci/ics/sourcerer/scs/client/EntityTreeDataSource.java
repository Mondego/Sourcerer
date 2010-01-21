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

import java.util.LinkedList;
import java.util.List;

import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.widgets.tree.Tree;

import edu.uci.ics.sourcerer.db.adapter.client.Entity;
import edu.uci.ics.sourcerer.scs.common.client.EntityCategory;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 31, 2009
 */
public class EntityTreeDataSource{
	
	
	public static Tree getTree(ERTables erT, EntityCategory cat){
		Tree tree = new Tree();
		tree.setShowRoot(false);
		tree.setModelType(TreeModelType.PARENT);
		tree.setNameProperty("Short Name");
		tree.setIdField("entityId");
		tree.setParentIdField("parentId");
		
		
		tree.setData(getEntityNodes(erT, cat).toArray(new EntityTreeNode[0]));
		
		return tree;
	}
	
	public static List<EntityTreeNode> getEntityNodes(ERTables erT, EntityCategory cat){
		List<EntityTreeNode> etnList = new LinkedList<EntityTreeNode>();
		
		for(Entity e: erT.entities){
			
			if(JavaNameUtil.isJavaPrimitive(e.fqn)){continue;}
			
			long parentId = e.parentId;
			
			if(e.category.equals(cat)){
				
				// reset the parentId if parent not in same category
				// to make tree list work 
				if (e.parentId > 0) {
					
					for (Entity ep : erT.entities) {
						if (ep.entityId == e.parentId
								&& !ep.category.equals(e.category)) {
							parentId = 0;
							break;
						}
					}
				}
				
				
				etnList.add(new EntityTreeNode(e, parentId));
			}
		}
		
		return etnList;
	}
}