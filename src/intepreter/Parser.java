package intepreter;

import parser.ast.*;
import values.*;

/*
 * The Parser class has been copied from the Sili example from the set of LDI examples.
 * Any added functionality will be noted in comments.
 */
public class Parser implements JlangVisitor {
    // Scope display handler  - From Sili
    private Display scope = new Display();

    // Get the ith child of a given node.  - From Sili
    private static SimpleNode getChild(SimpleNode node, int childIndex) {
        return (SimpleNode)node.jjtGetChild(childIndex);
    }

    // Get the token value of the ith child of a given node.  - From Sili
    private static String getTokenOfChild(SimpleNode node, int childIndex) {
        return getChild(node, childIndex).tokenValue;
    }

    // Execute a given child of the given node  - From Sili
    private Object doChild(SimpleNode node, int childIndex, Object data) {
        return node.jjtGetChild(childIndex).jjtAccept(this, data);
    }

    // Execute a given child of a given node, and return its value as a Value.
    // This is used by the expression evaluation nodes.
    //From Sili
    Value doChild(SimpleNode node, int childIndex) {
        return (Value)doChild(node, childIndex, null);
    }

    // Execute all children of the given node  - From Sili
    Object doChildren(SimpleNode node, Object data) {
        return node.childrenAccept(this, data);
    }

    // Called if one of the following methods is missing...  - From Sili
    public Object visit(SimpleNode node, Object data) {
        System.out.println(node + ": acceptor not implemented in subclass?");
        return data;
    }

    // Execute a Jlang program  - From Sili
    public Object visit(ASTCode node, Object data) {
        return doChildren(node, data);
    }

    // Execute a statement  - From Sili
    public Object visit(ASTStatement node, Object data) {
        return doChildren(node, data);
    }

    // Execute a block  - From Sili
    public Object visit(ASTBlock node, Object data) {
        return doChildren(node, data);
    }

    // Function definition  - From Sili
    public Object visit(ASTFnDef node, Object data) {
        // Already defined?
        if (node.optimised != null)
            return data;
        // Child 0 - identifier (fn name)
        String fnname = getTokenOfChild(node, 0);
        if (scope.findFunctionInCurrentLevel(fnname) != null)
            throw new ExceptionSemantic("Function " + fnname + " already exists.");
        FunctionDefinition currentFunctionDefinition = new FunctionDefinition(fnname, scope.getLevel() + 1);
        // Child 1 - function definition parameter list
        doChild(node, 1, currentFunctionDefinition);
        // Add to available functions
        scope.addFunction(currentFunctionDefinition);
        // Child 2 - function body
        currentFunctionDefinition.setFunctionBody(getChild(node, 2));
        // Child 3 - optional return expression
        if (node.fnHasReturn)
            currentFunctionDefinition.setFunctionReturnExpression(getChild(node, 3));
        // Preserve this definition for future reference, and so we don't define
        // it every time this node is processed.
        node.optimised = currentFunctionDefinition;
        return data;
    }

    // Function definition parameter list  - From Sili
    public Object visit(ASTParmlist node, Object data) {
        FunctionDefinition currentDefinition = (FunctionDefinition)data;
        for (int i=0; i<node.jjtGetNumChildren(); i++)
            currentDefinition.defineParameter(getTokenOfChild(node, i));
        return data;
    }

    // Function body  - From Sili
    public Object visit(ASTFnBody node, Object data) {
        return doChildren(node, data);
    }

    // Function return expression  - From Sili
    public Object visit(ASTReturnExpression node, Object data) {
        return doChildren(node, data);
    }

    // Function call  - From Sili
    public Object visit(ASTCall node, Object data) {
        FunctionDefinition fndef;
        if (node.optimised == null) {
            // Child 0 - identifier (fn name)
            String fnname = getTokenOfChild(node, 0);
            fndef = scope.findFunction(fnname);
            if (fndef == null)
                throw new ExceptionSemantic("Function " + fnname + " is undefined.");
            // Save it for next time
            node.optimised = fndef;
        } else
            fndef = (FunctionDefinition)node.optimised;
        FunctionInvocation newInvocation = new FunctionInvocation(fndef);
        // Child 1 - arglist
        doChild(node, 1, newInvocation);
        // Execute
        scope.execute(newInvocation, this);
        return data;
    }

