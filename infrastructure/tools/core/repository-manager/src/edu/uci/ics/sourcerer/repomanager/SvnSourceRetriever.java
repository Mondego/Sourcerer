///*
// * Sourcerer: An infrastructure for large-scale source code analysis.
// * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// * 
// */
//package edu.uci.ics.sourcerer.repomanager;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.net.MalformedURLException;
//import java.util.LinkedList;
//import java.util.Properties;
//
//import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
//import org.tigris.subversion.svnclientadapter.ISVNInfo;
//import org.tigris.subversion.svnclientadapter.SVNClientException;
//import org.tigris.subversion.svnclientadapter.SVNNodeKind;
//import org.tigris.subversion.svnclientadapter.SVNRevision;
//import org.tigris.subversion.svnclientadapter.SVNUrl;
//import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapter;
//import org.tigris.subversion.svnclientadapter.commandline.CmdLineNotificationHandler;
//
///**
// * 
// * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
// * @created Jan 12, 2009
// * 
// */
//public class SvnSourceRetriever extends AbstractScmSourceRetriever {
//
//	protected boolean checkout(String sourceRetrieveExpression, String projectFolder) {
//		
//		String _url = sourceRetrieveExpression.replaceAll("\\s+", " ").split("\\s")[2];
//		
//		ISVNInfo _info = null;
//		
//		CmdLineClientAdapter ca = new CmdLineClientAdapter(new CmdLineNotificationHandler());
//		
//		// optimistic about this credential working with svn servers
//		// exists on apache, java.net, sourceforge svn servers
//		ca.setUsername("guest");
//		ca.setPassword("guest");
//		
//		boolean errorInRootUrl = false;
//		// get svn info
//		try {
//			_info = ca.getInfo(new SVNUrl(_url));
//		} catch (MalformedURLException e) {
//			// TODO log
//			errorInRootUrl = true;
//			System.err.println("ERROR in svn url:" + projectFolder);
//			e.printStackTrace();
//		} catch (SVNClientException e) {
//			// TODO log
//			System.err.println("ERROR in svn url:" + projectFolder);
//			errorInRootUrl = true;
//			e.printStackTrace();
//		}
//		
//		// url picked by crawler failed
//		// try again with one step up in the url, if the url ends with trunk
//		if( (_url.trim().endsWith("/trunk/") || _url.trim().endsWith("/trunk")) &&
//				errorInRootUrl){
//			
//			errorInRootUrl = false;
//			
//			// fixes the non existent trunk in google code
//			// will use the root instead
//			_url = _url.replaceAll("/trunk[/]{0,1}+$", "");
//		
//			try {
//				_info = ca.getInfo(new SVNUrl(_url));
//			} catch (MalformedURLException e) {
//				// TODO log
//				errorInRootUrl = true;
//				System.err.println("ERROR in svn url, one step up:" + projectFolder);
//				e.printStackTrace();
//			} catch (SVNClientException e) {
//				// TODO log
//				System.err.println("ERROR in svn url, one step up:" + projectFolder);
//				errorInRootUrl = true;
//				e.printStackTrace();
//			}
//		
//		}
//		
//		if(errorInRootUrl) // assume all checkout failed
//			return false;
//		
//		Properties svninfoProperties = new Properties();
//		
//		// svninfoProperties.setProperty("copy.rev", _info.getCopyRev().toString());
//		// svninfoProperties.setProperty("copy.url", _info.getCopyUrl().toString());
//		
//		svninfoProperties.setProperty("last.changed.date", _info.getLastChangedDate().toString());
//		svninfoProperties.setProperty("last.changed.revision", _info.getLastChangedRevision().toString());
//		svninfoProperties.setProperty("last.commit.author", _info.getLastCommitAuthor());
//		
//		// svninfoProperties.setProperty("last.date.props.update", _info.getLastDatePropsUpdate().toString());
//		// svninfoProperties.setProperty("last.date.text.update", _info.getLastDateTextUpdate().toString());
//		
//		svninfoProperties.setProperty("node.kind", _info.getNodeKind().toString());
//		svninfoProperties.setProperty("repository", _info.getRepository().toString());
//		svninfoProperties.setProperty("revision", _info.getRevision().toString());
//		// item's url
//		svninfoProperties.setProperty("url", _info.getUrlString());
//		svninfoProperties.setProperty("uuid", _info.getUuid());
//		
//		LinkedList<String> trunks = new LinkedList<String>();
//		
//		
//		
//		if (_url.trim().endsWith("/trunk/") || _url.trim().endsWith("/trunk")) {
//			trunks.add(_url);
//		} else {
//			// checkout all trunks
//			try {
//				trunks = getTrunks(ca, _info.getRevision().toString(), _url,
//						projectFolder);
//			} catch (MalformedURLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (SVNClientException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} finally {
//				// else checkout the root url
//				if (trunks.size() == 0)
//					trunks.add(_url);
//			}
//		}
//		
//		boolean allCheckoutFailed = true;
//		int i = 0;
//		for(String trunk: trunks){
//			boolean createdCoFolder = new File(FileUtils.makePath(this.getCheckoutFolder(), Constants.getSvncoFolderNamePrefix() + i)).mkdir();
//			
//			if(createdCoFolder){
//				String svncoFolderName = this.getCheckoutFolder() + File.separator + Constants.getSvncoFolderNamePrefix() + i;
//				// checkout
//				try {
//					ca.checkout(new SVNUrl(trunk), new File(svncoFolderName), SVNRevision.HEAD, true);
//					// add this checkout folder into the properties file
//					svninfoProperties.put(Constants.getSvncoFolderNamePrefix() + i, trunk);
//					
//					// at least one of the checkouts worked
//					allCheckoutFailed = false;
//				} catch (MalformedURLException e) {
//					// TODO log
//					e.printStackTrace();
//				} catch (SVNClientException e) {
//					// TODO log
//					e.printStackTrace();
//				}
//			} else {
//				// TODO log
//				System.err.println("Cannot make directory: " + FileUtils.makePath(this.getCheckoutFolder(), Constants.getSvncoFolderNamePrefix() + i));
//			}
//			
//			i++;
//		}
//		
//		// write the properties file
//		//svninfoProperties.save(out, comments);
//		String _opF = projectFolder + File.separator + Constants.getSvnStatOutputFileName();
//		try {
//			svninfoProperties.store(new FileOutputStream(_opF), null);
//		} catch (FileNotFoundException e) {
//			// TODO log
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO log
//			e.printStackTrace();
//		}
//		
//		return !allCheckoutFailed;
//	}
//	
//	private LinkedList<String> getTrunks(CmdLineClientAdapter ca, String revision, String url, String projectFolder) throws MalformedURLException, SVNClientException{
//		long rev = Long.parseLong(revision);
//		
//		int depth = 1;
//		LinkedList<ISVNDirEntry> entries = getSvnEntries(ca, rev, url); 
//		LinkedList<String> currentLevelNodes = new LinkedList<String>();
//		for(ISVNDirEntry entry : entries){
//			currentLevelNodes.add(url 
//					+ (url.endsWith("/")?"":"/") 
//					+ entry.getPath());
//		}
//		LinkedList<String> trunks = getTrunksFromNodes(currentLevelNodes);
//		
//		depth = 2;
//		while (depth<=4) {
//			if (trunks.size() == 0) {
//				LinkedList<String> _parents = currentLevelNodes;
//				if(_parents.size()==0)
//					break;
//				currentLevelNodes = new LinkedList<String>(); 
//				for (String _url : _parents) {
//					LinkedList<ISVNDirEntry> _entries = getSvnEntries(ca, rev, _url);
//					for(ISVNDirEntry entry : _entries){
//						currentLevelNodes.add(_url 
//								+ (_url.endsWith("/")?"":"/") 
//								+ entry.getPath());
//					}
//				}
//				trunks = getTrunksFromNodes(currentLevelNodes);
//				depth++;
//			
//			} else {
//				break;
//			}
//		}
//		
//		return trunks;
//	}
//
//	/**
//	 * @param url
//	 * @return
//	 * @throws SVNClientException 
//	 * @throws MalformedURLException 
//	 */
//	private LinkedList<ISVNDirEntry> getSvnEntries(CmdLineClientAdapter ca, long rev, String url) throws MalformedURLException, SVNClientException {
//
//		ISVNDirEntry[] entries = ca.getList(new SVNUrl(url), new SVNRevision.Number(rev), false);
//		
//		LinkedList<ISVNDirEntry> dirs = new LinkedList<ISVNDirEntry>();
//		
//		for(ISVNDirEntry entry: entries){
//			if (entry.getNodeKind() == SVNNodeKind.DIR)
//				dirs.add(entry);
//		}
//		
//		return dirs;
//	}
//
//	/**
//	 * @param currentLevelNodes
//	 * @param trunks
//	 */
//	private LinkedList<String> getTrunksFromNodes(
//			LinkedList<String> currentLevelNodes) {
//		
//		LinkedList<String> trunks = new LinkedList<String>();
//		for(String entry: currentLevelNodes){
//			
//			if(entry.contains("trunk")) trunks.add(entry);
//		}
//		return trunks;
//		
//	}
//	
//}
