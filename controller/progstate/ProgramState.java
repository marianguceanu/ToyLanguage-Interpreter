package controller.progstate;

import controller.evaluation.Eval;
import controller.exestack.ExeStack;
import controller.exestack.MyDeque;
import controller.symtable.SymTable;
import exceptions.*;
import model.statements.*;
import model.symbol.ISymbol;

public class ProgramState implements IProgramState {

    private SymTable<String, ISymbol> table;
    private ExeStack stack;
    private MyDeque<String> output;

    public ProgramState(String program) {
        this.table = new SymTable<String, ISymbol>();
        this.stack = new ExeStack();
        this.output = new MyDeque<String>();
        this.stack.addLast(new CompStmt(new String(program)));
    }

    public SymTable<String, ISymbol> getTable() {
        return this.table;
    }

    public ExeStack getStack() {
        return this.stack;
    }

    public MyDeque<String> getOutput() {
        return this.output;
    }

    @Override
    public void nextIsAssign() throws SymbolException, TypeException, DivisionByZero {
        CompStmt comp = (CompStmt) this.stack.getLast();
        String assignContent = comp.getStmt();
        try {
            // IStmt nexCompStmt = ((CompStmt) comp).nextCompStmt();
            AssignStmt stmt = new AssignStmt(assignContent);
            AssignStmt astmt = Eval.processAssign(table, stmt);
            if (astmt == null)
                return;
            this.stack.removeLast();
            this.stack.addLast(((CompStmt) comp).nextCompStmt());
            this.stack.addFirst(astmt);
        } catch (SymbolException s) {
            throw new SymbolException(s.getMessage());
        } catch (TypeException t) {
            throw new TypeException(t.getMessage());
        } catch (DivisionByZero d) {
            throw new DivisionByZero(d.getMessage());
        }
    }

    @Override
    public void nextIsDecl() throws SymbolException {
        CompStmt comp = (CompStmt) this.stack.getLast();
        String declContent = comp.getStmt();
        try {
            VarDecl v = new VarDecl(declContent);
            VarDecl dstmt = Eval.processDecl(table, v);
            if (dstmt == null)
                return;
            this.stack.removeLast();
            this.stack.addLast(comp.nextCompStmt());
            this.stack.addFirst(v);
        } catch (SymbolException s) {
            throw new SymbolException(s.getMessage());
        }
    }

    @Override
    public void nextIsIf() throws SymbolException, TypeException, DivisionByZero, StmtException {
        CompStmt comp = (CompStmt) this.stack.getLast();
        String condContent = comp.getStmt();
        try {
            IfStmt conditional = new IfStmt(condContent);
            IfStmt cond = Eval.processConditional(stack, output, table, conditional);
            if (cond == null) {
                return;
            }
            this.stack.removeLast();
            this.stack.addLast(comp.nextCompStmt());
            this.stack.addFirst(cond);
        } catch (SymbolException s) {
            throw new SymbolException(s.getMessage());
        } catch (TypeException t) {
            throw new TypeException(t.getMessage());
        } catch (DivisionByZero d) {
            throw new DivisionByZero(d.getMessage());
        } catch (StmtException st) {
            throw new StmtException(st.getMessage());
        }
    }

    @Override
    public void nextIsPrint() throws SymbolException {
        CompStmt comp = (CompStmt) this.stack.getLast();
        String printContent = comp.getStmt();
        try {
            PrintStmt print = new PrintStmt(printContent);
            PrintStmt check = Eval.processPrint(output, table, print);
            if (check == null) {
                return;
            }
            this.stack.removeLast();
            this.stack.addLast(comp.nextCompStmt());
            this.stack.addFirst(check);
        } catch (SymbolException s) {
            throw new SymbolException(s.getMessage());
        }
    }

    @Override
    public void nextStep() throws SymbolException, TypeException, DivisionByZero, StmtException {
        IStmt last = this.stack.getLast();
        if (last.getType() == "NOP") {
            System.exit(0);
        }
        try {
            this.nextIsAssign();
            this.nextIsDecl();
            this.nextIsPrint();
            this.nextIsIf();
        } catch (SymbolException s) {
            throw new SymbolException(s.getMessage());
        } catch (TypeException t) {
            throw new TypeException(t.getMessage());
        } catch (DivisionByZero d) {
            throw new DivisionByZero(d.getMessage());
        } catch (StmtException st) {
            throw new StmtException(st.getMessage());
        }
    }
}