    // Function invocation in an expression  - From Sili
    public Object visit(ASTFnInvoke node, Object data) {
        FunctionDefinition fndef;
        if (node.optimised == null) {
            // Child 0 - identifier (fn name)
            String fnname = getTokenOfChild(node, 0);
            fndef = scope.findFunction(fnname);
            if (fndef == null)
                throw new ExceptionSemantic("Function " + fnname + " is undefined.");
            if (!fndef.hasReturn())
                throw new ExceptionSemantic("Function " + fnname + " is being invoked in an expression but does not have a return value.");
            // Save it for next time
            node.optimised = fndef;
        } else
            fndef = (FunctionDefinition)node.optimised;
        FunctionInvocation newInvocation = new FunctionInvocation(fndef);
        // Child 1 - arglist
        doChild(node, 1, newInvocation);
        // Execute
        return scope.execute(newInvocation, this);
    }

    // Function invocation argument list.  - From Sili
    public Object visit(ASTArgList node, Object data) {
        FunctionInvocation newInvocation = (FunctionInvocation)data;
        for (int i=0; i<node.jjtGetNumChildren(); i++)
            newInvocation.setArgument(doChild(node, i));
        newInvocation.checkArgumentCount();
        return data;
    }

    // Execute an IF  - From Sili
    /*
     * The If statement code has been modified slightly in order to add the elseif functionality
     * that is to be implemented in Jlang.
     * 
     * (non-Javadoc)
     * @see parser.ast.JlangVisitor#visit(parser.ast.ASTIfStatement, java.lang.Object)
     */
    public Object visit(ASTIfStatement node, Object data) {
    	//value to use for the doChild of the elseif and else statements.
    	int i = 2;
    	//Boolean value that determines whether the if or elseif statement has been executed or not.
    	boolean hasStatementExecuted = false;

        // evaluate boolean expression
        Value hopefullyValueBoolean = doChild(node, 0);
        if (!(hopefullyValueBoolean instanceof ValueBoolean))
            throw new ExceptionSemantic("The test expression of an if statement must be boolean.");
        //Check the if statement and then execute it if it is true.
        if (((ValueBoolean)hopefullyValueBoolean).booleanValue()) {
        	doChild(node, 1);							// if(true), therefore do 'if' statement
        	//The if statement has been executed, so set the boolean value to true.
        	hasStatementExecuted = true;
        } else if (node.ifHasElseIf) {					// does it have an elseif statement?
        	while (true) {
        		 //Get the value to check in the elseif statement
             	Value hopefullyValueBoolean2 = doChild(node, i);
             	//The value in the elseif statement isn't a boolean so throw an error.
             	if (!(hopefullyValueBoolean2 instanceof ValueBoolean)) {
             		throw new ExceptionSemantic("The test expression of an if statement must be boolean.");
             	}
                 if (!((ValueBoolean)hopefullyValueBoolean2).booleanValue()) {
                	 //Increment the i value since the elseif values aren't the same and then start the loop again.
                	i = i + 2;
                 } else {
                	 //The statements match so execute the elseif statement.
                 	doChild(node, (i + 1));
                 	//Now since the statement has been executed, set the boolean to true so we don't run the else statement aswell.
                 	hasStatementExecuted = true;
                 	//And finally, exit from the while loop.
                 	break;
                 }
             }
        }
        //If the if statement has a else statement associated with it
        //and the else if statement hasn't been executed, execute what
        //needs to be done in the else statement.
        if (node.ifHasElse && !hasStatementExecuted) {
        	//Then execute the else statement.
        	doChild(node, i);
        }
        return data;
    }

