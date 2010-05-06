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

package edu.uci.ics.sourcerer.db.adapter;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.uci.ics.sourcerer.scs.common.SourcererSearchAdapter;
import edu.uci.ics.sourcerer.scs.common.client.EntityCategory;
import edu.uci.ics.sourcerer.scs.common.client.EntityType;
import edu.uci.ics.sourcerer.scs.common.client.HitFqnEntityId;
import edu.uci.ics.sourcerer.scs.common.client.UsedFqn;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 20, 2009
 */

public class SourcererDbAdapter {

	private JdbcDataSource dataSource;

	// initialization

	public void setDataSource(JdbcDataSource ds) {
		this.dataSource = ds;
	}

	public UsedFqn fillUsedFqnDetails(HitFqnEntityId hitFqn, EntityCategory cat) {

		String table = "entities";

		// if (cat == EntityCategory.JDK) {
		// table = "library_entities";
		// } else if (cat == EntityCategory.LIB) {
		// table = "jar_entities";
		// } else if (cat == EntityCategory.LOCAL) {
		// table = "entities";
		// } else {
		// return null; // never
		// }

		String _sql = "select entity_id, entity_type from " + table
				+ " where fqn='" + hitFqn.fqn + "'";

		Iterator<Map<String, Object>> _results = dataSource.getData(_sql);

		UsedFqn _usedFqn = new UsedFqn(hitFqn.fqn, cat, hitFqn.getUseCount());

		while (_results.hasNext()) {
			Map<String, Object> resultMap = _results.next();

			_usedFqn.addEntityId(new Long(((BigInteger) resultMap
					.get("entity_id")).longValue()));
			String _entity_type = (String) resultMap.get("entity_type");

			setEntityType(_usedFqn, _entity_type);

			if (_usedFqn.getType().equals(EntityType.UNKNOWN) /*
															 * ||
															 * ufqn.getType().
															 * equals
															 * (EntityType.
															 * OTHER)
															 */) {
				_usedFqn.setType(EntityType
						.valueOf(lookupJarEntityTypeForUnknown(_usedFqn
								.getFqn())));
			}

		}

		return _usedFqn;

	}

	/**
	 * @param _usedFqn
	 * @param _entity_type
	 */
	private void setEntityType(UsedFqn _usedFqn, String _entity_type) {

		if (_entity_type.equals("CLASS")) {
			_usedFqn.setType(EntityType.CLASS);
		} else if (_entity_type.equals("INTERFACE")) {
			_usedFqn.setType(EntityType.INTERFACE);
		} else if (_entity_type.equals("METHOD")) {
			_usedFqn.setType(EntityType.METHOD);
		} else if (_entity_type.equals("CONSTRUCTOR")) {
			_usedFqn.setType(EntityType.CONSTRUCTOR);
		} else if (_entity_type.equals("FIELD")) {
			_usedFqn.setType(EntityType.FIELD);
		} else if (_entity_type.equals("UNKNOWN")) {
			_usedFqn.setType(EntityType.UNKNOWN);
		} else
			_usedFqn.setType(EntityType.OTHER);

	}

	public String lookupJarEntityTypeForUnknown(String fqn) {

		fqn = fqn.replaceFirst("\\.\\(", ".<init>(");
		String sql = "select distinct entity_type from entities where fqn='"
				+ fqn + "'" + " and entity_type <> 'UNKNOWN'";

		String type = "UNKNOWN";

		Iterator<Map<String, Object>> _results = dataSource.getData(sql);
		while (_results.hasNext()) {
			Map<String, Object> resultMap = _results.next();
			type = (String) resultMap.get("entity_type");
			break;
		}

		return type;
	}

	public List<UsedFqn> fillUsedFqnsDetails(List<HitFqnEntityId> apis,
			EntityCategory cat) {

		List<UsedFqn> usedFqns = new LinkedList<UsedFqn>();
		for (HitFqnEntityId api : apis) {
			usedFqns.add(fillUsedFqnDetails(api, cat));
		}

		return usedFqns;
	}

	/**
	 * 
	 * @param entityId
	 * @param usedTopLibApis
	 *            assuming sorted by count in desc order
	 * @param topKApis
	 * @return
	 */
	public String getSnippetForJarEntityHit(String entityId,
			List<UsedFqn> usedTopLibApis, int topKApis) {

		StringBuffer buf = new StringBuffer();

		for (String s : getSnippetsForJarEntityHit(entityId, usedTopLibApis,
				topKApis)) {
			buf.append("\n\n    ...\n\n");
			buf.append(s);
		}

		return buf.toString();
	}

