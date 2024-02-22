package CompilerCreation;

import org.antlr.runtime.tree.TreeWizard;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.tree.*;

import javax.management.NotificationEmitter;
import java.util.ArrayList;
import java.util.*;
import java.io.*;


public class Driver {

    public static class VerboseListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer,
                                Object offendingSymbol,
                                int line, int charPositionInLine,
                                String msg,
                                RecognitionException e) {
            List<String> stack = ((Parser) recognizer).getRuleInvocationStack();
            Collections.reverse(stack);
        }
    }


    public static void main(String[] args) throws IOException {
        //CharStream input = CharStreams.fromStream(System.in);
        CharStream input = CharStreams.fromFileName("C:\\Users\\James 305\\Desktop\\The Projects\\Java\\CompilerCreation\\test3.micro");
        LittleLexer lex = new LittleLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        LittleParser parser = new LittleParser(tokens);

        ParseTree tree = parser.program(); // begin parsing at program rule
        ParseTreeWalker walker = new ParseTreeWalker();
        SimpleTableBuilder simpleTableBuilder = new SimpleTableBuilder(); // EXAMPLE
        //Walk the tre created during the parse, trigger callbacks
        walker.walk(simpleTableBuilder, tree);
        simpleTableBuilder.newPrint();

        simpleTableBuilder.process3AC();
        System.out.println("sys halt");

    }
}

//This class defines entries to the symbol table.
class SymbolArrayEntry {
    String name = null;
    String type = null;
    String value = null;

    public SymbolArrayEntry() {
    }