    // Execute a FOR loop - From Sili
    public Object visit(ASTForLoop node, Object data) {
        // loop initialisation
        doChild(node, 0);
        while (true) {
            // evaluate loop test
            Value hopefullyValueBoolean = doChild(node, 1);
            if (!(hopefullyValueBoolean instanceof ValueBoolean))
                throw new ExceptionSemantic("The test expression of a for loop must be boolean.");
            if (!((ValueBoolean)hopefullyValueBoolean).booleanValue())
                break;
            // do loop statement
            doChild(node, 3);
            // assign loop increment
            doChild(node, 2);
        }
        return data;
    }

    //While loop
    /*
     * This is a while loop definition for Jlang
     * 
     * (non-Javadoc)
     * @see parser.ast.JlangVisitor#visit(parser.ast.ASTWhileLoop, java.lang.Object)
     */
    public Object visit(ASTWhileLoop node, Object data) {
        while (true) {
            // evaluate loop test
            Value hopefullyValueBoolean = doChild(node, 0);
            if (!(hopefullyValueBoolean instanceof ValueBoolean))
                throw new ExceptionSemantic("The test expression of a while loop must be boolean.");
            if (!((ValueBoolean)hopefullyValueBoolean).booleanValue())
                break;
            // do loop statement
            doChild(node, 1);
        }
        return data;
    }
    