	/**
	 * 
	 * @param entityId
	 * @param usedTopLibApis
	 *            assuming sorted by count in desc order
	 * @param topKApis
	 * @return
	 */
	public List<String> getSnippetsForJarEntityHit(String entityId,
			List<UsedFqn> usedTopLibApis, int topKApis) {

		List<String> returnVal = new LinkedList<String>();

		if (usedTopLibApis == null)
			return returnVal;
		else if (usedTopLibApis.size() == 0)
			return returnVal;

		List<String> topClasses = new LinkedList<String>();
		List<String> topInterfaces = new LinkedList<String>();
		List<String> topMethods = new LinkedList<String>();
		List<String> topConstructors = new LinkedList<String>();
		List<String> topFields = new LinkedList<String>();
		List<String> topOthers = new LinkedList<String>();

		int _CLcount = 0;
		int _INcount = 0;
		int _MEcount = 0;
		int _FIcount = 0;
		int _COcount = 0;
		int _OTcount = 0;

		for (UsedFqn usedFqn : usedTopLibApis) {

			switch (usedFqn.getType()) {

			case CLASS:
				if (_CLcount < topKApis / 2) {
					for (Long cid : usedFqn.getEntityIdsMatchingFqns())
						topClasses.add(cid + "");
				}
				_CLcount++;
				break;

			case INTERFACE:
				if (_INcount < topKApis / 2) {
					for (Long iid : usedFqn.getEntityIdsMatchingFqns())
						topInterfaces.add(iid + "");
				}
				_INcount++;
				break;

			case METHOD:
				if (_MEcount < topKApis) {
					for (Long mid : usedFqn.getEntityIdsMatchingFqns())
						topMethods.add(mid + "");
				}
				_MEcount++;
				break;

			case CONSTRUCTOR:
				if (_COcount < topKApis / 2) {
					for (Long coid : usedFqn.getEntityIdsMatchingFqns())
						topConstructors.add(coid + "");
				}
				_COcount++;
				break;

			case FIELD:
				// if (_FIcount < topKApis) {
				// for (Long tid : usedFqn.getEntityIdsMatchingFqns())
				// topFields.add(tid + "");
				// }
				// _FIcount++;
				break;

			default:
				// if (_OTcount < topKApis) {
				// for (Long oid : usedFqn.getEntityIdsMatchingFqns())
				// topOthers.add(oid + "");
				// }
				// _OTcount++;
				break;
			}

			// if( all_counts >=topKApis)
			// break;

		}

		List<String> _topUsedEids = new LinkedList<String>();

		StringBuffer _sbufTopUsedEntities = new StringBuffer();
		_sbufTopUsedEntities.append("(");

		for (String cl : topClasses) {
			_topUsedEids.add(cl);
		}

		for (String in : topInterfaces) {
			_topUsedEids.add(in);
		}

		for (String me : topMethods) {
			_topUsedEids.add(me);
		}

		// for(String fi: topFields){
		// 				_topUsedEids.add(fi);
		// }

		for (String co : topConstructors) {
			_topUsedEids.add(co);
		}



		// no used apis
		if (_topUsedEids.size() < 1)
			return returnVal;

		SortedMap<Long, Long> _offsetLengths = new TreeMap<Long, Long>();
		// only include a rationale-comment once (ie only one instance of usage)
		SortedMap<Long, Set<String>> _offsetComments = new TreeMap<Long, Set<String>>();
		String classFileId = "";

		for (String _usedEid : _topUsedEids) {

			String _sql = "select jr.offset, jr.length, jr.file_id, jr.relation_type, used_je.fqn from relations as jr "
					+ " inner join entities as used_je on jr.rhs_eid=used_je.entity_id "
					+ " where jr.lhs_eid="
					+ entityId
					+ " and jr.rhs_eid is not null and jr.length is not null and jr.offset is not null "
					+ " and internal=0 " 
					+ " and jr.rhs_eid=" + _usedEid
			;

			Iterator<Map<String, Object>> _results = dataSource.getData(_sql);

			while (_results.hasNext()) {
				Map<String, Object> resultMap = _results.next();

				Long _off = (Long) resultMap.get("offset");
				Long _length = (Long) resultMap.get("length");
				String _relation = (String) resultMap.get("relation_type");
				String _usedFqn = (String) resultMap.get("fqn");

				if (_offsetLengths.containsKey(_off)) {
					if (_offsetLengths.get(_off).longValue() > _length) {
						_offsetLengths.put(_off, _length);
					}
					_offsetComments.get(_off).add(
							"/// " + _relation + " " + _usedFqn); // TODO
					// remove
					// hard-coded
					// markup

				} else {

					_offsetLengths.put(_off, _length);
					_offsetComments.put(_off, new HashSet<String>());
					_offsetComments.get(_off).add(
							"/// " + _relation + " " + _usedFqn);
				}

				String _cfid = ((BigInteger) resultMap.get("file_id"))
						.toString()
						+ "";
				if (_cfid.length() > 0)
					classFileId = _cfid;

			}

		}

		// System.err.println(classFileId);
		// this entity is not using any top api
		if (classFileId.equals(""))
			return returnVal;
		String jarClassFileContent = getClassFileIdForJarEntity(classFileId);

		return getSnippetFromSourceString(jarClassFileContent, _offsetLengths,
				_offsetComments);
	}

