package br.ufpe.cin.if688.minijava;

import br.ufpe.cin.if688.minijava.antlr.MiniJavaLexer;
import br.ufpe.cin.if688.minijava.antlr.MiniJavaParser;
import br.ufpe.cin.if688.minijava.ast.Program;
import br.ufpe.cin.if688.minijava.visitor.MiniJavaTVisitor;
import br.ufpe.cin.if688.minijava.visitor.PrettyPrintVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        new PrettyPrintVisitor().visit(new MiniJavaTVisitor<Program>().visit(new MiniJavaParser(new CommonTokenStream(new MiniJavaLexer(CharStreams.fromStream(System.in)))).goal()));
    }
}