    //Execute a Switch statement
    /*
     * This is the switch statement implementation code for Jlang
     * 
     * (non-Javadoc)
     * @see parser.ast.JlangVisitor#visit(parser.ast.ASTSwitchStatement, java.lang.Object)
     */
    public Object visit(ASTSwitchStatement node, Object data) {
    	//Get the value to compare all the cases against
    	Value switchValue = doChild(node, 0);
    	
    	//The case value is an integer
        if (switchValue instanceof ValueInteger) {
        	try {
        		//Cast the case value to an integer
            	ValueInteger switchValueAsValueInteger = (ValueInteger)switchValue;
            	//Used to make sure we don't execute the default statement
            	boolean statementExecuted = false;
            	//Varible to hold the number of the child to execute
            	int i = 1;
            	
            	//Loop through the cases and execute.
            	while (true) {
            		//Get the actual value of the case statement to compare against
            		ValueInteger caseValue = (ValueInteger)doChild(node, i);
            		
            		//Break the switch statement if the caseValue is null.
            		if (caseValue == null) {
            			break;
            		}
            		//Compare the switch value as an integer with the case value obtained above.
            		if (switchValueAsValueInteger.eq(caseValue).booleanValue()) {
            			//Execute the case statement
                		doChild(node, (i + 1));
                		//The statement has already been executed so set the boolean to true.
                		statementExecuted = true;
                	} else {
                		//Increment loop count if the case values aren't equal.
                		i = i + 2;
                	}
            		if (statementExecuted) {
            			//If the statement has been executed, break out of the while loop.
            			break;
            		}

            		//If the switch statement has a default statement and the statement hasn't executed,
            		//run the default statement.
            		if (node.switchHasDefault && statementExecuted) {
                		//Execute statement
                		doChild(node, i);
                		break;
                	}
            	}
        	} catch (Exception e) {
        		//The case value isn't an integer, so throw an error
        		throw new ExceptionSemantic("The expression provided for the switch case must be an integer.");
        	}
        } else if (switchValue instanceof ValueBoolean) { //The case value is a boolean
        	try {
        		//Cast the case value to a boolean
            	ValueBoolean switchValueAsValueBoolean = (ValueBoolean)switchValue;
            	//Used to make sure we don't execute the default statement
            	boolean statementExecuted = false;
            	//Varible to hold the number of the child to execute
            	int i = 1;
            	
            	//Loop through the cases and execute.
            	while (true) {
            		//Get the actual value of the case statement to compare against
            		ValueBoolean caseValue = (ValueBoolean)doChild(node, i);
            		//Break the switch statement if the caseValue is null.
            		if (caseValue == null) {
            			break;
            		}
            		
            		if (switchValueAsValueBoolean.eq(caseValue).booleanValue()) {
            			//Execute the case statement
                		doChild(node, (i + 1));
                		//The statement has already been executed so set the boolean to true.
                		statementExecuted = true;
                	} else {
                		//Increment loop count if the case values aren't equal.
                		i = i + 2;
                	}
            		if (statementExecuted) {
            			//If the statement has been executed, break out of the while loop.
            			break;
            		}

            		//If the switch statement has a default statement and the statement hasn't executed,
            		//run the default statement.
            		if (node.switchHasDefault && statementExecuted) {
                		//Execute statement
                		doChild(node, i);
                		break;
                	}
            	}
        	} catch (Exception e) {
        		//The case value isn't a boolean, so throw an error
        		throw new ExceptionSemantic("The expression provided for the switch case must be a boolean.");
        	}
        } else if (switchValue instanceof ValueFloat) { //The case value is a float
        	try {
        		//Cast the case value to a float
            	ValueFloat switchValueAsValueFloat = (ValueFloat)switchValue;
            	//Used to make sure we don't execute the default statement
            	boolean statementExecuted = false;
            	//Varible to hold the number of the child to execute
            	int i = 1;
            	
            	//Loop through the cases and execute.
            	while (true) {
            		//Get the actual value of the case statement to compare against
            		ValueFloat caseValue = (ValueFloat)doChild(node, i);
            		//Break the switch statement if the caseValue is null.
            		if (caseValue == null) {
            			break;
            		}
            		
            		if (switchValueAsValueFloat.eq(caseValue).booleanValue()) {
            			//Execute the case statement
                		doChild(node, (i + 1));
                		//The statement has already been executed so set the boolean to true.
                		statementExecuted = true;
                	} else {
                		//Increment loop count if the case values aren't equal.
                		i = i + 2;
                	}
            		if (statementExecuted) {
            			//If the statement has been executed, break out of the while loop.
            			break;
            		}

            		//If the switch statement has a default statement and the statement hasn't executed,
            		//run the default statement.
            		if (node.switchHasDefault && statementExecuted) {
                		//Execute statement
                		doChild(node, i);
                		break;
                	}
            	}
        	} catch (Exception e) {
        		//The case value isn't a float, so throw an error
        		throw new ExceptionSemantic("The expression provided for the switch case must be a float.");
        	}
        } else if (switchValue instanceof ValueString) { //The case value is a string
        	try {
        		//Cast the case value to a string
            	ValueString switchValueAsValueString = (ValueString)switchValue;
            	//Used to make sure we don't execute the default statement
            	boolean statementExecuted = false;
            	//Varible to hold the number of the child to execute
            	int i = 1;
            	
            	//Loop through the cases and execute.
            	while (true) {
            		//Get the actual value of the case statement to compare against
            		ValueString caseValue = (ValueString)doChild(node, i);
            		//Break the switch statement if the caseValue is null.
            		if (caseValue == null) {
            			break;
            		}
            		
            		if (switchValueAsValueString.eq(caseValue).booleanValue()) {
            			//Execute the case statement
                		doChild(node, (i + 1));
                		//The statement has already been executed so set the boolean to true.
                		statementExecuted = true;
                	} else {
                		//Increment loop count if the case values aren't equal.
                		i = i + 2;
                	}
            		if (statementExecuted) {
            			//If the statement has been executed, break out of the while loop.
            			break;
            		}

            		//If the switch statement has a default statement and the statement hasn't executed,
            		//run the default statement.
            		if (node.switchHasDefault && statementExecuted) {
                		//Execute statement
                		doChild(node, i);
                		break;
                	}
            	}
        	} catch (Exception e) {
        		//The case value isn't a string, so throw an error
        		throw new ExceptionSemantic("The expression provided for the switch case must be a string.");
        	}
        } else {
        	//The user gave a varible that isn't supported so through an error
        	throw new ExceptionSemantic("The expression provided for the switch statement isn't supported.");
        }
    	
    	return data;
    }
    
    // Process an identifier
    // This doesn't do anything, but needs to be here because we need an ASTIdentifier node.
    /*************
     * From Sili *
     *************/
    public Object visit(ASTIdentifier node, Object data) {
        return data;
    }

    // Execute the WRITE statement - From Sili
    public Object visit(ASTWrite node, Object data) {
        System.out.println(doChild(node, 0));
        return data;
    }