	class SnippetPart {
		String line;
		Set<String> comments;
		Long offset;

		public SnippetPart(String line, Set<String> comments, Long offset) {
			this.line = line;
			this.comments = comments;
			this.offset = offset;
		}

		public void addAllComments(Set<String> comments2) {
			comments.addAll(comments2);
		}

	}

	class SC implements Comparator {

		Map map;

		public SC(Map m) {
			this.map = m;
		}

		public int compare(Object obj1, Object obj2) {

			SnippetPart o1 = (SnippetPart) map.get((String) obj1);
			SnippetPart o2 = (SnippetPart) map.get((String) obj2);

			if (o1.offset > o2.offset)
				return 1;
			else if (o1.offset < o2.offset)
				return -1;
			else
				return 0;
		}

	}

	/**
	 * return string that has lines of code extracted from jarClassFileContent
	 * (String representation of jar class file), based on list of
	 * offset,lengths
	 * 
	 * @param jarClassFileContent
	 * @param offsetLengths
	 *            sorted on offset
	 * @param offsetComments
	 * @return
	 */
	private List<String> getSnippetFromSourceString(String jarClassFileContent,
			SortedMap<Long, Long> offsetLengths,
			SortedMap<Long, Set<String>> offsetComments) {

		List<String> retVal = new LinkedList<String>();
		Map<String, SnippetPart> commentedLines = new HashMap<String, SnippetPart>();
		Set<Set<String>> _commentsSoFar = new HashSet<Set<String>>();

		for (Long off : offsetLengths.keySet()) {
			String line = getLine(jarClassFileContent, off.intValue(),
					offsetLengths.get(off).intValue()).trim();

			if (!commentedLines.containsKey(line)) {
				commentedLines.put(line, new SnippetPart(line,
						new HashSet<String>(), off));
			}
			commentedLines.get(line).addAllComments(offsetComments.get(off));
		}

		Map<String, SnippetPart> sortedCommentedLines = new TreeMap<String, SnippetPart>(
				new SC(commentedLines));
		sortedCommentedLines.putAll(commentedLines);

		for (String line : sortedCommentedLines.keySet()) {
			StringBuffer snippetLines = new StringBuffer();

			if (_commentsSoFar
					.contains(sortedCommentedLines.get(line).comments)) {
				continue;
			}

			_commentsSoFar.add(sortedCommentedLines.get(line).comments);

			snippetLines
					.append(stringSetToLines(sortedCommentedLines.get(line).comments));
			snippetLines.append(line);
			retVal.add(snippetLines.toString());
		}

		return retVal;
	}

	private String stringSetToLines(Set<String> stringSet) {
		StringBuffer _sbuf = new StringBuffer();
		for (String s : stringSet) {
			_sbuf.append(s);
			_sbuf.append("\n");
		}
		return _sbuf.toString();
	}

	private String getLine(String jarClassFileContent, int offset, int length) {

		int start = offset;
		int end = offset + length;

		int pointer = start;

		while (true) {
			if (pointer <= 0)
				break;
			if (pointer >= jarClassFileContent.length() - 1) {
				pointer = jarClassFileContent.length() - 1;
				break;
			}
			if (jarClassFileContent.charAt(pointer) == ';'
			/*
			 * || jarClassFileContent.charAt(pointer) == '\n' ||
			 * jarClassFileContent.charAt(pointer) == '\r'
			 */
			) {
				pointer = pointer + 1;
				break;
			} else {
				pointer = pointer - 1;
			}
		}

		start = pointer;
		pointer = end;

		while (true) {
			if (pointer >= jarClassFileContent.length())
				break;

			if (jarClassFileContent.charAt(pointer) == ';'
			/*
			 * || jarClassFileContent.charAt(pointer) == '\n' ||
			 * jarClassFileContent.charAt(pointer) == '\r'
			 */
			) {
				pointer = pointer + 1;
				break;
			} else {
				pointer = pointer + 1;
			}
		}
		end = pointer;

		// prevent AIOB
		if (start < 0)
			start = 0;
		if (end > jarClassFileContent.length() - 1)
			end = jarClassFileContent.length() - 1;

		return jarClassFileContent.substring(start, end);

	}