    public SymbolArrayEntry(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public SymbolArrayEntry(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public String getValue() {
        return this.value;
    }

    @Override public String toString() {
        if (this.value == null) return ("name " + this.name + " type " + this.type + "\n");
        else return ("name " + this.name + " type " + this.type + " value " + this.value + "\n");
    }
}

//AST INFORMATION
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
class AST {

    Node root;
    Node currentNode;
    Node tempNode;
    int nodeNumber = 0;

    public void addNode(Node newNode) {
        if (nodeNumber == 0) {
            root = newNode;
            currentNode = root;
            nodeNumber++;
            return;
        }

        if (nodeNumber == 1) {
            currentNode.leftChild = newNode;
            nodeNumber++;
            return;
        }

        if (nodeNumber == 2) {
            tempNode = newNode;
            nodeNumber++;
            return;
        }

        if (nodeNumber == 3) {
            currentNode.rightChild = newNode;
            currentNode.rightChild.leftChild = tempNode;
            currentNode = currentNode.rightChild;
            nodeNumber++;
            return;
        }

        if (nodeNumber == 4) {
            currentNode.rightChild = newNode;
            nodeNumber++;
            return;
        }

        if (nodeNumber == 5) {
            nodeNumber = 0;
            //send to list and clear table
        }
    }

    public void resetCount() {
        nodeNumber = 0;
    }

    public void print_AST(int nodeNumber) {
        if (nodeNumber == 0) {
            return;
        }

        if (nodeNumber == 1) {
            System.out.println(root.value + " " + root.variableType);
        }

        if (nodeNumber == 2) {
            System.out.println(root.value + " " + root.variableType);
            System.out.println(root.leftChild.value + " " + root.leftChild.variableType);
        }

        if (nodeNumber == 3) {
            System.out.println(root.value + " " + root.variableType);
            System.out.println(root.leftChild.value + " " + root.leftChild.variableType);
            System.out.println(tempNode.value + " " + tempNode.variableType);
        }

        if (nodeNumber == 4) {
            System.out.println(root.value + " " + root.variableType);
            System.out.println(root.leftChild.value + " " + root.leftChild.variableType);
            System.out.println(tempNode.value + " " + tempNode.variableType);
            System.out.println(root.rightChild.value + " " + root.rightChild.variableType);
        }

        if (nodeNumber == 5) {
            System.out.println(root.value + " " + root.variableType);
            System.out.println(root.leftChild.value + " " + root.leftChild.variableType);
            System.out.println(tempNode.value + " " + tempNode.variableType);
            System.out.println(root.rightChild.value + " " + root.rightChild.variableType);
            System.out.println(root.rightChild.rightChild.value + " " + root.rightChild.rightChild.variableType);
        }

    }
}

class Node {
    String variableType;
    String value;
    Node leftChild;
    Node rightChild;
    Node parent;

    public Node() {
    }

    public Node(String variableType) {
        this.variableType = variableType;
    }

    public Node(String value, String variableType) {
        this.value = value;
        this.variableType = variableType;
    }


}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//END AST INFORMATION

class SimpleTableBuilder extends LittleBaseListener {
    //step 3
    LinkedHashMap<String, ArrayList<SymbolArrayEntry>> table2scope = new LinkedHashMap<>();
    ArrayList<SymbolArrayEntry> symbolTable = new ArrayList<>();
    Stack<String> scopeStack = new Stack<>();
    //step 4
    Node tempNode;
    AST astTree;
    ArrayList<AST> AST_array = new ArrayList<>();


    int symbolTableIndex = 0;
    int stackLevel = 0;
    int blockCount = 1;
    boolean containsElse = false;
    boolean isThereError = false;


    // everything PLUS READ and WRITE
    @Override
    public void enterId(LittleParser.IdContext ctx) {
        //if (table2scope.size() == 1) return;
        //System.out.println(ctx.getText());
        //   System.out.print("");
    }

    // READ and WRITE right side
    @Override
    public void enterId_list(LittleParser.Id_listContext ctx) {
        //if (table2scope.size() == 1) return;
        //System.out.println(ctx.getText());
    }


    @Override
    public void enterAssign_stmt(LittleParser.Assign_stmtContext ctx) {
        tempNode = new Node();
        String assign_Expression = ctx.assign_expr().getText();

        tempNode.variableType = "OPERATOR";
        tempNode.value = assign_Expression.substring(1,3);


        //System.out.println("enterAssign_stmt: " + tempNode.variableType + " " + tempNode.value);

        astTree = new AST();
        astTree.addNode(tempNode);
        AST_array.add(astTree);
//        astTree.print_AST(astTree.nodeNumber);

    }

    @Override public void exitBase_stmt(LittleParser.Base_stmtContext ctx) {
//        astTree.print_AST(astTree.nodeNumber);
//        System.out.println();
    }


    @Override
    public void enterAssign_expr(LittleParser.Assign_exprContext ctx) {
        String id = ctx.id().getText();
        tempNode = new Node();

        tempNode.variableType = "IDENTIFIER";
        tempNode.value = id;

        //System.out.println("enterAssign_expr: " + tempNode.variableType + " " + tempNode.value);
        astTree.addNode(tempNode);
//        astTree.print_AST(astTree.nodeNumber);

//        root.leftChild = temp;
//
//        System.out.println(root.leftChild.value);
    }

    @Override
    public void enterPrimary(LittleParser.PrimaryContext ctx) {
        String id;
        String INTLITERAL;
        String FLOATLITERAL;
        tempNode = new Node();

        if (ctx.id() != null) {
            id = ctx.id().getText();
           // System.out.print(id);
            tempNode.variableType = "IDENTIFIER";
            tempNode.value = id;

            //System.out.println("enterPrimary: " + tempNode.variableType + " " + tempNode.value);
            astTree.addNode(tempNode);
//            astTree.print_AST(astTree.nodeNumber);
        }

        if (ctx.INTLITERAL() != null) {
            INTLITERAL = ctx.INTLITERAL().getText();
           // System.out.print(INTLITERAL);
            tempNode.variableType = "INTLITERAL";
            tempNode.value = INTLITERAL;

            //System.out.println("enterPrimary: " + tempNode.variableType + " " + tempNode.value);
            astTree.addNode(tempNode);
//            astTree.print_AST(astTree.nodeNumber);
        }

        if (ctx.FLOATLITERAL() != null) {
            FLOATLITERAL = ctx.FLOATLITERAL().getText();
            // System.out.print(FLOATLITERAL);
            tempNode.variableType = "FLOATLITERAL";
            tempNode.value = FLOATLITERAL;

            //System.out.println("enterPrimary: " + tempNode.variableType + " " + tempNode.value);
            astTree.addNode(tempNode);
//            astTree.print_AST(astTree.nodeNumber);
        }
    }

    @Override
    public void enterCall_expr(LittleParser.Call_exprContext ctx) {
        String id = ctx.id().getText();

        System.out.print(id + " ");
    }

    @Override
    public void enterExpr_list_tail(LittleParser.Expr_list_tailContext ctx) {
        String expr;
        String expr_list_tail;

        if (ctx.expr() != null) {
            expr = ctx.expr().getText();
            System.out.print(expr);
        }
        if (ctx.expr_list_tail() != null) {
            expr_list_tail = ctx.expr_list_tail().getText();
            System.out.print(", " + expr_list_tail);
        }

    }

    @Override
    public void enterAddop(LittleParser.AddopContext ctx) {
        String addOP = ctx.getText();
        //System.out.print(addOP);
        tempNode = new Node();

        tempNode.variableType = "ADD_OPERATOR";
        tempNode.value = addOP;

        //System.out.println("enterAddop: " + tempNode.variableType + " " + tempNode.value);
        astTree.addNode(tempNode);
//        astTree.print_AST(astTree.nodeNumber);

    }

    @Override
    public void enterMulop(LittleParser.MulopContext ctx) {

        String mulOp = ctx.getText();
        //System.out.print(mulOp);
        tempNode = new Node();

        tempNode.variableType = "MUL_OPERATOR";
        tempNode.value = mulOp;

        //System.out.println("enterMulop: " + tempNode.variableType + " " + tempNode.value);
        astTree.addNode(tempNode);
//        astTree.print_AST(astTree.nodeNumber);
    }

    @Override
    public void enterRead_stmt(LittleParser.Read_stmtContext ctx) {
        String temp = ctx.getText();
        temp.replaceAll("READ","");
        temp.replaceAll("\\(","");
        temp.replaceAll("\\)","");
        temp.replaceAll(";","");

        String[] reads = ctx.id_list().getText().split(",");
        String variableType;
        int valueCounter;

        for (int j = 0; j < reads.length; j++) {
            for (Map.Entry<String, ArrayList<SymbolArrayEntry>> entries : table2scope.entrySet()) {
                // keyA = entries.getKey();
                valueCounter = entries.getValue().size();
                for (int i = 0; i < valueCounter; i++) {

                    if (Objects.equals(entries.getValue().get(i).getName(), reads[j])) {
                        variableType = entries.getValue().get(i).getType();
                        if (Objects.equals(variableType, "INT")) {
                            astTree = new AST();
                            tempNode = new Node();
                            tempNode.value = "sys readi " + reads[j];
                            astTree.addNode(tempNode);
                            AST_array.add(astTree);
                        } else if (Objects.equals(variableType, "FLOAT")) {
                            astTree = new AST();
                            tempNode = new Node();
                            tempNode.value = "sys readr " + reads[j];
                            astTree.addNode(tempNode);
                            AST_array.add(astTree);
                        } else if (Objects.equals(variableType, "STRING")) {
                            astTree = new AST();
                            tempNode = new Node();
                            tempNode.value = "sys reads " + reads[j];
                            astTree.addNode(tempNode);
                            AST_array.add(astTree);
                        }
                    }
                }
            }
        }

//        astTree.print_AST(astTree.nodeNumber);
//        System.out.println();
    }

    @Override
    public void enterWrite_stmt(LittleParser.Write_stmtContext ctx) {
        String temp = ctx.getText();
        temp.replaceAll("WRITE","");
        temp.replaceAll("\\(","");
        temp.replaceAll("\\)","");
        temp.replaceAll(";","");

        String[] writes = ctx.id_list().getText().split(",");
        String variableType;
        int valueCounter;

        for (int j = 0; j < writes.length; j++) {
            for (Map.Entry<String, ArrayList<SymbolArrayEntry>> entries : table2scope.entrySet()) {
                valueCounter = entries.getValue().size();
                for (int i = 0; i < valueCounter; i++) {

                    if (Objects.equals(entries.getValue().get(i).getName(), writes[j])) {
                        variableType = entries.getValue().get(i).getType();
                        if (Objects.equals(variableType, "INT")) {
                            astTree = new AST();
                            tempNode = new Node();
                            tempNode.value = "sys writei " + writes[j];
                            astTree.addNode(tempNode);
                            AST_array.add(astTree);
                        } else if (Objects.equals(variableType, "FLOAT")) {
                            astTree = new AST();
                            tempNode = new Node();
                            tempNode.value = "sys writer " + writes[j];
                            astTree.addNode(tempNode);
                            AST_array.add(astTree);
                        } else if (Objects.equals(variableType, "STRING")) {
                            astTree = new AST();
                            tempNode = new Node();
                            tempNode.value = "sys writes " + writes[j];
                            astTree.addNode(tempNode);
                            AST_array.add(astTree);
                        }
                    }
                }
            }
        }
    }

/*************************************** STEP 3 STUFF **************************************************/
    @Override
    public void enterProgram(LittleParser.ProgramContext ctx) {
        scopeStack.push("GLOBAL"); //push level name to stack

        symbolTable = new ArrayList<>();
        symbolTableIndex = 0;
        table2scope.put(scopeStack.get(stackLevel), symbolTable);
    }

    @Override
    public void exitProgram(LittleParser.ProgramContext ctx) {
        scopeStack.pop();
    }

    @Override
    public void enterString_decl(LittleParser.String_declContext ctx) {
        String name = ctx.id().getText();
        String type = "STRING";
        String value = ctx.str().getText();

        //step 3
        SymbolArrayEntry symbolEntry = new SymbolArrayEntry(name, type, value);
        isThereError = checkSymbolTable(symbolTable, symbolEntry);
        symbolTable.add(symbolTableIndex, symbolEntry);
        table2scope.put(scopeStack.get(stackLevel), symbolTable);
        symbolTableIndex++;
    }

    @Override
    public void enterVar_decl(LittleParser.Var_declContext ctx) {
        String type = null;
        String[] names = new String[0];

        //type gets updated to INT or FLOAT
        if (ctx.getText().startsWith("INT")) {
            names = ctx.getText().substring(3).replace(";", "").split(",");
            type = "INT";
        } else if (ctx.getText().startsWith("FLOAT")) {
            names = ctx.getText().substring(5).replace(";", "").split(",");
            type = "FLOAT";
        }

        //names are added to names array
        for (int i = 0; i < names.length; i++) {
            SymbolArrayEntry symbolEntry = new SymbolArrayEntry(names[i], type);
            isThereError = checkSymbolTable(symbolTable, symbolEntry);
            symbolTable.add(symbolTableIndex, symbolEntry);
            symbolTableIndex++;
        }
        table2scope.put(scopeStack.get(stackLevel), symbolTable);
    }

    @Override
    public void enterParam_decl_list(LittleParser.Param_decl_listContext ctx) {
        String type = null;
        String[] names = new String[0];

        if (ctx.getText().startsWith("INT")) {
            names = ctx.getText().substring(3).replace(")", "").split(",INT");
            type = "INT";
        } else if (ctx.getText().startsWith("FLOAT")) {
            names = ctx.getText().substring(5).replace(")", "").split(",FLOAT");
            type = "FLOAT";
        }

        for (int i = 0; i < names.length; i++) {
            SymbolArrayEntry symbolEntry = new SymbolArrayEntry(names[i], type);
            isThereError = checkSymbolTable(symbolTable, symbolEntry);
            symbolTable.add(symbolTableIndex, symbolEntry);
            symbolTableIndex++;
        }
        table2scope.put(scopeStack.get(stackLevel), symbolTable);

    }

    @Override
    public void enterFunc_decl(LittleParser.Func_declContext ctx) {
        stackLevel++;
        String functionName = ctx.id().getText();
        scopeStack.push(functionName);
        symbolTable = new ArrayList<>();
        symbolTableIndex = 0;
    }

    @Override
    public void exitFunc_decl(LittleParser.Func_declContext ctx) {
        scopeStack.pop();
        stackLevel--;
    }

    @Override
    public void enterWhile_stmt(LittleParser.While_stmtContext ctx) {
        stackLevel++;
        scopeStack.push("BLOCK " + blockCount);
        blockCount++;

        symbolTable = new ArrayList<>();
        symbolTableIndex = 0;

        table2scope.put(scopeStack.get(stackLevel), symbolTable);
    }

    @Override
    public void exitWhile_stmt(LittleParser.While_stmtContext ctx) {
        scopeStack.pop();
        stackLevel--;
    }

    @Override
    public void enterIf_stmt(LittleParser.If_stmtContext ctx) {
        if (ctx.getText().contains("ELSE")) {
            containsElse = true;
        }
        stackLevel++;
        scopeStack.push("BLOCK " + blockCount);
        blockCount++;

        symbolTable = new ArrayList<>();
        symbolTableIndex = 0;

        table2scope.put(scopeStack.get(stackLevel), symbolTable);
    }

    @Override
    public void exitIf_stmt(LittleParser.If_stmtContext ctx) {
        if (containsElse) {
            scopeStack.pop();
            containsElse = false;
            stackLevel--;
        }
        scopeStack.pop();
        stackLevel--;
    }

    @Override
    public void enterElse_part(LittleParser.Else_partContext ctx) {
        if (containsElse) {
            stackLevel++;
            scopeStack.push("BLOCK " + blockCount);
            blockCount++;

            symbolTable = new ArrayList<>();
            symbolTableIndex = 0;

            table2scope.put(scopeStack.get(stackLevel), symbolTable);
        }
    }


    public boolean checkSymbolTable(ArrayList<SymbolArrayEntry> scopedTable, SymbolArrayEntry scopedEntry) {
        for (int i = 0; i < scopedTable.size(); i++) {
            if (Objects.equals(scopedTable.get(i).getType(), scopedEntry.getType()) &&
                    Objects.equals(scopedTable.get(i).getName(), scopedEntry.getName())) {
                System.out.println("DECLARATION ERROR " + scopedEntry.getName());
                System.exit(0);
                return true;
            }
        }
        return false;
    }

    public void prettyPrint() {
        if (isThereError) {
//            System.out.println("Declaration Error " + errorStatement);
        } else {

            String key;
            int valueCounter;
            for (Map.Entry<String, ArrayList<SymbolArrayEntry>> entries : table2scope.entrySet()) {
                key = entries.getKey();
                if (entries.getValue().size() == 0) {
                    System.out.println("Symbol table " + key + "\n");
                    continue;
                }
                valueCounter = entries.getValue().size();
                System.out.println("Symbol table " + key);
                for (int i = 0; i < valueCounter; i++) {
                    if (Objects.equals(entries.getValue().get(i).getType(), "STRING")) {
                        System.out.println("name " + entries.getValue().get(i).getName() +
                                " type " + entries.getValue().get(i).getType() +
                                " value " + entries.getValue().get(i).getValue());
                    } else {
                        System.out.println("name " + entries.getValue().get(i).getName() +
                                " type " + entries.getValue().get(i).getType());
                    }
                }
                System.out.println();
            }
        }
    }

    public void newPrint() {
        if (isThereError) {
//            System.out.println("Declaration Error " + errorStatement);
        } else {

            String key;
            int valueCounter;
            for (Map.Entry<String, ArrayList<SymbolArrayEntry>> entries : table2scope.entrySet()) {
                key = entries.getKey();
                if (entries.getValue().size() == 0) {
                    continue;
                }
                valueCounter = entries.getValue().size();

                if (Objects.equals(key, "GLOBAL")) {
                    for (int i = 0; i < valueCounter; i++) {
                        if ((Objects.equals(entries.getValue().get(i).getType(), "FLOAT")) ||
                                (Objects.equals(entries.getValue().get(i).getType(), "INT"))) {
                            System.out.println("var " + entries.getValue().get(i).name);
                        } else {
                            System.out.print("str " + entries.getValue().get(i).name);
                            System.out.println(" " + entries.getValue().get(i).value);
                        }
                    }
                }
            }
        }
    }

    public void process3AC() {
        char registerName = 'r';
        int registerCounter = 0;
        int previousRegister = 0;
        String assignOperator;
        String memLocation1, memLocation2, memLocation3;
        String value;
        int valueCounter;

        for (int i = 0; i < AST_array.size(); i++) {
            //AST_array.get(i).print_AST(astTree.nodeNumber);

            //this means simple assignment    example a := 3
            if (AST_array.get(i).root.leftChild != null &&
                AST_array.get(i).root.rightChild == null) {
                assignOperator = "move";
                memLocation1 = AST_array.get(i).root.leftChild.value;
                value = AST_array.get(i).tempNode.value;
                System.out.println(assignOperator + " " + value + " " + registerName + registerCounter);
                System.out.println(assignOperator + " " + registerName + registerCounter + " " + memLocation1);
                registerCounter++;
            }

            if (AST_array.get(i).root.leftChild == null) {
                System.out.println(AST_array.get(i).root.value);
            }

            //math operation
            if (AST_array.get(i).root.leftChild != null &&
                AST_array.get(i).root.rightChild != null) {
                    boolean isFLOAT = false;
                    memLocation1 = AST_array.get(i).root.leftChild.value;
                    memLocation2 = AST_array.get(i).root.rightChild.leftChild.value;
                    memLocation3 = AST_array.get(i).root.rightChild.rightChild.value;



                if (Objects.equals(AST_array.get(i).root.rightChild.leftChild.variableType, "INTLITERAL") ||
                    Objects.equals(AST_array.get(i).root.rightChild.leftChild.variableType, "FLOATLITERAL")) {

                    System.out.println("move " + memLocation2 + " " + registerName + registerCounter);
                    previousRegister = registerCounter;
                    registerCounter++;
                    System.out.println("move " + memLocation3 + " " + registerName + registerCounter);

                    memLocation2 = "r" + String.valueOf(previousRegister);

                } else if (Objects.equals(AST_array.get(i).root.rightChild.rightChild.variableType, "INTLITERAL") ||
                        Objects.equals(AST_array.get(i).root.rightChild.rightChild.variableType, "FLOATLITERAL")) {

                    System.out.println("move " + memLocation3 + " " + registerName + registerCounter);
                    previousRegister = registerCounter;
                    registerCounter++;
                    System.out.println("move " + memLocation2 + " " + registerName + registerCounter);

                    memLocation3 = "r" + String.valueOf(previousRegister);
                } else {
                    System.out.println("move " + memLocation2  + " " + registerName + registerCounter);
                }


                    for (Map.Entry<String, ArrayList<SymbolArrayEntry>> entries : table2scope.entrySet()) {
                        valueCounter = entries.getValue().size();
                        for (int j = 0; j < valueCounter; j++) {
                            if (Objects.equals(entries.getValue().get(j).getName(), AST_array.get(i).root.leftChild.value)) {
                                if (Objects.equals(entries.getValue().get(j).getType(), "FLOAT")) {
                                    isFLOAT = true;
                                }
                            }
                        }
                    }


                    if (Objects.equals(AST_array.get(i).root.rightChild.value, "+")) {
                        System.out.print("add");
                    }

                    if (Objects.equals(AST_array.get(i).root.rightChild.value, "-")) {
                        System.out.print("sub");
                    }

                    if (Objects.equals(AST_array.get(i).root.rightChild.value, "*")) {
                        System.out.print("mul");
                    }

                    if (Objects.equals(AST_array.get(i).root.rightChild.value, "/")) {
                        System.out.print("div");
                    }

                    if(isFLOAT) {
                        System.out.print("r ");
                    } else {
                        System.out.print("i ");
                    }

                System.out.println(memLocation3 + " " + registerName + registerCounter);
                System.out.println("move " + registerName + registerCounter + " " + memLocation1);
                registerCounter++;
            }
        }
    }
}


