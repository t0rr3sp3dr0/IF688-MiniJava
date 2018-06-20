package br.ufpe.cin.if688.minijava.visitor;

import br.ufpe.cin.if688.minijava.ast.And;
import br.ufpe.cin.if688.minijava.ast.ArrayAssign;
import br.ufpe.cin.if688.minijava.ast.ArrayLength;
import br.ufpe.cin.if688.minijava.ast.ArrayLookup;
import br.ufpe.cin.if688.minijava.ast.Assign;
import br.ufpe.cin.if688.minijava.ast.Block;
import br.ufpe.cin.if688.minijava.ast.BooleanType;
import br.ufpe.cin.if688.minijava.ast.Call;
import br.ufpe.cin.if688.minijava.ast.ClassDeclExtends;
import br.ufpe.cin.if688.minijava.ast.ClassDeclSimple;
import br.ufpe.cin.if688.minijava.ast.False;
import br.ufpe.cin.if688.minijava.ast.Formal;
import br.ufpe.cin.if688.minijava.ast.Identifier;
import br.ufpe.cin.if688.minijava.ast.IdentifierExp;
import br.ufpe.cin.if688.minijava.ast.IdentifierType;
import br.ufpe.cin.if688.minijava.ast.If;
import br.ufpe.cin.if688.minijava.ast.IntArrayType;
import br.ufpe.cin.if688.minijava.ast.IntegerLiteral;
import br.ufpe.cin.if688.minijava.ast.IntegerType;
import br.ufpe.cin.if688.minijava.ast.LessThan;
import br.ufpe.cin.if688.minijava.ast.MainClass;
import br.ufpe.cin.if688.minijava.ast.MethodDecl;
import br.ufpe.cin.if688.minijava.ast.Minus;
import br.ufpe.cin.if688.minijava.ast.NewArray;
import br.ufpe.cin.if688.minijava.ast.NewObject;
import br.ufpe.cin.if688.minijava.ast.Not;
import br.ufpe.cin.if688.minijava.ast.Plus;
import br.ufpe.cin.if688.minijava.ast.Print;
import br.ufpe.cin.if688.minijava.ast.Program;
import br.ufpe.cin.if688.minijava.ast.This;
import br.ufpe.cin.if688.minijava.ast.Times;
import br.ufpe.cin.if688.minijava.ast.True;
import br.ufpe.cin.if688.minijava.ast.Type;
import br.ufpe.cin.if688.minijava.ast.VarDecl;
import br.ufpe.cin.if688.minijava.ast.While;
import br.ufpe.cin.if688.minijava.symboltable.Class;
import br.ufpe.cin.if688.minijava.symboltable.Method;
import br.ufpe.cin.if688.minijava.symboltable.SymbolTable;

public class TypeCheckVisitor implements IVisitor<Type> {
    private SymbolTable symbolTable;
    private Class currClass;
    private Method currMethod;

    private static final Type INTEGER_TYPE = new IntegerType();
    private static final Type BOOLEAN_TYPE = new BooleanType();
    private static final Type ARRAY_TYPE = new IntArrayType();

    public TypeCheckVisitor(SymbolTable st) {
        this.symbolTable = st;
        this.currClass = null;
        this.currMethod = null;
    }

    // MainClass m;
    // ClassDeclList cl;
    public Type visit(Program n) {
        n.m.accept(this);
        for (int i = 0; i < n.cl.size(); ++i)
            n.cl.elementAt(i).accept(this);
        return null;
    }

    // Identifier i1,i2;
    // Statement s;
    public Type visit(MainClass n) {
        this.currClass = this.symbolTable.getClass(n.i1.s);
        assert this.currClass != null;

        this.currMethod = this.symbolTable.getMethod("main", n.i1.s);
        assert this.currMethod != null;

        n.i1.accept(this);
//        n.i2.accept(this);
        n.s.accept(this);

        this.currMethod = null;
        this.currClass = null;
        return null;
    }

    // Identifier i;
    // VarDeclList vl;
    // MethodDeclList ml;
    public Type visit(ClassDeclSimple n) {
        this.currClass = this.symbolTable.getClass(n.i.s);
        assert this.currClass != null;

        n.i.accept(this);
        for (int i = 0; i < n.vl.size(); ++i)
            n.vl.elementAt(i).accept(this);
        for (int i = 0; i < n.ml.size(); ++i)
            n.ml.elementAt(i).accept(this);

        this.currClass = null;
        return null;
    }

