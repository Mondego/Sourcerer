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
package edu.uci.ics.sourcerer.repomanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapter;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineNotificationHandler;

/**
 * 
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 * 
 * TODO check why this is sometimes slow on 'svn info'
 * 		possibly replace this with svn ant task
 */
public class SvnSourceRetriever extends AbstractScmSourceRetriever {

	protected void checkout(String sourceRetrieveExpression, String projectFolder) {
		
		String _url = sourceRetrieveExpression.split("\\s")[2];
		
		ISVNInfo _info = null;
		
		CmdLineClientAdapter ca = new CmdLineClientAdapter(new CmdLineNotificationHandler());
		
		// optimistic about this credential working with svn servers
		// exists on apache, java.net, sourceforge svn servers
		ca.setUsername("guest");
		ca.setPassword("guest");
		
		try {
			ca.checkout(new SVNUrl(_url), new File(this.getCheckoutFolder()), SVNRevision.HEAD, true);
			_info = ca.getInfo(new SVNUrl(_url));
		} catch (MalformedURLException e) {
			// TODO log
			e.printStackTrace();
		} catch (SVNClientException e) {
			// TODO log
			e.printStackTrace();
		}
		
		Properties svninfoProperties = new Properties();
		
		// svninfoProperties.setProperty("copy.rev", _info.getCopyRev().toString());
		// svninfoProperties.setProperty("copy.url", _info.getCopyUrl().toString());
		
		svninfoProperties.setProperty("last.changed.date", _info.getLastChangedDate().toString());
		svninfoProperties.setProperty("last.changed.revision", _info.getLastChangedRevision().toString());
		svninfoProperties.setProperty("last.commit.author", _info.getLastCommitAuthor());
		
		// svninfoProperties.setProperty("last.date.props.update", _info.getLastDatePropsUpdate().toString());
		// svninfoProperties.setProperty("last.date.text.update", _info.getLastDateTextUpdate().toString());
		
		svninfoProperties.setProperty("node.kind", _info.getNodeKind().toString());
		svninfoProperties.setProperty("repository", _info.getRepository().toString());
		svninfoProperties.setProperty("revision", _info.getRevision().toString());
		// item's url
		svninfoProperties.setProperty("url", _info.getUrlString());
		svninfoProperties.setProperty("uuid", _info.getUuid());
		
		String _path = _info.getUrlString().replaceFirst(_info.getRepository().toString(), "");
		svninfoProperties.setProperty("path", _path);
		
		// write the properties file
		//svninfoProperties.save(out, comments);
		String _opF = projectFolder + File.separator + Constants.getSvnStatOutputFileName();
		try {
			svninfoProperties.store(new FileOutputStream(_opF), null);
		} catch (FileNotFoundException e) {
			// TODO log
			e.printStackTrace();
		} catch (IOException e) {
			// TODO log
			e.printStackTrace();
		}
		
	}
}