	public static String getClassFileIdForJarEntity(String jarClassFileID) {
		return SourcererSearchAdapter.getJarClassFileCodeRaw(jarClassFileID);
	}

	/**
	 * For now this only gives used jar entities
	 * 
	 * @param entityHits
	 * @return
	 */
	public List<UsedFqn> getTopUsedEntitiesForJarEntityHit(
			List<HitFqnEntityId> entityHits) {

		StringBuffer sbufUserJeids = new StringBuffer();
		sbufUserJeids.append("(");
		for (HitFqnEntityId hit : entityHits) {
			sbufUserJeids.append(hit.entityId);
			sbufUserJeids.append(",");
		}
		sbufUserJeids.append(")");
		String userJeids = sbufUserJeids.toString().replaceFirst(",\\)", ")");

		String sqlUsedJarEntities = "select provider_je.entity_id as pjeid, provider_je.fqn as pfqn, provider_je.entity_type as etype, jr.lhs_eid as ujeid "
				+ " from relations as jr inner join entities as provider_je on "
				+ " jr.rhs_eid=provider_je.entity_id "
				+ " and jr.relation_type in ('CALLS','EXTENDS','IMPLEMENTS','INSTANTIATES','USES', 'OVERRIDES')"
				+ " and internal=0 "
				+ " and jr.lhs_eid in" + userJeids;

		// System.err.println(sqlUsedJarEntities);
		if (userJeids.equals("()")) {
			return null;
		}

		Iterator<Map<String, Object>> _results = dataSource
				.getData(sqlUsedJarEntities);

		HashMap<String, UsedFqn> usedFqns = new HashMap<String, UsedFqn>();
		HashMap<String, Set<Long>> fqnsUserIds = new HashMap<String, Set<Long>>();

		while (_results.hasNext()) {
			Map<String, Object> resultMap = _results.next();

			Long providerJeid = new Long(((BigInteger) resultMap.get("pjeid"))
					.longValue());
			Long userJeid = new Long(((BigInteger) resultMap.get("ujeid"))
					.longValue());
			String providerFqn = (String) resultMap.get("pfqn");
			String etype = (String) resultMap.get("etype");

			if (fqnsUserIds.containsKey(providerFqn)) {
				fqnsUserIds.get(providerFqn).add(userJeid);
			} else {
				HashSet<Long> userIdSet = new HashSet<Long>();
				userIdSet.add(userJeid);
				fqnsUserIds.put(providerFqn, userIdSet);
			}

			if (usedFqns.containsKey(providerFqn)) {
				usedFqns.get(providerFqn).addEntityId(providerJeid);
			} else {
				UsedFqn usedFqn = new UsedFqn(providerFqn, EntityCategory.LIB,
						1);
				usedFqn.addEntityId(providerJeid);
				usedFqns.put(providerFqn, usedFqn);
			}

			setEntityType(usedFqns.get(providerFqn), etype);
			usedFqns.get(providerFqn).setUseCount(
					fqnsUserIds.get(providerFqn).size());

		}

		LinkedList<UsedFqn> retVal = new LinkedList<UsedFqn>();

		for (UsedFqn ufqn : usedFqns.values()) {
			retVal.add(ufqn);
			if (ufqn.getType().equals(EntityType.UNKNOWN) /*
														 * ||
														 * ufqn.getType().equals
														 * (EntityType.OTHER)
														 */) {
				ufqn.setType(EntityType
						.valueOf(lookupJarEntityTypeForUnknown(ufqn.getFqn())));
			}
		}

		Collections.sort(retVal, new UsedFqnValueComparator<UsedFqn>());

		return retVal;
	}

	class UsedFqnValueComparator<Usefqn> implements Comparator<UsedFqn> {

		public int compare(UsedFqn a, UsedFqn b) {

			if (a.getUseCount() < b.getUseCount()) {
				return 1;
			} else if (a.getUseCount() == b.getUseCount()) {
				return 0;
			} else {
				return -1;
			}
		}
	}

}