    // Dereference a variable or parameter, and return its value. - From Sili
    public Object visit(ASTDereference node, Object data) {
        Display.Reference reference;
        if (node.optimised == null) {
            String name = node.tokenValue;
            reference = scope.findReference(name);
            if (reference == null)
                throw new ExceptionSemantic("Variable or parameter " + name + " is undefined.");
            node.optimised = reference;
        } else
            reference = (Display.Reference)node.optimised;
        return reference.getValue();
    }
    
    // Execute an assignment statement.
    /*
     * The assignment statement has been modified in order to fit static typing as defined for Jlang
     * 
     * (non-Javadoc)
     * @see parser.ast.JlangVisitor#visit(parser.ast.ASTAssignment, java.lang.Object)
     */
    public Object visit(ASTAssignment node, Object data) {
        Display.Reference reference;
        if (node.optimised == null) {
            String name = getTokenOfChild(node, 0);
            reference = scope.findReference(name);
            if (reference == null)
                reference = scope.defineVariable(name);
            node.optimised = reference;
        } else {
        	reference = (Display.Reference)node.optimised;
        }
        /**********************************
         * ASSIGNMENT CODE STARTS HERE!!! *
         **********************************/
        try {
            //Get the value of the varible the user wants to assign the new value to
            Value firstValue = reference.getValue();
            //Get the value the user wants to assign to the varible.
            Value secondValue = doChild(node, 1);
            
            /************************************************
             *		CHECKING THE TYPES OF VARIBLES!!!		*
             ************************************************/
            if (secondValue instanceof ValueInteger) { //The second value is an integer
            	try {
            		//Try to convert the varible to a integer and if not, throw an exception.
            		ValueInteger firstValueAsValueInteger = (ValueInteger)firstValue;
            		//Now check if both the first and second varibles are of the same type
            		//i.e. they are both instances of the VaribleInteger class.
            		if (firstValueAsValueInteger.getClass().equals(secondValue.getClass())) {
                		//Set the varible to what the user specify's
                		reference.setValue(doChild(node, 1));
                	} else {
                		//If they are not, throw an exeption.
                		throw new ExceptionSemantic("Cannot assign from one datatype to another");
                	}
            	} catch (Exception e) {
            		//Throw an exception if the first value can't be casted from one datatype to another.
            		throw new ExceptionSemantic("Cannot assign from one datatype to another");
            	}
            } else if (secondValue instanceof ValueString) { //The second value is a string
            	try {
            		//Try to convert the varible to a string and if not, throw an exception.
                	ValueString firstValueAsValueString = (ValueString)firstValue;
                	//Now check if both the first and second varibles are of the same type
            		//i.e. they are both instances of the VaribleString class.
                	if (firstValueAsValueString.getClass().equals(secondValue.getClass())) {
                		//Set the varible to what the user specify's
                		reference.setValue(doChild(node, 1));
            		} else {
            			//If they are not, throw an exeption.
            			throw new ExceptionSemantic("Cannot assign from one datatype to another");
            		}
            	} catch (Exception e) {
            		//Throw an exception if the first value can't be casted from one datatype to another.
            		throw new ExceptionSemantic("Cannot assign from one datatype to another");
            	}
            } else if(secondValue instanceof ValueFloat) { //The second value is a float
            	try {
            		//Try to convert the varible to a float and if not, throw an exception.
            		ValueFloat firstValueAsValueFloat = (ValueFloat)firstValue;
            		//Now check if both the first and second varibles are of the same type
            		//i.e. they are both instances of the VaribleFloat class.
                	if (firstValueAsValueFloat.getClass().equals(secondValue.getClass())) {
                		//Set the varible to what the user specify's
                		reference.setValue(doChild(node, 1));
                	} else {
                		//If they are not, throw an exeption.
                		throw new ExceptionSemantic("Cannot assign from one datatype to another");
                	}
            	} catch (Exception e) {
            		//Throw an exception if the first value can't be casted from one datatype to another.
            		throw new ExceptionSemantic("Cannot assign from one datatype to another");
            	}
            } else if (secondValue instanceof ValueBoolean) { //The second value is a boolean
            	try {
            		//Try to convert the varible to a float and if not, throw an exception.
            		ValueBoolean firstValueAsValueBoolean = (ValueBoolean)firstValue;
            		//Now check if both the first and second varibles are of the same type
            		//i.e. they are both instances of the VaribleFloat class.
                	if (firstValueAsValueBoolean.getClass().equals(secondValue.getClass())) {
                		//Set the varible to what the user specify's
                		reference.setValue(doChild(node, 1));
                	} else {
                		//If they are not, throw an exeption.
                		throw new ExceptionSemantic("Cannot assign from one datatype to another");
                	}
            	} catch (Exception e) {
            		//Throw an exception if the first value can't be casted from one datatype to another.
            		throw new ExceptionSemantic("Cannot assign from one datatype to another");
            	}
            } else {
            	// The datatype the user wants to assign to the varible isn't supported so throw an error
            	throw new ExceptionSemantic("Cannot assign from one datatype to another");
            }
        } catch (Exception e) {
        	//The user didn't spefify a data type so throw an error
        	throw new ExceptionSemantic("Please spefify a type of int, bool, float or string!!");
        }
        
        return data;
    }
    
