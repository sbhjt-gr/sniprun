package com.gorai.sniprun;

import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.UnderlineSpan;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorHighlightManager {
    
    public static class ErrorInfo {
        private final int startIndex;
        private final int endIndex;
        private final String message;
        private final ErrorType type;
        
        public ErrorInfo(int startIndex, int endIndex, String message, ErrorType type) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.message = message;
            this.type = type;
        }
        
        public int getStartIndex() { return startIndex; }
        public int getEndIndex() { return endIndex; }
        public String getMessage() { return message; }
        public ErrorType getType() { return type; }
    }
    
    public enum ErrorType {
        SYNTAX_ERROR(Color.parseColor("#FF5555")),
        WARNING(Color.parseColor("#FFAA00")),
        INFO(Color.parseColor("#5555FF"));
        
        private final int color;
        
        ErrorType(int color) {
            this.color = color;
        }
        
        public int getColor() {
            return color;
        }
    }
    
    private final EditText codeEditor;
    private final List<ErrorInfo> errors;
    private boolean highlightingEnabled = true;
    
    public ErrorHighlightManager(EditText codeEditor) {
        this.codeEditor = codeEditor;
        this.errors = new ArrayList<>();
    }
    
    public void highlightErrors(String code) {
        if (!highlightingEnabled) {
            return;
        }
        
        clearErrorHighlights();
        errors.clear();
        
        findSyntaxErrors(code);
        applyErrorHighlights();
    }
    
    private void findSyntaxErrors(String code) {
        
        findUnmatchedBraces(code);
        findUnmatchedParentheses(code);
        findUnmatchedQuotes(code);
        findMissingSemicolons(code);
        findInvalidVariableNames(code);
        findUnusedImports(code);
    }
    
    private void findUnmatchedBraces(String code) {
        int braceCount = 0;
        int lastOpenBrace = -1;
        
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            
            if (c == '{') {
                braceCount++;
                lastOpenBrace = i;
            } else if (c == '}') {
                braceCount--;
                if (braceCount < 0) {
                    errors.add(new ErrorInfo(i, i + 1, "Unmatched closing brace", ErrorType.SYNTAX_ERROR));
                    braceCount = 0;
                }
            }
        }
        
        if (braceCount > 0 && lastOpenBrace != -1) {
            errors.add(new ErrorInfo(lastOpenBrace, lastOpenBrace + 1, "Unmatched opening brace", ErrorType.SYNTAX_ERROR));
        }
    }
    
    private void findUnmatchedParentheses(String code) {
        int parenCount = 0;
        int lastOpenParen = -1;
        
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            
            if (c == '(') {
                parenCount++;
                lastOpenParen = i;
            } else if (c == ')') {
                parenCount--;
                if (parenCount < 0) {
                    errors.add(new ErrorInfo(i, i + 1, "Unmatched closing parenthesis", ErrorType.SYNTAX_ERROR));
                    parenCount = 0;
                }
            }
        }
        
        if (parenCount > 0 && lastOpenParen != -1) {
            errors.add(new ErrorInfo(lastOpenParen, lastOpenParen + 1, "Unmatched opening parenthesis", ErrorType.SYNTAX_ERROR));
        }
    }
    
    private void findUnmatchedQuotes(String code) {
        boolean inString = false;
        boolean inChar = false;
        int stringStart = -1;
        int charStart = -1;
        
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            
            if (c == '"' && !inChar && (i == 0 || code.charAt(i - 1) != '\\')) {
                if (!inString) {
                    inString = true;
                    stringStart = i;
                } else {
                    inString = false;
                }
            } else if (c == '\'' && !inString && (i == 0 || code.charAt(i - 1) != '\\')) {
                if (!inChar) {
                    inChar = true;
                    charStart = i;
                } else {
                    inChar = false;
                }
            } else if (c == '\n') {
                if (inString && stringStart != -1) {
                    errors.add(new ErrorInfo(stringStart, i, "Unterminated string literal", ErrorType.SYNTAX_ERROR));
                    inString = false;
                }
                if (inChar && charStart != -1) {
                    errors.add(new ErrorInfo(charStart, i, "Unterminated character literal", ErrorType.SYNTAX_ERROR));
                    inChar = false;
                }
            }
        }
        
        if (inString && stringStart != -1) {
            errors.add(new ErrorInfo(stringStart, code.length(), "Unterminated string literal", ErrorType.SYNTAX_ERROR));
        }
        if (inChar && charStart != -1) {
            errors.add(new ErrorInfo(charStart, code.length(), "Unterminated character literal", ErrorType.SYNTAX_ERROR));
        }
    }
    
    private void findMissingSemicolons(String code) {
        String[] lines = code.split("\n");
        int currentIndex = 0;
        
        for (String line : lines) {
            String trimmed = line.trim();
            
            if (!trimmed.isEmpty() && 
                !trimmed.endsWith(";") && 
                !trimmed.endsWith("{") && 
                !trimmed.endsWith("}") && 
                !trimmed.startsWith("//") && 
                !trimmed.startsWith("/*") && 
                !trimmed.startsWith("*") && 
                !trimmed.startsWith("@") &&
                !trimmed.startsWith("import") &&
                !trimmed.startsWith("package") &&
                !isControlStatement(trimmed)) {
                
                int lineEnd = currentIndex + line.length();
                errors.add(new ErrorInfo(lineEnd - 1, lineEnd, "Missing semicolon", ErrorType.WARNING));
            }
            
            currentIndex += line.length() + 1;
        }
    }
    
    private boolean isControlStatement(String line) {
        return line.startsWith("if") || 
               line.startsWith("else") || 
               line.startsWith("for") || 
               line.startsWith("while") || 
               line.startsWith("do") || 
               line.startsWith("switch") || 
               line.startsWith("case") || 
               line.startsWith("default") || 
               line.startsWith("try") || 
               line.startsWith("catch") || 
               line.startsWith("finally") ||
               line.startsWith("class") ||
               line.startsWith("interface") ||
               line.startsWith("enum");
    }
    
    private void findInvalidVariableNames(String code) {
        Pattern variablePattern = Pattern.compile("\\b(?:int|double|float|boolean|char|byte|short|long|String)\\s+([a-zA-Z_$][a-zA-Z0-9_$]*)");
        Matcher matcher = variablePattern.matcher(code);
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            
            if (Character.isDigit(varName.charAt(0))) {
                int start = matcher.start(1);
                int end = matcher.end(1);
                errors.add(new ErrorInfo(start, end, "Variable name cannot start with a digit", ErrorType.SYNTAX_ERROR));
            }
            
            if (isJavaKeyword(varName)) {
                int start = matcher.start(1);
                int end = matcher.end(1);
                errors.add(new ErrorInfo(start, end, "Variable name cannot be a Java keyword", ErrorType.SYNTAX_ERROR));
            }
        }
    }
    
    private void findUnusedImports(String code) {
        Pattern importPattern = Pattern.compile("import\\s+([a-zA-Z_][a-zA-Z0-9_.]*);");
        Matcher matcher = importPattern.matcher(code);
        
        while (matcher.find()) {
            String importName = matcher.group(1);
            String className = importName.substring(importName.lastIndexOf('.') + 1);
            
            if (!code.contains(className) || code.indexOf(className) == matcher.start()) {
                errors.add(new ErrorInfo(matcher.start(), matcher.end(), "Unused import: " + importName, ErrorType.WARNING));
            }
        }
    }
    
    private boolean isJavaKeyword(String word) {
        String[] keywords = {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native", "new", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "try", "void", "volatile", "while", "true", "false", "null"
        };
        
        for (String keyword : keywords) {
            if (keyword.equals(word)) {
                return true;
            }
        }
        return false;
    }
    
    private void applyErrorHighlights() {
        Editable editable = codeEditor.getText();
        if (editable == null) return;
        
        for (ErrorInfo error : errors) {
            int start = Math.max(0, Math.min(error.getStartIndex(), editable.length()));
            int end = Math.max(start, Math.min(error.getEndIndex(), editable.length()));
            
            BackgroundColorSpan backgroundSpan = new BackgroundColorSpan(
                Color.argb(50, Color.red(error.getType().getColor()), 
                          Color.green(error.getType().getColor()), 
                          Color.blue(error.getType().getColor()))
            );
            
            UnderlineSpan underlineSpan = new UnderlineSpan();
            
            editable.setSpan(backgroundSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            editable.setSpan(underlineSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    private void clearErrorHighlights() {
        Editable editable = codeEditor.getText();
        if (editable == null) return;
        
        BackgroundColorSpan[] backgroundSpans = editable.getSpans(0, editable.length(), BackgroundColorSpan.class);
        for (BackgroundColorSpan span : backgroundSpans) {
            editable.removeSpan(span);
        }
        
        UnderlineSpan[] underlineSpans = editable.getSpans(0, editable.length(), UnderlineSpan.class);
        for (UnderlineSpan span : underlineSpans) {
            editable.removeSpan(span);
        }
    }
    
    public List<ErrorInfo> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public void setHighlightingEnabled(boolean enabled) {
        this.highlightingEnabled = enabled;
        if (!enabled) {
            clearErrorHighlights();
        }
    }
    
    public boolean isHighlightingEnabled() {
        return highlightingEnabled;
    }
    
    public int getErrorCount() {
        return errors.size();
    }
    
    public int getErrorCount(ErrorType type) {
        int count = 0;
        for (ErrorInfo error : errors) {
            if (error.getType() == type) {
                count++;
            }
        }
        return count;
    }
}
