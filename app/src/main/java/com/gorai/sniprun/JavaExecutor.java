package com.gorai.sniprun;

import android.content.Context;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaExecutor {
    
    private final Context context;
    private final JavaInterpreter interpreter;
    
    public static class ExecutionResult {
        private final String output;
        private final boolean success;
        private final String errorMessage;
        private final String compilationOutput;
        
        public ExecutionResult(String output, boolean success, String errorMessage, String compilationOutput) {
            this.output = output;
            this.success = success;
            this.errorMessage = errorMessage;
            this.compilationOutput = compilationOutput;
        }
        
        public String getOutput() { return output; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public String getCompilationOutput() { return compilationOutput; }
    }
    
    public JavaExecutor(Context context) {
        this.context = context;
        this.interpreter = new JavaInterpreter();
    }
    
    public ExecutionResult executeJavaCode(String code) {
        try {
            if (code == null || code.trim().isEmpty()) {
                return new ExecutionResult("", false, "Code is empty", null);
            }
            
            String result = compileAndExecute(code);
            return new ExecutionResult(result, true, null, "Compilation successful");
            
        } catch (Exception e) {
            return new ExecutionResult("", false, e.getMessage(), "Compilation failed");
        }
    }
    
    public String compileAndExecute(String sourceCode) {
        try {
            if (sourceCode == null || sourceCode.trim().isEmpty()) {
                return "Error: Source code is empty";
            }
            
            JavaParser parser = new JavaParser();
            ParseResult<CompilationUnit> parseResult = parser.parse(sourceCode);
            
            if (!parseResult.isSuccessful()) {
                StringBuilder errors = new StringBuilder("Syntax Errors:\n");
                parseResult.getProblems().forEach(problem -> 
                    errors.append("Line ").append(problem.getLocation().orElse(null))
                          .append(": ").append(problem.getMessage()).append("\n"));
                return errors.toString();
            }
            
            String className = extractClassName(sourceCode);
            if (className == null) {
                className = "TempClass";
                sourceCode = wrapInClass(sourceCode, className);
            }
            
            return interpreter.executeJavaCode(sourceCode, className);
            
        } catch (Exception e) {
            return "Compilation Error: " + e.getMessage();
        }
    }
    
    private String extractClassName(String sourceCode) {
        Pattern classPattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher matcher = classPattern.matcher(sourceCode);
        return matcher.find() ? matcher.group(1) : null;
    }
    
    private String wrapInClass(String code, String className) {
        return "public class " + className + " {\n" +
               "    public static void main(String[] args) {\n" +
               "        " + code.replaceAll("\n", "\n        ") + "\n" +
               "    }\n" +
               "}";
    }
    
    private static class JavaInterpreter {
        
        private final Map<String, Object> variables = new HashMap<>();
        private final StringBuilder output = new StringBuilder();
        private final Pattern PRINT_PATTERN = Pattern.compile("System\\.out\\.print(?:ln)?\\s*\\(([^)]+)\\)");
        private final Pattern VARIABLE_DECLARATION = Pattern.compile("(\\w+)\\s+(\\w+)\\s*=\\s*(.+?)(?:;|$)");
        private final Pattern ASSIGNMENT = Pattern.compile("(\\w+)\\s*=\\s*(.+?)(?:;|$)");
        
        private PrintStream originalOut;
        private PrintStream originalErr;
        private ByteArrayOutputStream outputCapture;
        
        public String executeJavaCode(String sourceCode, String className) {
            output.setLength(0);
            variables.clear();
            
            try {
                startCapture();
                
                String mainBody = extractMainMethodBody(sourceCode);
                if (mainBody != null) {
                    executeStatements(mainBody);
                } else {
                    executeStatements(sourceCode);
                }
                
                String result = output.toString();
                return result.isEmpty() ? "Code executed successfully (no output)" : result;
                
            } catch (Exception e) {
                return "Runtime error: " + e.getMessage();
            } finally {
                stopCapture();
            }
        }
        
        private String extractMainMethodBody(String sourceCode) {
            try {
                JavaParser parser = new JavaParser();
                ParseResult<CompilationUnit> parseResult = parser.parse(sourceCode);
                
                if (parseResult.isSuccessful()) {
                    CompilationUnit cu = parseResult.getResult().get();
                    
                    return cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                        .flatMap(cls -> cls.findAll(MethodDeclaration.class).stream())
                        .filter(method -> method.getNameAsString().equals("main"))
                        .filter(method -> method.isStatic())
                        .findFirst()
                        .map(method -> method.getBody().orElse(new BlockStmt()))
                        .map(Node::toString)
                        .map(body -> body.substring(1, body.length() - 1).trim())
                        .orElse(null);
                }
            } catch (Exception e) {
            }
            
            Pattern mainPattern = Pattern.compile(
                "public\\s+static\\s+void\\s+main\\s*\\([^)]*\\)\\s*\\{([^}]*(?:\\{[^}]*\\}[^}]*)*)\\}",
                Pattern.DOTALL);
            Matcher matcher = mainPattern.matcher(sourceCode);
            return matcher.find() ? matcher.group(1).trim() : null;
        }
        
        private void executeStatements(String code) {
            String[] lines = code.split("\\n");
            
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("import")) {
                    executeStatement(line);
                }
            }
        }
        
        private void executeStatement(String statement) {
            statement = statement.trim();
            if (statement.endsWith(";")) {
                statement = statement.substring(0, statement.length() - 1);
            }
            
            if (PRINT_PATTERN.matcher(statement).find()) {
                executePrintStatement(statement);
            } else if (statement.contains("for") && statement.contains("(")) {
                handleForLoop(statement);
            } else if (VARIABLE_DECLARATION.matcher(statement).matches()) {
                executeVariableDeclaration(statement);
            } else if (ASSIGNMENT.matcher(statement).matches()) {
                executeAssignment(statement);
            } else if (statement.contains(".add(") || statement.contains(".put(")) {
                executeCollectionOperation(statement);
            }
        }
        
        private void executePrintStatement(String statement) {
            Matcher matcher = PRINT_PATTERN.matcher(statement);
            if (matcher.find()) {
                String expression = matcher.group(1);
                Object value = evaluateExpression(expression);
                output.append(value).append("\n");
            }
        }
        
        private void executeVariableDeclaration(String statement) {
            Matcher matcher = VARIABLE_DECLARATION.matcher(statement);
            if (matcher.find()) {
                String type = matcher.group(1);
                String name = matcher.group(2);
                String value = matcher.group(3);
                
                Object evaluatedValue = evaluateExpression(value);
                
                if ("List".equals(type) || "ArrayList".equals(type)) {
                    variables.put(name, new ArrayList<>());
                } else if ("Map".equals(type) || "HashMap".equals(type)) {
                    variables.put(name, new HashMap<>());
                } else {
                    variables.put(name, evaluatedValue);
                }
            }
        }
        
        private void executeAssignment(String statement) {
            Matcher matcher = ASSIGNMENT.matcher(statement);
            if (matcher.find()) {
                String name = matcher.group(1);
                String value = matcher.group(2);
                Object evaluatedValue = evaluateExpression(value);
                variables.put(name, evaluatedValue);
            }
        }
        
        private Object evaluateExpression(String expression) {
            expression = expression.trim();
            
            if (expression.startsWith("\"") && expression.endsWith("\"")) {
                return expression.substring(1, expression.length() - 1);
            }
            
            if (expression.matches("\\d+")) {
                return Integer.parseInt(expression);
            }
            
            if (expression.matches("\\d+\\.\\d+")) {
                return Double.parseDouble(expression);
            }
            
            if (expression.equals("true") || expression.equals("false")) {
                return Boolean.parseBoolean(expression);
            }
            
            if (expression.startsWith("new ArrayList<>()")) {
                return new ArrayList<>();
            }
            
            if (expression.startsWith("new HashMap<>()")) {
                return new HashMap<>();
            }
            
            if (expression.startsWith("new java.util.Date()")) {
                return new java.util.Date();
            }
            
            if (variables.containsKey(expression)) {
                return variables.get(expression);
            }
            
            if (expression.contains("+")) {
                return evaluateAddition(expression);
            }
            
            if (expression.contains(".")) {
                return evaluateMethodCall(expression);
            }
            
            return expression;
        }
        
        private Object evaluateAddition(String expression) {
            String[] parts = expression.split("\\+");
            if (parts.length == 2) {
                Object left = evaluateExpression(parts[0].trim());
                Object right = evaluateExpression(parts[1].trim());
                
                if (left instanceof Integer && right instanceof Integer) {
                    return (Integer) left + (Integer) right;
                } else {
                    return left.toString() + right.toString();
                }
            }
            return expression;
        }
        
        private Object evaluateMethodCall(String expression) {
            if (expression.contains(".toUpperCase()")) {
                String varName = expression.substring(0, expression.indexOf("."));
                Object value = variables.get(varName);
                if (value instanceof String) {
                    return ((String) value).toUpperCase();
                }
            }
            
            if (expression.contains(".length()")) {
                String varName = expression.substring(0, expression.indexOf("."));
                Object value = variables.get(varName);
                if (value instanceof String) {
                    return ((String) value).length();
                }
            }
            
            return expression;
        }
        
        private void handleForLoop(String statement) {
            Pattern forPattern = Pattern.compile("for\\s*\\(\\s*(\\w+)\\s+(\\w+)\\s*=\\s*(\\d+);\\s*(\\w+)\\s*<=\\s*(\\d+);\\s*(\\w+)\\+\\+\\s*\\)");
            Matcher matcher = forPattern.matcher(statement);
            
            if (matcher.find()) {
                String varName = matcher.group(2);
                int start = Integer.parseInt(matcher.group(3));
                int end = Integer.parseInt(matcher.group(5));
                
                for (int i = start; i <= end; i++) {
                    variables.put(varName, i);
                }
            }
            
            Pattern enhancedForPattern = Pattern.compile("for\\s*\\(\\s*(\\w+)\\s+(\\w+)\\s*:\\s*(\\w+)\\s*\\)");
            Matcher enhancedMatcher = enhancedForPattern.matcher(statement);
            
            if (enhancedMatcher.find()) {
                String itemName = enhancedMatcher.group(2);
                String collectionName = enhancedMatcher.group(3);
                Object collection = variables.get(collectionName);
                
                if (collection instanceof List) {
                    for (Object item : (List<?>) collection) {
                        variables.put(itemName, item);
                    }
                }
            }
        }
        
        private void executeCollectionOperation(String statement) {
            if (statement.contains(".add(")) {
                Pattern addPattern = Pattern.compile("(\\w+)\\.add\\(([^)]+)\\)");
                Matcher matcher = addPattern.matcher(statement);
                
                if (matcher.find()) {
                    String listName = matcher.group(1);
                    String value = matcher.group(2);
                    Object evaluatedValue = evaluateExpression(value);
                    
                    Object list = variables.get(listName);
                    if (list instanceof List) {
                        ((List<Object>) list).add(evaluatedValue);
                    }
                }
            }
            
            if (statement.contains(".put(")) {
                Pattern putPattern = Pattern.compile("(\\w+)\\.put\\(([^,]+),\\s*([^)]+)\\)");
                Matcher matcher = putPattern.matcher(statement);
                
                if (matcher.find()) {
                    String mapName = matcher.group(1);
                    String key = matcher.group(2);
                    String value = matcher.group(3);
                    
                    Object keyObj = evaluateExpression(key);
                    Object valueObj = evaluateExpression(value);
                    
                    Object map = variables.get(mapName);
                    if (map instanceof Map) {
                        ((Map<Object, Object>) map).put(keyObj, valueObj);
                    }
                }
            }
        }
        
        private void startCapture() {
            originalOut = System.out;
            originalErr = System.err;
            outputCapture = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputCapture));
            System.setErr(new PrintStream(outputCapture));
        }
        
        private String getOutput() {
            if (outputCapture != null) {
                return outputCapture.toString();
            }
            return "";
        }
        
        private void stopCapture() {
            if (originalOut != null) {
                System.setOut(originalOut);
            }
            if (originalErr != null) {
                System.setErr(originalErr);
            }
            if (outputCapture != null) {
                try {
                    outputCapture.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
