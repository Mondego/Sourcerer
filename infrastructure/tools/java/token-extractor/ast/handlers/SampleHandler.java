package ast.handlers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
//import org.eclipse.jface.text.Document;

public class SampleHandler extends AbstractHandler {
	static int count;
	

	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		FileWriter fstream = null;
		try 
		{
			fstream = new FileWriter ("/Users/admin/Documents/workspace/AST/output/clone-apachecommons-new1.txt");
		}
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
		PrintWriter out = new PrintWriter(fstream);
		
		
		// Get the root of the workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		// Get all projects in the workspace
		IProject[] projects = root.getProjects();
		// Loop over all projects
		for (IProject project : projects) {
			try {
				printProjectInfo(project,out);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private void printProjectInfo(IProject project,  PrintWriter out) throws CoreException,
			JavaModelException {
		//System.out.println("Working in project " + project.getName());
		// Check if we have a Java project
		//System.out.print(project.getName()+":");
		if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
			IJavaProject javaProject = JavaCore.create(project);
			printPackageInfos(javaProject,out);
		}
	}

	private void printPackageInfos(IJavaProject javaProject,  PrintWriter out)
			throws JavaModelException {
		IPackageFragment[] packages = javaProject.getPackageFragments();
		for (IPackageFragment mypackage : packages) {
			// Package fragments include all packages in the
			// classpath
			// We will only look at the package from the source
			// folder
			// K_BINARY would include also included JARS, e.g.
			// rt.jar
			if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
				//System.out.println("Package " + mypackage.getElementName());
				//System.out.print(mypackage.getElementName()+":");
				printICompilationUnitInfo(mypackage, out);

			}

		}
	}

	private void printICompilationUnitInfo(IPackageFragment mypackage,  PrintWriter out)
			throws JavaModelException {
		for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			//System.out.println("Source file " + unit.getElementName());
			//System.out.print(unit.getElementName()+":");
			//Document doc = new Document(unit.getSource());
			//System.out.println("Has number of lines: " + doc.getNumberOfLines());
			printIMethods(unit,out);

		}
	}

	public static boolean isNotNullNotEmptyNotWhiteSpaceOnlyByJava( final String string)  
	{  
			   return string != null && !string.isEmpty() && !string.trim().isEmpty();  
	} 
	
	private void printIMethods(ICompilationUnit unit,PrintWriter out) throws JavaModelException {
		IType[] allTypes = unit.getAllTypes();
		for (IType type : allTypes) {
			IMethod[] methods = type.getMethods();
			for (IMethod method : methods) {

				//System.out.println("Method name " + method.getElementName());
				String methodBody = method.getSource();
				methodBody = methodBody.replaceAll("\\r\\n|\\r|\\n", " ");
				//String truncatedmethodBody = methodBody.replaceAll(", replacement)
				System.out.println(methodBody);
				//methodBody.
				if(isNotNullNotEmptyNotWhiteSpaceOnlyByJava(methodBody))
				{	
					out.print(count+":");
					out.print(method.getElementName()+":");
					out.print(methodBody);
					out.println();
					count++;
				}	
				//System.out.println("Signature " + method.getSignature());
				//System.out.println("Return Type " + method.getReturnType());

			}
			
		}
	}
}
