package ast.handlers;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;



public class ClassVisitor extends ASTVisitor {
	List<TypeDeclaration> classes = new ArrayList<TypeDeclaration>();
	List<String> fields =  new ArrayList<String>();
	
	@Override
	public boolean visit(TypeDeclaration node) {
		classes.add(node);
		FieldDeclaration[] fieldDeclarations = node.getFields();
		
		for (FieldDeclaration fieldDeclaration : fieldDeclarations)
		{
			List<VariableDeclarationFragment> fragments = fieldDeclaration.fragments();
			for (VariableDeclarationFragment variableDeclFragment : fragments)
			{
				System.out.println("Visiting field :"+variableDeclFragment.getName());
				fields.add(variableDeclFragment.getName().toString());
			}
			
		}
		
		
		return super.visit(node);
	}

	public List<String> getFields()
	{
		return fields;
	}
	public List<TypeDeclaration> getClasses()
	{
		return classes;
	}
}