    // Identifier i;
    // Identifier j;
    // VarDeclList vl;
    // MethodDeclList ml;
    public Type visit(ClassDeclExtends n) {
        this.currClass = this.symbolTable.getClass(n.i.s);
        assert this.currClass != null;

        n.i.accept(this);
        n.j.accept(this);
        for (int i = 0; i < n.vl.size(); ++i)
            n.vl.elementAt(i).accept(this);
        for (int i = 0; i < n.ml.size(); ++i)
            n.ml.elementAt(i).accept(this);

        this.currClass = null;
        return null;
    }

    // Type t;
    // Identifier i;
    public Type visit(VarDecl n) {
        Type type = n.t.accept(this);
        n.i.accept(this);
        return type;
    }

    // Type t;
    // Identifier i;
    // FormalList fl;
    // VarDeclList vl;
    // StatementList sl;
    // Exp e;
    public Type visit(MethodDecl n) {
        assert this.currClass != null;
        this.currMethod = this.symbolTable.getMethod(n.i.s, this.currClass.getId());

        Type type = n.t.accept(this);
        n.i.accept(this);
        for (int i = 0; i < n.fl.size(); ++i)
            n.fl.elementAt(i).accept(this);
        for (int i = 0; i < n.vl.size(); ++i)
            n.vl.elementAt(i).accept(this);
        for (int i = 0; i < n.sl.size(); ++i)
            n.sl.elementAt(i).accept(this);

        if (!this.symbolTable.compareTypes(type, n.e.accept(this)))
            throw new RuntimeException(String.format("Incompatible types\t%s", n.e));

        this.currMethod = null;
        return type;
    }

    // Type t;
    // Identifier i;
    public Type visit(Formal n) {
        Type type = n.t.accept(this);
        n.i.accept(this);
        return type;
    }

    public Type visit(IntArrayType n) {
        return n;
    }

    public Type visit(BooleanType n) {
        return n;
    }

    public Type visit(IntegerType n) {
        return n;
    }

    // String s;
    public Type visit(IdentifierType n) {
        if (!this.symbolTable.containsClass(n.s))
            throw new RuntimeException("Cannot resolve symbol");
        return n;
    }

    // StatementList sl;
    public Type visit(Block n) {
        for (int i = 0; i < n.sl.size(); ++i)
            n.sl.elementAt(i).accept(this);
        return null;
    }

    // Exp e;
    // Statement s1,s2;
    public Type visit(If n) {
        if (!(n.e.accept(this) instanceof BooleanType))
            throw new RuntimeException(String.format("Incompatible types\t%s", n.e));
        n.s1.accept(this);
        n.s2.accept(this);
        return null;
    }

    // Exp e;
    // Statement s;
    public Type visit(While n) {
        if (!(n.e.accept(this) instanceof BooleanType))
            throw new RuntimeException(String.format("Incompatible types\t%s", n.e));
        n.s.accept(this);
        return null;
    }

    // Exp e;
    public Type visit(Print n) {
        n.e.accept(this);
        return null;
    }

    // Identifier i;
    // Exp e;
    public Type visit(Assign n) {
        if (!this.symbolTable.compareTypes(n.i.accept(this), n.e.accept(this)))
            throw new RuntimeException(String.format("Incompatible types\t%s", n.e));
        return null;
    }

    // Identifier i;
    // Exp e1,e2;
    public Type visit(ArrayAssign n) {
        if (n.i.accept(this) instanceof IntArrayType && n.e1.accept(this) instanceof IntegerType && n.e2.accept(this) instanceof IntegerType)
            return INTEGER_TYPE;
        throw new RuntimeException(String.format("Incompatible types\t%s\t%s", n.e1, n.e2));
    }

    // Exp e1,e2;
    public Type visit(And n) {
        if (n.e1.accept(this) instanceof BooleanType && n.e2.accept(this) instanceof BooleanType)
            return BOOLEAN_TYPE;
        throw new RuntimeException(String.format("Incompatible types\t%s\t%s", n.e1, n.e2));
    }

    // Exp e1,e2;
    public Type visit(LessThan n) {
        if (n.e1.accept(this) instanceof IntegerType && n.e2.accept(this) instanceof IntegerType)
            return BOOLEAN_TYPE;
        throw new RuntimeException(String.format("Incompatible types\t%s\t%s", n.e1, n.e2));
    }

