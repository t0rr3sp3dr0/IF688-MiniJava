package br.ufpe.cin.if688.minijava.visitor;

import br.ufpe.cin.if688.minijava.antlr.MiniJavaParser;
import br.ufpe.cin.if688.minijava.antlr.MiniJavaVisitor;
import br.ufpe.cin.if688.minijava.ast.*;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Iterator;

public class MiniJavaTVisitor<T> implements MiniJavaVisitor<T> {
    @Override
    public T visitGoal(MiniJavaParser.GoalContext ctx) {
        MainClass mainClass = ctx.mainClass().accept((MiniJavaVisitor<? extends MainClass>) this);

        ClassDeclList classDeclList = new ClassDeclList();
        for (MiniJavaParser.ClassDeclarationContext classDeclarationContext : ctx.classDeclaration())
            classDeclList.addElement(classDeclarationContext.accept((MiniJavaVisitor<? extends ClassDecl>) this));

        return (T) new Program(mainClass, classDeclList);
    }

    @Override
    public T visitMainClass(MiniJavaParser.MainClassContext ctx) {
        Identifier identifier = ctx.identifier(0).accept((MiniJavaVisitor<? extends Identifier>) this);

        Identifier args = ctx.identifier(1).accept((MiniJavaVisitor<? extends Identifier>) this);

        Statement statement = ctx.statement().accept((MiniJavaVisitor<? extends Statement>) this);

        return (T) new MainClass(identifier, args, statement);
    }

    @Override
    public T visitClassDeclaration(MiniJavaParser.ClassDeclarationContext ctx) {
        Identifier identifier = ctx.identifier(0).accept((MiniJavaVisitor<? extends Identifier>) this);

        Identifier _extends = ctx.identifier().size() == 2 ? ctx.identifier(1).accept((MiniJavaVisitor<? extends Identifier>) this) : null;

        VarDeclList varDeclList = new VarDeclList();
        for (MiniJavaParser.VarDeclarationContext varDeclarationContext : ctx.varDeclaration())
            varDeclList.addElement(varDeclarationContext.accept((MiniJavaVisitor<? extends VarDecl>) this));

        MethodDeclList methodDeclList = new MethodDeclList();
        for (MiniJavaParser.MethodDeclarationContext methodDeclarationContext : ctx.methodDeclaration())
            methodDeclList.addElement(methodDeclarationContext.accept((MiniJavaVisitor<? extends MethodDecl>) this));

        if (_extends == null)
            return (T) new ClassDeclSimple(identifier, varDeclList, methodDeclList);

        return (T) new ClassDeclExtends(identifier, _extends, varDeclList, methodDeclList);
    }

    @Override
    public T visitVarDeclaration(MiniJavaParser.VarDeclarationContext ctx) {
        Type type = ctx.type().accept((MiniJavaVisitor<? extends Type>) this);

        Identifier identifier = ctx.identifier().accept((MiniJavaVisitor<? extends Identifier>) this);

        return (T) new VarDecl(type, identifier);
    }

    @Override
    public T visitMethodDeclaration(MiniJavaParser.MethodDeclarationContext ctx) {
        Type type = ctx.type(0).accept((MiniJavaVisitor<? extends Type>) this);

        Identifier identifier = ctx.identifier(0).accept((MiniJavaVisitor<? extends Identifier>) this);

        Iterator<MiniJavaParser.TypeContext> typeContextIterator = ctx.type().iterator();
        typeContextIterator.next();
        Iterator<MiniJavaParser.IdentifierContext> identifierContextIterator = ctx.identifier().iterator();
        identifierContextIterator.next();
        FormalList formalList = new FormalList();
        while (typeContextIterator.hasNext() && identifierContextIterator.hasNext())
            formalList.addElement(new Formal(typeContextIterator.next().accept((MiniJavaVisitor<? extends Type>) this), identifierContextIterator.next().accept((MiniJavaVisitor<? extends Identifier>) this)));

        VarDeclList varDeclList = new VarDeclList();
        for (MiniJavaParser.VarDeclarationContext varDeclarationContext : ctx.varDeclaration())
            varDeclList.addElement(varDeclarationContext.accept((MiniJavaVisitor<? extends VarDecl>) this));

        StatementList statementList = new StatementList();
        for (MiniJavaParser.StatementContext statementContext : ctx.statement())
            statementList.addElement(statementContext.accept((MiniJavaVisitor<? extends Statement>) this));

        Exp exp = ctx.expression().accept((MiniJavaVisitor<? extends Exp>) this);

        return (T) new MethodDecl(type, identifier, formalList, varDeclList, statementList, exp);
    }

    @Override
    public T visitType(MiniJavaParser.TypeContext ctx) {
        String text = ctx.getText();
        switch (text) {
            case "int[]":
                return (T) new IntArrayType();

            case "boolean":
                return (T) new BooleanType();

            case "int":
                return (T) new IntegerType();

            default:
                return (T) new IdentifierType(text);
        }
    }

