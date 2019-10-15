package intepreter;

import parser.ast.ASTCode;
import parser.ast.Jlang;
import parser.ast.JlangVisitor;


public class Intepreter {

    private static void usage() {
        System.out.println("Usage: jlang [-d1] < <source>");
        System.out.println("          -d1 -- output AST");
    }

    public static void main(String[] args) {
        boolean debugAST = false;
        if (args.length == 1) {
            if (args[0].equals("-d1"))
                debugAST = true;
            else {
                usage();
                return;
            }
        }
        Jlang language = new Jlang(System.in);
        try {
            ASTCode parser = language.code();
            JlangVisitor nodeVisitor;
            if (debugAST)
                nodeVisitor = new ParserDebugger();
            else
                nodeVisitor = new Parser();
            parser.jjtAccept(nodeVisitor, null);
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }

    }
}
