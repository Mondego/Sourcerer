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
package edu.uci.ics.sourcerer.evalsnippets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import edu.uci.ics.sourcerer.db.adapter.JdbcDataSource;
import edu.uci.ics.sourcerer.db.adapter.SourcererDbAdapter;
import edu.uci.ics.sourcerer.evalsnippets.server.SnippetServiceAdapter;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 9, 2010
 * 
 */
public class EvalResultsFetcher {

	public static String OP = "/Users/shoeseal/sandbox/Sourcerer/infrastructure/apps/codesearch/test/output"; 
	public static String RANKING_FILE = OP + File.separatorChar + "ranking.txt";
	public static String JUDGEMENT_FILE = OP + File.separatorChar + "judgement.txt";
	
	RelevancyMap relevancyMap = new RelevancyMap();
	DocMap docMap = new DocMap();
	RankedResults rankedResult = new RankedResults();

	Map<String, String> qidEidSchemeid2DocIdMap = new HashMap<String, String>();

	Map<String, String> judgements;

	public void fetch() {

		judgements = EvalSnippetsData.openJudgementFile();

		for (String query : EvalSnippetsData.queries.keySet()) {

			System.out.println(query);

			String queryId = EvalSnippetsData.queries.get(query);

			// <eid : <scheme: {snippet, rank}>>
			Map<String, Map<String, String[]>> results = SnippetServiceAdapter
					.getSnippetsForAllSchemesWithRank(query, dba);


			// build results, doc map
			for (String entityId : results.keySet()) {
				for (String scheme : results.get(entityId).keySet()) {
					String[] snippetRank = results.get(entityId).get(scheme);

					String _queryId = queryId;
					String _docId = MD5(snippetRank[0]);
					String _rank = snippetRank[1];
					String _schemeId = EvalSnippetsData.schemes.get(scheme);
					String _snippet = snippetRank[0];
					String _entityId = entityId;

					if(_entityId.equals("6613569")){
						System.out.println("");;
					}
					
					rankedResult.addResult(_queryId, _docId, _rank, _schemeId);
					docMap.addDocument(_docId, _entityId, _snippet);
					// initially everything is not relevant
					relevancyMap.setRelevancy(_queryId, _docId, "NotRelevant");
					qidEidSchemeid2DocIdMap.put(_queryId + "!" + _entityId
							+ "!" + _schemeId, _docId);

					// System.out.println(_queryId + ", "
					// + _docId + ", "
					// + _rank + ", "
					// + _schemeId + ", "
					// + entityId);
				}
			}

			// enable to test for one query
			// break;
		}

	}

	public void updateRelevancy() {
		// update relevancy using judgement data
		for (String judgementKey : judgements.keySet()) {
			String[] keyParts = judgementKey.split("!");
			String qid = keyParts[0];
			String relevancy = judgements.get(judgementKey);

			if(!qid.equals("q1")){continue;}
			
			// System.out.println(judgementKey);
			if (relevancy.equals("Relevant")) {

				String docId = qidEidSchemeid2DocIdMap.get(judgementKey);

				if (docId == null) {

					// qid!eid!sid
					System.err.println(judgementKey);

					continue;
				}

				relevancyMap.setRelevancy(qid, docId, relevancy);
			}
		}
	}

	private static String MD5(String snippet) {

		String md5 = null;
		try {

			md5 = MD5Generator.get(snippet);

		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
			throw new RuntimeException("could not get MD5 for" + snippet);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("could not get MD5 for (eid" + snippet);
		}
		return md5;
	}

