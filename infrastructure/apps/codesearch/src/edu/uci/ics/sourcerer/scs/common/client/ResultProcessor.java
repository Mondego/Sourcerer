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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;


/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jul 31, 2009
 */
public class ResultProcessor {
	
	public static ArrayList<String> getHitEidsFromHits(String xmlResultInString){
		ArrayList<String> _hitEids = new ArrayList<String>();
		Document doc = XMLParser.parse(xmlResultInString);
		NodeList hits = doc.getElementsByTagName("doc");
		
		for (int i = 0; i < hits.getLength(); i++) {
			Node hit = hits.item(i);

			NodeList hitDocChildNodes = hit.getChildNodes();

			
			String entity_id = "";

			for (int j = 0; j < hitDocChildNodes.getLength(); j++) {
				Node hitDocChildNode = hitDocChildNodes.item(j);

				NamedNodeMap attrs = hitDocChildNode.getAttributes();
				Node _attributeNode = null;
				_attributeNode = attrs.getNamedItem("name");

				if (_attributeNode != null) {
					if (_attributeNode.getNodeValue().equals("entity_id")) {
						entity_id = hitDocChildNode.getFirstChild()
								.getNodeValue();
					} 
				}
			}

			_hitEids.add(entity_id);

		}
		
		return _hitEids;
	}
	
	public static ArrayList<String> getUsedFqnFromHits(String xmlResultInString) {
		ArrayList<String> usedFqns = new ArrayList<String>();

		Document doc = XMLParser.parse(xmlResultInString);
		NodeList _arrs = doc.getElementsByTagName("arr");

		for (int i = 0; i < _arrs.getLength(); i++) {
			Node _arr = _arrs.item(i);
			NamedNodeMap _nnm = _arr.getAttributes();
			if (_nnm != null) {
				Node _nameNode = _nnm.getNamedItem("name");
				if (_nameNode != null) {
					String _nodeType = _nameNode.getNodeValue();
					if (_nodeType.equals("lib_use_fqn_full")
							|| _nodeType.equals("jdk_use_fqn_full")
							|| _nodeType.equals("local_use_fqn_full")) {
						NodeList _fqns = _arr.getChildNodes();
						for (int j = 0; j < _fqns.getLength(); j++) {
							Node _fqn = _fqns.item(j);
							if (_fqn.getNodeName().equals("str")) {
								String _usedFqn = _fqn.getFirstChild()
										.getNodeValue();
								usedFqns.add(_usedFqn);
							}
						}
					}
				}
			}
		}
		return usedFqns;
	}

	public static List<HitFqnEntityId> getUsedApisFromFacetedHits(String xmlResultInString, EntityCategory c){
		
//		if (c.equals(EntityCategory.LOCAL)) 
//			throw new IllegalArgumentException("only jdk or lib");
		
		List<HitFqnEntityId> apis = new LinkedList<HitFqnEntityId>();

		Document doc = XMLParser.parse(xmlResultInString);
		NodeList apisNodes = doc.getElementsByTagName("lst");
		
		String nodeName;
		
		if(c.equals(EntityCategory.JDK))
			nodeName = "jdk_use_fqn_full";
		else if(c.equals(EntityCategory.LIB))
			nodeName = "lib_use_fqn_full";
		else return apis;
		
		for (int i = 0; i < apisNodes.getLength(); i++){
			Node apiNode = apisNodes.item(i);
			NamedNodeMap attrs = apiNode.getAttributes();
			Node _attributeNode = null;
			_attributeNode = attrs.getNamedItem("name");

			if (_attributeNode != null) {
				if (_attributeNode.getNodeValue().equals(nodeName)) {
					// iterate through int nodes
					NodeList fqnNodes = apiNode.getChildNodes();
					
					for(int j = 0; j < fqnNodes.getLength(); j++){
						Node fqnNode = fqnNodes.item(j);
						if(fqnNode.getNodeName().equals("int")){
							String fqn = getAttributeValue(fqnNode, "name");
							if (fqn!=null){
								String count = fqnNode.getFirstChild().getNodeValue();
								
								HitFqnEntityId _apiFqn = new HitFqnEntityId(fqn, "-1", count);
								apis.add(_apiFqn);
								
							}
						}
					}
					
					
				}
			}
		}
		
		return apis;
	}
	
	private static String getAttributeValue(Node node, String attributeName){
		NamedNodeMap attrs = node.getAttributes();
		Node _attributeNode = null;
		_attributeNode = attrs.getNamedItem("name");
		if (_attributeNode == null) return null;
		else
			return _attributeNode.getNodeValue();
			
	}
	
	public static List<HitFqnEntityId> getFqnEntityIdFromHits(String xmlResultInString) {

		List<HitFqnEntityId> hitsInfo = new LinkedList<HitFqnEntityId>();
		
		Node responseNode = getResponseNode(xmlResultInString);
		NodeList hits = responseNode.getChildNodes(); 
		
		for (int i = 0; i < hits.getLength(); i++) {
			String fqn = "";
			String entity_id = "";
			
			Node hit = hits.item(i);
			if(!hit.getNodeName().equals("doc")) continue;
			
			NodeList hitDocChildNodes = hit.getChildNodes();

			for (int j = 0; j < hitDocChildNodes.getLength(); j++) {
				Node hitDocChildNode = hitDocChildNodes.item(j);

				NamedNodeMap attrs = hitDocChildNode.getAttributes();
				Node _attributeNode = null;
				_attributeNode = attrs.getNamedItem("name");

				if (_attributeNode != null) {
					if (_attributeNode.getNodeValue().equals("entity_id")) {
						entity_id = hitDocChildNode.getFirstChild()
								.getNodeValue();
					} else if (_attributeNode.getNodeValue().equals("fqn_full")) {
						fqn = hitDocChildNode.getFirstChild().getNodeValue();
					}
				}
			}
			
			hitsInfo.add(new HitFqnEntityId(fqn, entity_id));
			
		}
		return hitsInfo;
	}

	public static HitsStat getStatsFromHits(String xmlResultInString) {
		long start = 0;
		long numResults = 0;
		double timeTaken = 0;

		
		Node responseNode = getResponseNode(xmlResultInString);
		
		if (responseNode == null) return null;
		
		NamedNodeMap attrs = responseNode.getAttributes();
		Node _numFoundNode = attrs.getNamedItem("numFound");
		Node _startNode = attrs.getNamedItem("start");

		if (_numFoundNode != null) {
			numResults = Long.parseLong(_numFoundNode
					.getNodeValue());
		}

		if (_startNode != null) {
			start = Long.parseLong(_startNode.getNodeValue());
		}
		
		return new HitsStat(start, numResults, timeTaken);
	}
	
	private static Node getResponseNode(String xmlResultInString){
	
		Document doc = XMLParser.parse(xmlResultInString);
		NodeList results = doc.getElementsByTagName("result");

		for (int i = 0; i < results.getLength(); i++) {
			Node result = results.item(i);
			NamedNodeMap attrs = result.getAttributes();
			Node _attributeNode = null;
			_attributeNode = attrs.getNamedItem("name");

			if (_attributeNode != null) {
				if (_attributeNode.getNodeValue().equals("response")) {
					
					return result;
				
				}
			}
		}
	
		return null;
	
	}
	
	

}
