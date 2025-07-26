package com.gorai.sniprun;

import java.util.regex.Pattern;
import java.util.Stack;

public class JavaCodeFormatter {
    
    private static final String INDENT = "    ";
    
    public static String format(String code) {
        if (code == null || code.trim().isEmpty()) {
            return code;
        }
        
        StringBuilder formatted = new StringBuilder();
        String[] lines = code.split("\n");
        int indentLevel = 0;
        boolean inMultiLineComment = false;
        boolean inString = false;
        Stack<Character> braceStack = new Stack<>();
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            if (trimmedLine.isEmpty()) {
                formatted.append("\n");
                continue;
            }
            
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
            
            if (trimmedLine.startsWith("//")) {
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
            
            if (trimmedLine.startsWith("case ") || trimmedLine.startsWith("default:")) {
            }
            
            formatted.append("\n");
        }
        
        return formatted.toString().trim();
    }
    
    private static String getIndent(int level) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indent.append(INDENT);
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
    
    public static String formatJavaCode(String code) {
        return format(code);
    }
    
    public static String addMinimalFormatting(String code) {
        if (code == null || code.trim().isEmpty()) {
            return code;
        }
        
        return code
            .replaceAll("\\{", " {\n")
            .replaceAll("\\}", "\n}\n")
            .replaceAll(";", ";\n")
            .replaceAll("\\n\\s*\\n", "\n")
            .trim();
    }
}