	public static void main(String[] args) {
		EvalResultsFetcher sp = new EvalResultsFetcher();
		try {
			sp.setUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		sp.fetch();
		sp.updateRelevancy();
		
//		sp.printRelevancy(System.out);
//		System.out.println("");
//		sp.printRanking(System.out);
		
		sp.writeJudgement();
		sp.writeRankings();
		
		System.out.println("DONE.");
	}

	public void printRelevancy(PrintStream out) {
		// out.println("");
		for (String response : relevancyMap.getAll().keySet()) {
			int _r = 0;
			if (relevancyMap.getRelevancy(response) == 1) {
				_r = 1;
			}

			String[] parts = response.split("!");
			String queryId = parts[0];
			String docId = parts[1];
			String queryNumber = queryId.replaceFirst("q", "");

			out.println(queryNumber + "\t" + queryId + "\t" + docId + "\t"
							+ _r);
		}
	}

	public void printRanking(PrintStream out) {

		for (String scheme : this.rankedResult.getResults().keySet()) {

			for (String query : this.rankedResult.getResults().get(scheme)
					.keySet()) {
				Set<Document> docs = this.rankedResult.getResults().get(scheme)
						.get(query);

				for (Document doc : docs) {

					String queryId = query;
					String queryNumber = query.replaceFirst("q", "");
					String docId = doc.docid;
					int rank = doc.rank;
					double score = doc.score;
					String schemeId = scheme;

					out.println(queryNumber + "\t" + queryId + "\t" + docId
							+ "\t" + rank + "\t" + score + "\t" + schemeId);
				}

			}

		}
		;

	}

	public void writeJudgement() {
		printRelevancy(getPrinter(JUDGEMENT_FILE));
		printRanking(getPrinter(RANKING_FILE));
	}

	public void writeRankings() {

	}

	public static PrintStream getPrinter(String file) {
		FileOutputStream out; 
		PrintStream p = null; 
		try { 
			out = new FileOutputStream(file); 
			p = new PrintStream(out);
		} catch (Exception e) {
			System.err.println("Error opening file to write " + file);
		}
		
		return p;

	}
	
	public static void closeStream(PrintStream p){
		p.close();
	}

	JdbcDataSource dataSource = new JdbcDataSource();
	SourcererDbAdapter dba = new SourcererDbAdapter();

	protected void setUp() throws Exception {

		Properties p = new Properties();
		p.put("driver", "com.mysql.jdbc.Driver");
		p.put("url", "jdbc:mysql://tagus.ics.uci.edu:3306/sourcerer_eclipse");
		p.put("user", System.getProperty( "sourcerer.db.user" ));
		p.put("password", System.getProperty( "sourcerer.db.password" ));
		dataSource.init(p);

		dba.setDataSource(dataSource);
	}

	// class Result{
	// public String query;
	// public String docId; // snippet's md5
	// public String rank;
	// public String scheme;
	// }

	class Document implements Comparable<Document> {
		public String docid;
		public int rank;
		public double score;

		public Document(String docId2, String rank2) {
			this.docid = docId2;
			this.rank = Integer.parseInt(rank2);
			this.score = (double) 1 / (double) this.rank;
		}

		public int compareTo(Document o) {
			if (o.rank == this.rank)
				return 0;

			// reverse sorted
			return (o.rank < this.rank) ? 1 : -1;
		}
	}

	class RankComparator implements Comparator<Document> {

		public int compare(Document o1, Document o2) {
			return o1.compareTo(o2);
		}

	}

	class RankedResults {

		// < scheme : < queryid : < document > >>
		HashMap<String, HashMap<String, Set<Document>>> results = new HashMap<String, HashMap<String, Set<Document>>>();

		public void addResult(String query, String docId, String rank,
				String scheme) {

			Document doc = new Document(docId, rank);

			HashMap<String, Set<Document>> queryResults = results.get(scheme);
			Set<Document> documents;

			if (queryResults == null) {
				queryResults = new HashMap<String, Set<Document>>();
				documents = new TreeSet<Document>(new RankComparator());

				documents.add(doc);
				queryResults.put(query, documents);
				results.put(scheme, queryResults);

			} else {
				documents = queryResults.get(query);

				if (documents == null) {
					documents = new HashSet<Document>();

					documents.add(doc);
					queryResults.put(query, documents);
				} else {

					documents.add(doc);
				}

			}

		}

		public HashMap<String, HashMap<String, Set<Document>>> getResults() {
			return results;
		}

	}

	class Snippet {
		public Snippet(String docId, String snippet2, String eid2) {
			this.md5 = docId;
			this.snippet = snippet2;
			this.eid = eid2;
		}

		public String md5;
		public String snippet;
		public String eid;
	}

	class DocMap {

		// <md5: Snippet>
		HashMap<String, Snippet> map = new HashMap<String, Snippet>();

		public String getSnippet(String docId) {
			return map.get(docId).snippet;
		}

		public String getEntityId(String docId) {
			return map.get(docId).eid;
		}

		public void addDocument(String snippet, String eid) {

			String docId = EvalResultsFetcher.MD5(snippet);
			map.put(docId, new Snippet(docId, snippet, eid));

		}

		public void addDocument(String docId, String eid, String snippet) {
			map.put(docId, new Snippet(docId, snippet, eid));
		}

	}

	enum RelevancyOption {
		Relevant, NotRelevant
	}

	class Response {
		String queryId;
		String docId;

		Response(String q, String d) {
			this.queryId = q;
			this.docId = d;
		}

		public boolean equals(Object other) {
			if (other instanceof Response) {
				if (this.queryId.equals(((Response) other).queryId)
						&& this.docId.equals(((Response) other).docId)) {
					return true;
				} else
					return false;
			} else

				return false;
		}

	}

	class RelevancyMap {

		// < "queryId!docId" : option >
		public Map<String, RelevancyOption> getAll() {
			return relevancyMap;
		}

		HashMap<String, RelevancyOption> relevancyMap = new HashMap<String, RelevancyOption>();

		public int getRelevancy(String response) {
			RelevancyOption rel = relevancyMap.get(response);
			if (rel.equals(RelevancyOption.Relevant))
				return 1;
			else
				return 0;
		}

		public void setRelevancy(String queryId, String docId, String relevancy) {
			RelevancyOption rel = RelevancyOption.valueOf(relevancy);
			String response = queryId + "!" + docId;
			relevancyMap.put(response, rel);

		}
	}

}
