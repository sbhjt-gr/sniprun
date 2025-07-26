package com.gorai.sniprun.compiler;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AndroidCompatibleJavaCompiler {
    
    private static final String TAG = "AndroidCompatibleJavaCompiler";
    private final Context context;
    private final File workingDirectory;
    private final File sourceDirectory;
    private final File classDirectory;
    private final File tempDirectory;
    
    public static class CompilationResult {
        private final boolean success;
        private final String output;
        private final String errorMessage;
        private final List<String> compilationErrors;
        private final long executionTimeMs;
        
        public CompilationResult(boolean success, String output, String errorMessage, 
                               List<String> compilationErrors, long executionTimeMs) {
            this.success = success;
            this.output = output;
            this.errorMessage = errorMessage;
            this.compilationErrors = compilationErrors != null ? compilationErrors : new ArrayList<>();
            this.executionTimeMs = executionTimeMs;
        }
        
        public boolean isSuccess() { return success; }
        public String getOutput() { return output; }
        public String getErrorMessage() { return errorMessage; }
        public List<String> getCompilationErrors() { return compilationErrors; }
        public long getExecutionTimeMs() { return executionTimeMs; }
    }
    
    public AndroidCompatibleJavaCompiler(Context context) {
        this.context = context;
        this.workingDirectory = new File(context.getFilesDir(), "java_compiler");
        this.sourceDirectory = new File(workingDirectory, "src");
        this.classDirectory = new File(workingDirectory, "classes");
        this.tempDirectory = new File(workingDirectory, "temp");
        
        initializeDirectories();
    }
    
    private void initializeDirectories() {
        try {
            if (!workingDirectory.exists()) workingDirectory.mkdirs();
            if (!sourceDirectory.exists()) sourceDirectory.mkdirs();
            if (!classDirectory.exists()) classDirectory.mkdirs();
            if (!tempDirectory.exists()) tempDirectory.mkdirs();
            
            cleanupDirectories();
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize directories", e);
        }
    }
    
    private void cleanupDirectories() {
        try {
            deleteDirectoryContents(classDirectory);
            deleteDirectoryContents(tempDirectory);
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup directories", e);
        }
    }
    
    private void deleteDirectoryContents(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectoryContents(file);
                    }
                    file.delete();
                }
            }
        }
    }
    
    public CompilationResult compileAndExecute(String sourceCode) {
        long startTime = System.currentTimeMillis();
        
        try {
            if (sourceCode == null || sourceCode.trim().isEmpty()) {
                return new CompilationResult(false, "", "Source code is empty", null, 0);
            }
            
            String className = extractClassName(sourceCode);
            if (className == null) {
                className = "TempClass";
                sourceCode = wrapInClass(sourceCode, className);
            }
            
            // Validate basic syntax first
            List<String> syntaxErrors = validateSyntax(sourceCode);
            if (!syntaxErrors.isEmpty()) {
                return new CompilationResult(false, "", "Syntax validation failed", 
                                           syntaxErrors, System.currentTimeMillis() - startTime);
            }
            
            String output = executeWithInterpreter(sourceCode, className);
            
            return new CompilationResult(true, output, null, null, 
                                       System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            Log.e(TAG, "Execution error", e);
            return new CompilationResult(false, "", "Internal error: " + e.getMessage(), 
                                       null, System.currentTimeMillis() - startTime);
        } finally {
            cleanup();
        }
    }
    
    private String extractClassName(String sourceCode) {
        Pattern classPattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher matcher = classPattern.matcher(sourceCode);
        return matcher.find() ? matcher.group(1) : null;
    }
    
    private String wrapInClass(String code, String className) {
        // Check if it's a complete main method or just statements
        if (code.contains("public static void main")) {
            return "public class " + className + " {\n" + code + "\n}";
        } else {
            return "public class " + className + " {\n" +
                   "    public static void main(String[] args) {\n" +
                   "        " + code.replaceAll("\n", "\n        ") + "\n" +
                   "    }\n" +
                   "}";
        }
    }
    
    private List<String> validateSyntax(String sourceCode) {
        List<String> errors = new ArrayList<>();
        
        // Basic syntax validation
        int openBraces = 0;
        int closeBraces = 0;
        int openParens = 0;
        int closeParens = 0;
        boolean inString = false;
        boolean inChar = false;
        boolean escape = false;
        
        char[] chars = sourceCode.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            
            if (escape) {
                escape = false;
                continue;
            }
            
            if (c == '\\') {
                escape = true;
                continue;
            }
            
            if (!inChar && c == '"') {
                inString = !inString;
                continue;
            }
            
            if (!inString && c == '\'') {
                inChar = !inChar;
                continue;
            }
            
            if (!inString && !inChar) {
                switch (c) {
                    case '{':
                        openBraces++;
                        break;
                    case '}':
                        closeBraces++;
                        break;
                    case '(':
                        openParens++;
                        break;
                    case ')':
                        closeParens++;
                        break;
                }
            }
        }
        
        if (openBraces != closeBraces) {
            errors.add("Mismatched braces: " + openBraces + " open, " + closeBraces + " close");
        }
        
        if (openParens != closeParens) {
            errors.add("Mismatched parentheses: " + openParens + " open, " + closeParens + " close");
        }
        
        if (inString) {
            errors.add("Unclosed string literal");
        }
        
        if (inChar) {
            errors.add("Unclosed character literal");
        }
        
        return errors;
    }
    
    private String executeWithInterpreter(String sourceCode, String className) throws Exception {
        AndroidJavaInterpreter interpreter = new AndroidJavaInterpreter();
        return interpreter.execute(sourceCode, className);
    }
    
    private void cleanup() {
        try {
            cleanupDirectories();
        } catch (Exception e) {
            Log.e(TAG, "Cleanup failed", e);
        }
    }
    
    private static class AndroidJavaInterpreter {
        private final Map<String, Object> variables = new HashMap<>();
        private final StringBuilder output = new StringBuilder();
        private final Pattern PRINT_PATTERN = Pattern.compile("System\\.out\\.print(?:ln)?\\s*\\(([^)]+)\\)");
        private final Pattern VARIABLE_DECLARATION = Pattern.compile("(\\w+)\\s+(\\w+)\\s*=\\s*(.+?)(?:;|$)");
        private final Pattern ASSIGNMENT = Pattern.compile("(\\w+)\\s*=\\s*(.+?)(?:;|$)");
        private final Pattern METHOD_CALL = Pattern.compile("(\\w+)\\.(\\w+)\\(([^)]*)\\)");
        
        public String execute(String sourceCode, String className) {
            output.setLength(0);
            variables.clear();
            
            try {
                String mainBody = extractMainMethodBody(sourceCode);
                if (mainBody != null) {
                    executeStatements(mainBody);
                } else {
                    executeStatements(sourceCode);
                }
                
                String result = output.toString();
                return result.isEmpty() ? "Program executed successfully (no output)" : result;
                
            } catch (Exception e) {
                return "Runtime error: " + e.getMessage();
            }
        }
        
        private String extractMainMethodBody(String sourceCode) {
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
                if (!line.isEmpty() && !line.startsWith("import") && !line.startsWith("//")) {
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
            } else if (statement.contains("if") && statement.contains("(")) {
                handleIfStatement(statement);
            } else if (VARIABLE_DECLARATION.matcher(statement).matches()) {
                executeVariableDeclaration(statement);
            } else if (ASSIGNMENT.matcher(statement).matches()) {
                executeAssignment(statement);
            } else if (statement.contains(".add(") || statement.contains(".put(")) {
                executeCollectionOperation(statement);
            } else if (METHOD_CALL.matcher(statement).find()) {
                executeMethodCall(statement);
            }
        }
        
        private void executePrintStatement(String statement) {
            // Custom parsing for print statements to handle nested parentheses
            String printPrefix = "System.out.print";
            int startIndex = statement.indexOf(printPrefix);
            if (startIndex != -1) {
                int openParenIndex = statement.indexOf('(', startIndex);
                if (openParenIndex != -1) {
                    String expression = extractBalancedParentheses(statement, openParenIndex);
                    if (expression != null) {
                        android.util.Log.d("AndroidInterpreter", "Print expression: " + expression);
                        Object value = evaluateExpression(expression);
                        android.util.Log.d("AndroidInterpreter", "Evaluated value: " + value);
                        output.append(value).append("\n");
                    }
                }
            }
        }
        
        private String extractBalancedParentheses(String text, int startIndex) {
            int parenCount = 0;
            int i = startIndex;
            
            // Find the opening parenthesis
            while (i < text.length() && text.charAt(i) != '(') {
                i++;
            }
            
            if (i >= text.length()) return null;
            
            i++; // Move past the opening parenthesis
            int contentStart = i;
            parenCount = 1;
            
            while (i < text.length() && parenCount > 0) {
                char c = text.charAt(i);
                if (c == '(') {
                    parenCount++;
                } else if (c == ')') {
                    parenCount--;
                }
                i++;
            }
            
            if (parenCount == 0) {
                return text.substring(contentStart, i - 1); // -1 to exclude the closing parenthesis
            }
            
            return null;
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
        
        private void executeMethodCall(String statement) {
            Matcher matcher = METHOD_CALL.matcher(statement);
            if (matcher.find()) {
                String objectName = matcher.group(1);
                String methodName = matcher.group(2);
                String args = matcher.group(3);
                
                Object obj = variables.get(objectName);
                if (obj != null) {
                    try {
                        if ("add".equals(methodName) && obj instanceof List) {
                            Object value = evaluateExpression(args);
                            ((List<Object>) obj).add(value);
                        } else if ("put".equals(methodName) && obj instanceof Map) {
                            String[] parts = args.split(",");
                            if (parts.length == 2) {
                                Object key = evaluateExpression(parts[0].trim());
                                Object value = evaluateExpression(parts[1].trim());
                                ((Map<Object, Object>) obj).put(key, value);
                            }
                        }
                    } catch (Exception e) {
                        // Ignore method call errors
                    }
                }
            }
        }
        
        private void handleForLoop(String statement) {
            // Simple for loop handling
            Pattern forPattern = Pattern.compile("for\\s*\\(\\s*(\\w+)\\s+(\\w+)\\s*=\\s*(\\d+);\\s*(\\w+)\\s*<=\\s*(\\d+);\\s*(\\w+)\\+\\+\\s*\\)");
            Matcher matcher = forPattern.matcher(statement);
            
            if (matcher.find()) {
                String varName = matcher.group(2);
                int start = Integer.parseInt(matcher.group(3));
                int end = Integer.parseInt(matcher.group(5));
                
                for (int i = start; i <= end; i++) {
                    variables.put(varName, i);
                    // For simplicity, we just store the final values
                }
            }
        }
        
        private void handleIfStatement(String statement) {
            // Basic if statement handling - simplified
            Pattern ifPattern = Pattern.compile("if\\s*\\(([^)]+)\\)");
            Matcher matcher = ifPattern.matcher(statement);
            
            if (matcher.find()) {
                String condition = matcher.group(1);
                // For simplicity, assume conditions are true
                // In a real implementation, you'd evaluate the condition
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
        
        private Object evaluateExpression(String expression) {
            expression = expression.trim();
            
            // String literals
            if (expression.startsWith("\"") && expression.endsWith("\"")) {
                return expression.substring(1, expression.length() - 1);
            }
            
            // Integer literals
            if (expression.matches("\\d+")) {
                return Integer.parseInt(expression);
            }
            
            // Double literals
            if (expression.matches("\\d+\\.\\d+")) {
                return Double.parseDouble(expression);
            }
            
            // Boolean literals
            if (expression.equals("true") || expression.equals("false")) {
                return Boolean.parseBoolean(expression);
            }
            
            // Object creation
            if (expression.startsWith("new ArrayList<>()")) {
                return new ArrayList<>();
            }
            
            if (expression.startsWith("new HashMap<>()")) {
                return new HashMap<>();
            }
            
            // Variable references
            if (variables.containsKey(expression)) {
                return variables.get(expression);
            }
            
            // Complex expressions with parentheses - evaluate them first
            if (expression.contains("(") && expression.contains(")")) {
                return evaluateComplexExpression(expression);
            }
            
            // Arithmetic operations
            if (expression.contains("+")) {
                return evaluateAddition(expression);
            }
            
            if (expression.contains("-")) {
                return evaluateSubtraction(expression);
            }
            
            if (expression.contains("*")) {
                return evaluateMultiplication(expression);
            }
            
            if (expression.contains("/")) {
                return evaluateDivision(expression);
            }
            
            // Method calls
            if (expression.contains(".")) {
                return evaluateMethodCall(expression);
            }
            
            return expression;
        }
        
        private Object evaluateComplexExpression(String expression) {
            // Handle simple parentheses like "Test output: " + (5 + 3)
            Pattern parenthesesPattern = Pattern.compile("(.*)\\(([^()]+)\\)(.*)");
            Matcher matcher = parenthesesPattern.matcher(expression);
            
            if (matcher.find()) {
                String before = matcher.group(1);
                String inside = matcher.group(2);
                String after = matcher.group(3);
                
                // Evaluate the expression inside parentheses
                Object innerResult = evaluateExpression(inside);
                
                // Reconstruct the expression with the evaluated result
                String newExpression = before + innerResult + after;
                return evaluateExpression(newExpression);
            }
            
            return expression;
        }
        
        private Object evaluateAddition(String expression) {
            // Find the last + that's not inside parentheses or quotes
            int lastPlusIndex = findLastTopLevelPlus(expression);
            
            if (lastPlusIndex != -1) {
                String left = expression.substring(0, lastPlusIndex).trim();
                String right = expression.substring(lastPlusIndex + 1).trim();
                
                Object leftVal = evaluateExpression(left);
                Object rightVal = evaluateExpression(right);
                
                if (leftVal instanceof Integer && rightVal instanceof Integer) {
                    return (Integer) leftVal + (Integer) rightVal;
                } else if (leftVal instanceof Double || rightVal instanceof Double) {
                    double leftDouble = (leftVal instanceof Double) ? (Double) leftVal : ((Integer) leftVal).doubleValue();
                    double rightDouble = (rightVal instanceof Double) ? (Double) rightVal : ((Integer) rightVal).doubleValue();
                    return leftDouble + rightDouble;
                } else {
                    return leftVal.toString() + rightVal.toString();
                }
            }
            return expression;
        }
        
        private int findLastTopLevelPlus(String expression) {
            int parenDepth = 0;
            boolean inString = false;
            boolean inChar = false;
            boolean escape = false;
            
            for (int i = expression.length() - 1; i >= 0; i--) {
                char c = expression.charAt(i);
                
                if (escape) {
                    escape = false;
                    continue;
                }
                
                if (c == '\\') {
                    escape = true;
                    continue;
                }
                
                if (!inChar && c == '"') {
                    inString = !inString;
                    continue;
                }
                
                if (!inString && c == '\'') {
                    inChar = !inChar;
                    continue;
                }
                
                if (!inString && !inChar) {
                    if (c == ')') {
                        parenDepth++;
                    } else if (c == '(') {
                        parenDepth--;
                    } else if (c == '+' && parenDepth == 0) {
                        return i;
                    }
                }
            }
            return -1;
        }
        
        private Object evaluateSubtraction(String expression) {
            String[] parts = expression.split("\\-");
            if (parts.length == 2) {
                Object left = evaluateExpression(parts[0].trim());
                Object right = evaluateExpression(parts[1].trim());
                
                if (left instanceof Integer && right instanceof Integer) {
                    return (Integer) left - (Integer) right;
                } else if (left instanceof Double || right instanceof Double) {
                    double leftVal = (left instanceof Double) ? (Double) left : ((Integer) left).doubleValue();
                    double rightVal = (right instanceof Double) ? (Double) right : ((Integer) right).doubleValue();
                    return leftVal - rightVal;
                }
            }
            return expression;
        }
        
        private Object evaluateMultiplication(String expression) {
            String[] parts = expression.split("\\*");
            if (parts.length == 2) {
                Object left = evaluateExpression(parts[0].trim());
                Object right = evaluateExpression(parts[1].trim());
                
                if (left instanceof Integer && right instanceof Integer) {
                    return (Integer) left * (Integer) right;
                } else if (left instanceof Double || right instanceof Double) {
                    double leftVal = (left instanceof Double) ? (Double) left : ((Integer) left).doubleValue();
                    double rightVal = (right instanceof Double) ? (Double) right : ((Integer) right).doubleValue();
                    return leftVal * rightVal;
                }
            }
            return expression;
        }
        
        private Object evaluateDivision(String expression) {
            String[] parts = expression.split("\\/");
            if (parts.length == 2) {
                Object left = evaluateExpression(parts[0].trim());
                Object right = evaluateExpression(parts[1].trim());
                
                if (left instanceof Integer && right instanceof Integer) {
                    int rightVal = (Integer) right;
                    if (rightVal != 0) {
                        return (Integer) left / rightVal;
                    }
                } else if (left instanceof Double || right instanceof Double) {
                    double leftVal = (left instanceof Double) ? (Double) left : ((Integer) left).doubleValue();
                    double rightVal = (right instanceof Double) ? (Double) right : ((Integer) right).doubleValue();
                    if (rightVal != 0.0) {
                        return leftVal / rightVal;
                    }
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
            
            if (expression.contains(".toLowerCase()")) {
                String varName = expression.substring(0, expression.indexOf("."));
                Object value = variables.get(varName);
                if (value instanceof String) {
                    return ((String) value).toLowerCase();
                }
            }
            
            if (expression.contains(".length()")) {
                String varName = expression.substring(0, expression.indexOf("."));
                Object value = variables.get(varName);
                if (value instanceof String) {
                    return ((String) value).length();
                } else if (value instanceof List) {
                    return ((List<?>) value).size();
                }
            }
            
            if (expression.contains(".size()")) {
                String varName = expression.substring(0, expression.indexOf("."));
                Object value = variables.get(varName);
                if (value instanceof List) {
                    return ((List<?>) value).size();
                } else if (value instanceof Map) {
                    return ((Map<?, ?>) value).size();
                }
            }
            
            return expression;
        }
    }
}