    // Exp e1,e2;
    public Type visit(Plus n) {
        if (n.e1.accept(this) instanceof IntegerType && n.e2.accept(this) instanceof IntegerType)
            return INTEGER_TYPE;
        throw new RuntimeException(String.format("Incompatible types\t%s\t%s", n.e1, n.e2));
    }

    // Exp e1,e2;
    public Type visit(Minus n) {
        if (n.e1.accept(this) instanceof IntegerType && n.e2.accept(this) instanceof IntegerType)
            return INTEGER_TYPE;
        throw new RuntimeException(String.format("Incompatible types\t%s\t%s", n.e1, n.e2));
    }

    // Exp e1,e2;
    public Type visit(Times n) {
        if (n.e1.accept(this) instanceof IntegerType && n.e2.accept(this) instanceof IntegerType)
            return INTEGER_TYPE;
        throw new RuntimeException(String.format("Incompatible types\t%s\t%s", n.e1, n.e2));
    }

    // Exp e1,e2;
    public Type visit(ArrayLookup n) {
        if (n.e1.accept(this) instanceof IntArrayType && n.e2.accept(this) instanceof IntegerType)
            return INTEGER_TYPE;
        throw new RuntimeException(String.format("Incompatible types\t%s\t%s", n.e1, n.e2));
    }

    // Exp e;
    public Type visit(ArrayLength n) {
        if (n.e.accept(this) instanceof IntArrayType)
            return INTEGER_TYPE;
        throw new RuntimeException(String.format("Incompatible types\t%s", n.e));
    }

    // Exp e;
    // Identifier i;
    // ExpList el;
    public Type visit(Call n) {
        Type type;
        if ((type = n.e.accept(this)) instanceof IdentifierType) {
            IdentifierType identifierType = (IdentifierType) type;

            Class currClass = this.currClass;

            this.currClass = this.symbolTable.getClass(identifierType.s);
            assert this.currClass != null;

            Type identifier = n.i.accept(this);

            this.currClass = currClass;

            Method method = this.symbolTable.getMethod(n.i.s, identifierType.s);
            if (method.getParamAt(n.el.size()) != null)
                throw new RuntimeException("Cannot resolve method");
            for (int i = 0; i < n.el.size(); ++i)
                if (!this.symbolTable.compareTypes(method.getParamAt(i).type(), n.el.elementAt(i).accept(this)))
                    throw new RuntimeException(String.format("Incompatible types\t%s", n.el.elementAt(i)));

            return identifier;
        }
        throw new RuntimeException("Identifier expected");
    }

    // int i;
    public Type visit(IntegerLiteral n) {
        return TypeCheckVisitor.INTEGER_TYPE;
    }

    public Type visit(True n) {
        return TypeCheckVisitor.BOOLEAN_TYPE;
    }

    public Type visit(False n) {
        return TypeCheckVisitor.BOOLEAN_TYPE;
    }

    // String s;
    public Type visit(IdentifierExp n) {
        assert this.currClass != null;
        return this.symbolTable.getVarType(this.currMethod, this.currClass, n.s);
    }

    public Type visit(This n) {
        assert this.currClass != null;
        return this.currClass.type();
    }

    // Exp e;
    public Type visit(NewArray n) {
        if (n.e.accept(this) instanceof IntegerType)
            return TypeCheckVisitor.ARRAY_TYPE;
        throw new RuntimeException(String.format("Incompatible types\t%s", n.e));
    }

    // Identifier i;
    public Type visit(NewObject n) {
        return n.i.accept(this);
    }

    // Exp e;
    public Type visit(Not n) {
        if (n.e.accept(this) instanceof BooleanType)
            return TypeCheckVisitor.BOOLEAN_TYPE;
        throw new RuntimeException(String.format("Incompatible types\t%s", n.e));
    }

    // String s;
    public Type visit(Identifier n) {
        Type type;
        if ((this.symbolTable.containsClass(n.s) && (type = this.symbolTable.getClass(n.s).type()) != null) || (this.currClass != null && this.currClass.containsMethod(n.s) && (type = this.currClass.getMethod(n.s).type()) != null) || (this.currClass != null && this.currClass.containsVar(n.s) && (type = this.currClass.getVar(n.s).type()) != null) || (type = this.symbolTable.getVarType(this.currMethod, this.currClass, n.s)) != null)
            return type;
        throw new RuntimeException(String.format("Cannot resolve symbol %s", n.s));
    }
}
