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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.AbstractCvsTask;

/**
 * @author <a href="bajracharya@gmail.com">Sushil Bajracharya</a>
 * @created Jan 12, 2009
 *
 */
public class CvsSourceRetriever extends AbstractScmSourceRetriever {

	protected void checkout(String sourceRetrieveExpression, String projectFolder) {
		
		//BuildListener antLogListener = new AntLogListener(projectFolder + File.separator + Constants.getCvsTaskLogFileName());
		
		/**
		 * TODO so far this works with java.net cvs links
		 * 		make the parsing of sourceRetrieveExpression more robust
		 * 
		 */
		
		// guest account works with java.net
		// cvs -d :pserver:guest@cvs.dev.java.net:/cvs login;cvs -d :pserver:guest@cvs.dev.java.net:/cvs checkout ss74j
		
		File _fileCoOut = new File(projectFolder + File.separator +  Constants.getCvsCoOutputFileName());
		File _fileStatOut = new File(projectFolder + File.separator + Constants.getCvsStatOutputFileName());
		File _fileError = new File(projectFolder  + File.separator + Constants.getCvsErrorFileName());
		
		String[] _exprs = sourceRetrieveExpression.split(";");
		
		String[] _moduleExpr = _exprs[1].trim().split("\\s");
		
		String _root = null;
		
		// TODO handle different repositories gracefully
		// works with sourceforge
		if(sourceRetrieveExpression.indexOf("cvs.sourceforge.net") > -1)
			_root = _moduleExpr[2].replaceAll("-d:pserver:", ":pserver:");
		else // works with javanet
			_root = _moduleExpr[2].replaceAll(":pserver:username@", ":pserver:guest@");
				
		
		String _module = _moduleExpr[ _moduleExpr.length - 1 ];
		
		// if sourceforge extract projectname and use it as the modulename 
		if(sourceRetrieveExpression.indexOf("cvs.sourceforge.net") > -1)
			_module = _moduleExpr[2].substring(_moduleExpr[2].lastIndexOf("/") + 1, _moduleExpr[2].length());
		
		File dest = new File(this.getCheckoutFolder());
		
		// check out
		CvsTask cvsco = new CvsTask();
		// cvsco.getProject().addBuildListener(antLogListener);
		cvsco.setCvsRoot(_root);
		cvsco.setPackage(_module);
		cvsco.setDest(dest);
		cvsco.setError(_fileError);
		cvsco.setAppend(true);
		cvsco.setQuiet(true);
		cvsco.setCommand("checkout");
		
		cvsco.setOutput(_fileCoOut);
		cvsco.execute();
		
		// status
		CvsTask cvsstat = new CvsTask();
		// cvsco.getProject().addBuildListener(antLogListener);
		cvsstat.setCvsRoot(_root);
		cvsstat.setPackage(_module);
		cvsstat.setDest(dest);
		cvsstat.setError(_fileError);
		cvsstat.setAppend(true);
		cvsstat.setQuiet(true);
		cvsstat.setCommand("status");
		cvsstat.setOutput(_fileStatOut);
		cvsstat.execute();
		
		/// other CVS command you could run
		// cvsco.setCommand("rannotate");
		// cvsco.setCommand("ls -e " + _module);
		// cvsco.setCommand("log -bh");
		// cvsco.setCommand("rlog -bh");
		/// history does not work
		// cvsco.setCommand("history -al -m " + _module);
		
	}
	
	class CvsTask extends AbstractCvsTask{
		public CvsTask() {
			setProject(new Project());
			getProject().init();
		    setTaskType("cvscommand");
		    setTaskName("cvscommand");
		    setOwningTarget(new Target());
		}
	}

}
