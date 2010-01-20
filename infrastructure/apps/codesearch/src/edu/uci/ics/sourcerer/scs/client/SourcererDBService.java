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

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.uci.ics.sourcerer.scs.common.client.EntityCategory;
import edu.uci.ics.sourcerer.scs.common.client.HitFqnEntityId;
import edu.uci.ics.sourcerer.scs.common.client.UsedFqn;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 22, 2009
 */

@RemoteServiceRelativePath("sdb")
public interface SourcererDBService extends RemoteService {
	
	ERTables getERTables(List<String> hitEntities);
	UsedFqn fillUsedFqnDetails(HitFqnEntityId fqn, EntityCategory cat);
	List<UsedFqn> fillUsedFqnDetails(List<HitFqnEntityId> fqns, EntityCategory cat);
}