    //Assignment statements for other data types
    /*
     * The below code is for assignment statements in order to provide the static typing functionality
     * 
     * (non-Javadoc)
     * @see parser.ast.JlangVisitor#visit(parser.ast.ASTAssignmentInteger, java.lang.Object)
     */
    
    // Execute an assignment statement for an integer.
    public Object visit(ASTAssignmentInteger node, Object data) {
        Display.Reference reference;
        if (node.optimised == null) {
            String name = getTokenOfChild(node, 0);
            reference = scope.findReference(name);
            if (reference == null)
                reference = scope.defineVariable(name);
            node.optimised = reference;
        } else
            reference = (Display.Reference)node.optimised;
        
        //Get the value the user enterd
        Value hopefullyValueInteger = doChild(node, 1);
        //Check that the value the user enterd is of a integer type.
        //If it isn't throw an error.
        if (!(hopefullyValueInteger instanceof ValueInteger)) {
        	throw new ExceptionSemantic("The Integer value can only hold values of type Integer.");
        }
        
        //The check succeded so atually set the value
        reference.setValue(doChild(node, 1));
        return data;
    }
    
    // Execute an assignment statement for an string.
    public Object visit(ASTAssignmentString node, Object data) {
        Display.Reference reference;
        if (node.optimised == null) {
            String name = getTokenOfChild(node, 0);
            reference = scope.findReference(name);
            if (reference == null)
                reference = scope.defineVariable(name);
            node.optimised = reference;
        } else
            reference = (Display.Reference)node.optimised;
        
        //Get the value the user enterd
        Value hopefullyValueString = doChild(node, 1);
        //Check that the value the user enterd is of a string type.
        //If it isn't throw an error.
        if (!(hopefullyValueString instanceof ValueString)) {
        	throw new ExceptionSemantic("The String value can only hold values of type String.");
        }
        
        //The check succeded so atually set the value
        reference.setValue(doChild(node, 1));
        return data;
    }

    // Execute an assignment statement for an float.
    public Object visit(ASTAssignmentFloat node, Object data) {
        Display.Reference reference;
        if (node.optimised == null) {
            String name = getTokenOfChild(node, 0);
            reference = scope.findReference(name);
            if (reference == null)
                reference = scope.defineVariable(name);
            node.optimised = reference;
        } else
            reference = (Display.Reference)node.optimised;

        //Get the value the user enterd
        Value hopefullyValueFloat = doChild(node, 1);
        //Check that the value the user enterd is of a float type.
        //If it isn't throw an error.
        if (!(hopefullyValueFloat instanceof ValueFloat)) {
        	throw new ExceptionSemantic("The Float value can only hold values of type Float.");
        }
        
        //The check succeded so atually set the value
        reference.setValue(doChild(node, 1));
        return data;
    }
    
