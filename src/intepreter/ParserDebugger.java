package intepreter;

import parser.ast.*;

public class ParserDebugger implements JlangVisitor {
    private int indent = 0;

    private String indentString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < indent; ++i) {
            sb.append(" ");
        }
        return sb.toString();
    }

    /** Debugging dump of a node. */
    private Object dump(SimpleNode node, Object data) {
        System.out.println(indentString() + node);
        ++indent;
        data = node.childrenAccept(this, data);
        --indent;
        return data;
    }

    public Object visit(SimpleNode node, Object data) {
        System.out.println(node + ": acceptor not implemented in subclass?");
        return data;
    }

    // Execute a Sili program
    public Object visit(ASTCode node, Object data) {
        dump(node, data);
        return data;
    }

    // Execute a statement
    public Object visit(ASTStatement node, Object data) {
        dump(node, data);
        return data;
    }

    // Execute a block
    public Object visit(ASTBlock node, Object data) {
        dump(node, data);
        return data;
    }

    // Execute an IF
    public Object visit(ASTIfStatement node, Object data) {
        dump(node, data);
        return data;
    }

    // Function definition parameter list
    public Object visit(ASTParmlist node, Object data) {
        dump(node, data);
        return data;
    }

    // Function body
    public Object visit(ASTFnBody node, Object data) {
        dump(node, data);
        return data;
    }

    // Function definition
    public Object visit(ASTFnDef node, Object data) {
        dump(node, data);
        return data;
    }

    // Function return expression
    public Object visit(ASTReturnExpression node, Object data) {
        dump(node, data);
        return data;
    }

    // Function argument list
    public Object visit(ASTArgList node, Object data) {
        dump(node, data);
        return data;
    }

    // Function call
    public Object visit(ASTCall node, Object data) {
        dump(node, data);
        return data;
    }

    // Function invocation in an expression
    public Object visit(ASTFnInvoke node, Object data) {
        dump(node, data);
        return data;
    }

    // Dereference a variable, and push its value onto the stack
    public Object visit(ASTDereference node, Object data) {
        dump(node, data);
        return data;
    }

    // Execute a FOR loop
    public Object visit(ASTForLoop node, Object data) {
        dump(node, data);
        return data;
    }
    
    // Execute a FOR loop
    public Object visit(ASTWhileLoop node, Object data) {
        dump(node, data);
        return data;
    }
    
    //Switch statement debugger
    public Object visit(ASTSwitchStatement node, Object data) {
    	dump(node, data);
    	return data;
    }
    
    //Array debugger
    public Object visit(ASTArray node, Object data) {
    	dump(node, data);
    	return data;
    }

    // Process an identifier
    // This doesn't do anything, but needs to be here because we need an ASTIdentifier node.
    public Object visit(ASTIdentifier node, Object data) {
        dump(node, data);
        return data;
    }

    // Execute the WRITE statement
    public Object visit(ASTWrite node, Object data) {
        dump(node, data);
        return data;
    }

    // Execute an assignment statement, by popping a value off the stack and assigning it
    // to a variable.
    public Object visit(ASTAssignment node, Object data) {
        dump(node, data);
        return data;
    }
    
    public Object visit(ASTAssignmentInteger node, Object data) {
        dump(node, data);
        return data;
    }
    
    public Object visit(ASTAssignmentString node, Object data) {
        dump(node, data);
        return data;
    }
    
    public Object visit(ASTAssignmentFloat node, Object data) {
        dump(node, data);
        return data;
    }
    
    public Object visit(ASTAssignmentBoolean node, Object data) {
        dump(node, data);
        return data;
    }
    
    // OR
    public Object visit(ASTOrExpression node, Object data) {
        dump(node, data);
        return data;
    }

    // AND
    public Object visit(ASTAndExpression node, Object data) {
        dump(node, data);
        return data;
    }

    // ==
    public Object visit(ASTComparisonEqual node, Object data) {
        dump(node, data);
        return data;
    }

    // !=
    public Object visit(ASTComparisonNotEqualTo node, Object data) {
        dump(node, data);
        return data;
    }

    // >=
    public Object visit(ASTComparisonGreatorThanOrEqualTo node, Object data) {
        dump(node, data);
        return data;
    }

    // <=
    public Object visit(ASTComparisonLessThanOrEqualTo node, Object data) {
        dump(node, data);
        return data;
    }

    // >
    public Object visit(ASTComparisonGreatorThan node, Object data) {
        dump(node, data);
        return data;
    }

    // <
    public Object visit(ASTComparisonLessThan node, Object data) {
        dump(node, data);
        return data;
    }

    // +
    public Object visit(ASTAddOperator node, Object data) {
        dump(node, data);
        return data;
    }

    // -
    public Object visit(ASTSubtractOperator node, Object data) {
        dump(node, data);
        return data;
    }

    // *
    public Object visit(ASTTimesOperator node, Object data) {
        dump(node, data);
        return data;
    }

    // /
    public Object visit(ASTDivideOperator node, Object data) {
        dump(node, data);
        return data;
    }

    // NOT
    public Object visit(ASTUnaryNotOperator node, Object data) {
        dump(node, data);
        return data;
    }

    // + (unary)
    public Object visit(ASTUnaryPlusOperator node, Object data) {
        dump(node, data);
        return data;
    }

    // - (unary)
    public Object visit(ASTUnaryMinusOperator node, Object data) {
        dump(node, data);
        return data;
    }

    // Push string literal to stack
    public Object visit(ASTCharacter node, Object data) {
        dump(node, data);
        return data;
    }

    // Push integer literal to stack
    public Object visit(ASTInteger node, Object data) {
        dump(node, data);
        return data;
    }

    // Push floating point literal to stack
    public Object visit(ASTRational node, Object data) {
        dump(node, data);
        return data;
    }

    // Push true literal to stack
    public Object visit(ASTTrue node, Object data) {
        dump(node, data);
        return data;
    }

    // Push false literal to stack
    public Object visit(ASTFalse node, Object data) {
        dump(node, data);
        return data;
    }
}
