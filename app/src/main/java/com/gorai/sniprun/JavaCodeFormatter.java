package com.gorai.sniprun;

import java.util.regex.Pattern;

public class JavaCodeFormatter {
    
    public static String format(String code) {
        if (code == null || code.trim().isEmpty()) {
            return code;
        }
        
        StringBuilder formatted = new StringBuilder();
        String[] lines = code.split("\n");
        int indentLevel = 0;
        boolean inMultiLineComment = false;
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            if (trimmedLine.startsWith("/*")) {
                inMultiLineComment = true;
            }
            
            if (inMultiLineComment) {
                formatted.append(getIndent(indentLevel)).append(trimmedLine).append("\n");
                if (trimmedLine.endsWith("*/")) {
                    inMultiLineComment = false;
                }
                continue;
            }
            
            if (trimmedLine.startsWith("//") || trimmedLine.isEmpty()) {
                formatted.append(getIndent(indentLevel)).append(trimmedLine).append("\n");
                continue;
            }
            
            if (trimmedLine.startsWith("}")) {
                indentLevel = Math.max(0, indentLevel - 1);
            }
            
            formatted.append(getIndent(indentLevel)).append(trimmedLine);
            
            if (trimmedLine.endsWith("{")) {
                indentLevel++;
            }
            
            if (!trimmedLine.endsWith(";") && !trimmedLine.endsWith("{") && 
                !trimmedLine.endsWith("}") && !trimmedLine.isEmpty()) {
                
                if (isControlStructure(trimmedLine) || isMethodDeclaration(trimmedLine) || 
                    isClassDeclaration(trimmedLine)) {
                    
                } else {
                    
                }
            }
            
            formatted.append("\n");
        }
        
        return formatted.toString();
    }
    
    private static String getIndent(int level) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indent.append("    ");
        }
        return indent.toString();
    }
    
    private static boolean isControlStructure(String line) {
        String trimmed = line.trim();
        return trimmed.startsWith("if") || trimmed.startsWith("else") || 
               trimmed.startsWith("for") || trimmed.startsWith("while") ||
               trimmed.startsWith("do") || trimmed.startsWith("switch") ||
               trimmed.startsWith("try") || trimmed.startsWith("catch") ||
               trimmed.startsWith("finally");
    }
    
    private static boolean isMethodDeclaration(String line) {
        return Pattern.matches(".*\\s+\\w+\\s*\\([^)]*\\).*", line.trim()) &&
               (line.contains("public") || line.contains("private") || 
                line.contains("protected") || line.contains("static"));
    }
    
    private static boolean isClassDeclaration(String line) {
        String trimmed = line.trim();
        return trimmed.startsWith("class ") || trimmed.startsWith("interface ") ||
               trimmed.startsWith("enum ") || trimmed.contains("class ") ||
               trimmed.contains("interface ") || trimmed.contains("enum ");
    }
}