    // Execute an assignment statement for an boolean.
    public Object visit(ASTAssignmentBoolean node, Object data) {
        Display.Reference reference;
        if (node.optimised == null) {
            String name = getTokenOfChild(node, 0);
            reference = scope.findReference(name);
            if (reference == null)
                reference = scope.defineVariable(name);
            node.optimised = reference;
        } else
            reference = (Display.Reference)node.optimised;
        
      //Get the value the user enterd
        Value hopefullyValueBoolean = doChild(node, 1);
      //Check that the value the user enterd is of a boolean type.
        //If it isn't throw an error.
        if (!(hopefullyValueBoolean instanceof ValueBoolean)) {
        	throw new ExceptionSemantic("The Bool value can only hold values of type Boolean (True or False).");
        }
        
        //The check succeded so atually set the value
        reference.setValue(doChild(node, 1));
        return data;
    }
    // OR - From Sili
    public Object visit(ASTOrExpression node, Object data) {
        return doChild(node, 0).or(doChild(node, 1));
    }

    // AND - From Sili
    public Object visit(ASTAndExpression node, Object data) {
        return doChild(node, 0).and(doChild(node, 1));
    }

    // == - From Sili
    public Object visit(ASTComparisonEqual node, Object data) {
        return doChild(node, 0).eq(doChild(node, 1));
    }

    // != - From Sili
    public Object visit(ASTComparisonNotEqualTo node, Object data) {
        return doChild(node, 0).neq(doChild(node, 1));
    }

    // >= - From Sili
    public Object visit(ASTComparisonGreatorThanOrEqualTo node, Object data) {
        return doChild(node, 0).gte(doChild(node, 1));
    }

    // <= - From Sili
    public Object visit(ASTComparisonLessThanOrEqualTo node, Object data) {
        return doChild(node, 0).lte(doChild(node, 1));
    }

    // > - From Sili
    public Object visit(ASTComparisonGreatorThan node, Object data) {
        return doChild(node, 0).gt(doChild(node, 1));
    }

    // < - From Sili
    public Object visit(ASTComparisonLessThan node, Object data) {
        return doChild(node, 0).lt(doChild(node, 1));
    }

    // + - From Sili
    public Object visit(ASTAddOperator node, Object data) {
        return doChild(node, 0).add(doChild(node, 1));
    }

    // - - From Sili
    public Object visit(ASTSubtractOperator node, Object data) {
        return doChild(node, 0).subtract(doChild(node, 1));
    }

    // * - From Sili
    public Object visit(ASTTimesOperator node, Object data) {
        return doChild(node, 0).mult(doChild(node, 1));
    }

    // / - From Sili
    public Object visit(ASTDivideOperator node, Object data) {
        return doChild(node, 0).div(doChild(node, 1));
    }

    // NOT - From Sili
    public Object visit(ASTUnaryNotOperator node, Object data) {
        return doChild(node, 0).not();
    }

    // + (unary) - From Sili
    public Object visit(ASTUnaryPlusOperator node, Object data) {
        return doChild(node, 0).unary_plus();
    }

    // - (unary) - From Sili
    public Object visit(ASTUnaryMinusOperator node, Object data) {
        return doChild(node, 0).unary_minus();
    }

    // Return string literal - From Sili
    public Object visit(ASTCharacter node, Object data) {
        if (node.optimised == null)
            node.optimised = ValueString.stripDelimited(node.tokenValue);
        return node.optimised;
    }

    // Return integer literal - From Sili
    public Object visit(ASTInteger node, Object data) {
        if (node.optimised == null)
            node.optimised = new ValueInteger(Long.parseLong(node.tokenValue));
        return node.optimised;
    }

    // Return floating point literal - From Sili
    public Object visit(ASTRational node, Object data) {
        if (node.optimised == null)
            node.optimised = new ValueFloat(Double.parseDouble(node.tokenValue));
        return node.optimised;
    }

    // Return true literal - From Sili
    public Object visit(ASTTrue node, Object data) {
        if (node.optimised == null)
            node.optimised = new ValueBoolean(true);
        return node.optimised;
    }

    // Return false literal - From Sili
    public Object visit(ASTFalse node, Object data) {
        if (node.optimised == null)
            node.optimised = new ValueBoolean(false);
        return node.optimised;
    }
}