    @Override
    public T visitStatement(MiniJavaParser.StatementContext ctx) {
        switch (ctx.getStart().getText()) {
            case "{": {
                StatementList statementList = new StatementList();
                for (MiniJavaParser.StatementContext statementContext : ctx.statement())
                    statementList.addElement(statementContext.accept((MiniJavaVisitor<? extends Statement>) this));

                return (T) new Block(statementList);
            }
            case "if": {
                Exp exp = ctx.expression(0).accept((MiniJavaVisitor<? extends Exp>) this);

                Statement _true = ctx.statement(0).accept((MiniJavaVisitor<? extends Statement>) this);

                Statement _false = ctx.statement(1).accept((MiniJavaVisitor<? extends Statement>) this);

                return (T) new If(exp, _true, _false);
            }

            case "while": {
                Exp exp = ctx.expression(0).accept((MiniJavaVisitor<? extends Exp>) this);

                Statement statement = ctx.statement(0).accept((MiniJavaVisitor<? extends Statement>) this);

                return (T) new While(exp, statement);
            }

            case "System.out.println": {
                Exp exp = ctx.expression(0).accept((MiniJavaVisitor<? extends Exp>) this);

                return (T) new Print(exp);
            }

            default: {
                if (ctx.expression().size() == 1) {
                    Identifier identifier = ctx.identifier().accept((MiniJavaVisitor<? extends Identifier>) this);

                    Exp exp = ctx.expression(0).accept((MiniJavaVisitor<? extends Exp>) this);

                    return (T) new Assign(identifier, exp);
                }

                Identifier identifier = ctx.identifier().accept((MiniJavaVisitor<? extends Identifier>) this);

                Exp index = ctx.expression(0).accept((MiniJavaVisitor<? extends Exp>) this);

                Exp value = ctx.expression(1).accept((MiniJavaVisitor<? extends Exp>) this);

                return (T) new ArrayAssign(identifier, index, value);
            }
        }
    }

    @Override
    public T visitExpression(MiniJavaParser.ExpressionContext ctx) {
        if (ctx.getChild(0) instanceof MiniJavaParser.ExpressionContext) {
            switch (ctx.getChild(1).getText()) {
                case "[": {
                    Exp array = ctx.expression(0).accept((MiniJavaVisitor<? extends Exp>) this);

                    Exp index = ctx.expression(0).accept((MiniJavaVisitor<? extends Exp>) this);

                    return (T) new ArrayLookup(array, index);
                }

                case ".": {
                    switch (ctx.getChild(2).getText()) {
                        case "length": {
                            Exp exp = ctx.expression(0).accept((MiniJavaVisitor<? extends Exp>) this);

                            return (T) new ArrayLength(exp);
                        }

                        default: {
                            Exp exp = ctx.expression(0).accept((MiniJavaVisitor<? extends Exp>) this);

                            Identifier identifier = ctx.identifier().accept((MiniJavaVisitor<? extends Identifier>) this);

                            boolean b = true;
                            ExpList expList = new ExpList();
                            for (MiniJavaParser.ExpressionContext expressionContext : ctx.expression()) {
                                if (b) {
                                    b = false;
                                    continue;
                                }

                                expList.addElement(expressionContext.accept((MiniJavaVisitor<? extends Exp>) this));
                            }

                            return (T) new Call(exp, identifier, expList);
                        }
                    }
                }

                default: {
                    Exp lhs = ctx.expression(0).accept((MiniJavaVisitor<? extends Exp>) this);

                    Exp rhs = ctx.expression(1).accept((MiniJavaVisitor<? extends Exp>) this);

                    switch (ctx.getChild(1).getText()) {
                        case "&&": {
                            return (T) new And(lhs, rhs);
                        }

                        case "<": {
                            return (T) new LessThan(lhs, rhs);
                        }

                        case "+": {
                            return (T) new Plus(lhs, rhs);
                        }

                        case "-": {
                            return (T) new Minus(lhs, rhs);
                        }

                        case "*": {
                            return (T) new Times(lhs, rhs);
                        }

                        default:
                            assert false;
                            return null;
                    }
                }
            }
        }

        switch (ctx.getStart().getText()) {
            case "true": {
                return (T) new True();
            }

            case "false": {
                return (T) new False();
            }

            case "this": {
                return (T) new This();
            }

            case "new": {
                if (ctx.getChild(1).getText().equals("int")) {
                    Exp exp = ctx.expression(0).accept((MiniJavaVisitor<? extends Exp>) this);

                    return (T) new NewArray(exp);
                }

                Identifier identifier = ctx.identifier().accept((MiniJavaVisitor<? extends Identifier>) this);

                return (T) new NewObject(identifier);
            }

            case "!": {
                Exp exp = ctx.expression(0).accept((MiniJavaVisitor<? extends Exp>) this);

                return (T) new Not(exp);
            }

            case "(": {
                Exp exp = ctx.expression(0).accept((MiniJavaVisitor<? extends Exp>) this);

                return (T) exp;
            }

            default: {
                TerminalNode terminalNode = ctx.INTEGER_LITERAL();
                if (terminalNode != null)
                    return (T) new IntegerLiteral(Integer.valueOf(terminalNode.getText()));

                Identifier identifier = ctx.identifier().accept((MiniJavaVisitor<? extends Identifier>) this);

                return (T) identifier;
            }
        }
    }

    @Override
    public T visitIdentifier(MiniJavaParser.IdentifierContext ctx) {
        return (T) new Identifier(ctx.getText());
    }

    @Override
    public T visit(ParseTree parseTree) {
        return parseTree.accept(this);
    }

    @Override
    public T visitChildren(RuleNode ruleNode) {
        return null;
    }

    @Override
    public T visitTerminal(TerminalNode terminalNode) {
        return null;
    }

    @Override
    public T visitErrorNode(ErrorNode errorNode) {
        return null;
    }
}
