package ast.handlers;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;

public class FieldAccessVisitor extends ASTVisitor {
	List<FieldAccess> fieldAccesses = new ArrayList<FieldAccess>();

	@Override
	public boolean visit(FieldAccess node) {
		fieldAccesses.add(node);
		return super.visit(node);
	}

	public List<FieldAccess> getFieldAccesses() {
		return fieldAccesses;
	}
}