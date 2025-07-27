package com.gorai.sniprun;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoCompleteManager {
    
    private static final String[] JAVA_KEYWORDS = {
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native", "new", "package",
        "private", "protected", "public", "return", "short", "static", "strictfp",
        "super", "switch", "synchronized", "this", "throw", "throws", "transient",
        "try", "void", "volatile", "while", "true", "false", "null"
    };
    
    private static final String[] JAVA_CLASSES = {
        "String", "Integer", "Double", "Float", "Boolean", "Character", "Byte",
        "Short", "Long", "Object", "Class", "List", "ArrayList", "HashMap", "Map",
        "Set", "HashSet", "LinkedList", "Vector", "Stack", "Queue", "Deque",
        "Collection", "Iterator", "Comparator", "Exception", "RuntimeException",
        "Thread", "Runnable", "StringBuilder", "StringBuffer", "Scanner", "File",
        "InputStream", "OutputStream", "BufferedReader", "FileReader", "PrintWriter",
        "System", "Math", "Arrays", "Collections", "Optional", "Stream"
    };
    
    private static final String[] COMMON_METHODS = {
        "toString()", "equals()", "hashCode()", "clone()", "compareTo()",
        "length()", "size()", "isEmpty()", "contains()", "add()", "remove()",
        "get()", "set()", "indexOf()", "substring()", "charAt()", "split()",
        "replace()", "toLowerCase()", "toUpperCase()", "trim()", "startsWith()",
        "endsWith()", "matches()", "replaceAll()", "valueOf()", "parseInt()",
        "parseDouble()", "format()", "append()", "insert()", "delete()",
        "println()", "print()", "printf()", "next()", "nextLine()", "hasNext()"
    };
    
    private final Context context;
    private final CodeEditor codeEditor;
    private ArrayAdapter<String> adapter;
    private List<String> suggestions;
    private Set<String> userDefinedVariables;
    private Set<String> userDefinedMethods;
    private Set<String> userDefinedClasses;
    
    public AutoCompleteManager(Context context, CodeEditor codeEditor) {
        this.context = context;
        this.codeEditor = codeEditor;
        this.suggestions = new ArrayList<>();
        this.userDefinedVariables = new HashSet<>();
        this.userDefinedMethods = new HashSet<>();
        this.userDefinedClasses = new HashSet<>();
        
        initializeSuggestions();
        setupAutoComplete();
    }
    
    private void initializeSuggestions() {
        suggestions.addAll(Arrays.asList(JAVA_KEYWORDS));
        suggestions.addAll(Arrays.asList(JAVA_CLASSES));
        suggestions.addAll(Arrays.asList(COMMON_METHODS));
    }
    
    private void setupAutoComplete() {
        codeEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateUserDefinedElements(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                triggerAutoComplete();
            }
        });
    }
    
    private void updateUserDefinedElements(String code) {
        extractVariables(code);
        extractMethods(code);
        extractClasses(code);
    }
    
    private void extractVariables(String code) {
        userDefinedVariables.clear();
        
        Pattern variablePattern = Pattern.compile(
            "(?:int|double|float|boolean|char|byte|short|long|String|var)\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher matcher = variablePattern.matcher(code);
        
        while (matcher.find()) {
            userDefinedVariables.add(matcher.group(1));
        }
    }
    
    private void extractMethods(String code) {
        userDefinedMethods.clear();
        
        Pattern methodPattern = Pattern.compile(
            "(?:public|private|protected|static)?\\s*(?:void|int|double|float|boolean|String|[A-Z][a-zA-Z0-9_]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
        Matcher matcher = methodPattern.matcher(code);
        
        while (matcher.find()) {
            String methodName = matcher.group(1);
            if (!methodName.equals("main")) {
                userDefinedMethods.add(methodName + "()");
            }
        }
    }
    
    private void extractClasses(String code) {
        userDefinedClasses.clear();
        
        Pattern classPattern = Pattern.compile("class\\s+([A-Z][a-zA-Z0-9_]*)");
        Matcher matcher = classPattern.matcher(code);
        
        while (matcher.find()) {
            userDefinedClasses.add(matcher.group(1));
        }
    }
    
    private void triggerAutoComplete() {
        String currentText = codeEditor.getText().toString();
        int cursorPosition = codeEditor.getSelectionStart();
        
        if (cursorPosition <= 0) return;
        
        String wordBeforeCursor = getWordBeforeCursor(currentText, cursorPosition);
        
        if (wordBeforeCursor.length() >= 2) {
            List<String> filteredSuggestions = getFilteredSuggestions(wordBeforeCursor);
            
            if (!filteredSuggestions.isEmpty()) {
                showSuggestions(filteredSuggestions, wordBeforeCursor);
            }
        }
    }
    
    private String getWordBeforeCursor(String text, int cursorPosition) {
        StringBuilder word = new StringBuilder();
        
        for (int i = cursorPosition - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') {
                word.insert(0, c);
            } else {
                break;
            }
        }
        
        return word.toString();
    }
    
    private List<String> getFilteredSuggestions(String prefix) {
        List<String> filtered = new ArrayList<>();
        String lowerPrefix = prefix.toLowerCase();
        
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(lowerPrefix)) {
                filtered.add(suggestion);
            }
        }
        
        for (String variable : userDefinedVariables) {
            if (variable.toLowerCase().startsWith(lowerPrefix)) {
                filtered.add(variable);
            }
        }
        
        for (String method : userDefinedMethods) {
            if (method.toLowerCase().startsWith(lowerPrefix)) {
                filtered.add(method);
            }
        }
        
        for (String className : userDefinedClasses) {
            if (className.toLowerCase().startsWith(lowerPrefix)) {
                filtered.add(className);
            }
        }
        
        return filtered;
    }
    
    private void showSuggestions(List<String> suggestions, String prefix) {
        
    }
    
    public void insertCompletion(String completion, String prefix) {
        int cursorPosition = codeEditor.getSelectionStart();
        int startPosition = cursorPosition - prefix.length();
        
        Editable editable = codeEditor.getText();
        if (editable != null) {
            editable.replace(startPosition, cursorPosition, completion);
        }
    }
    
    public List<String> getSuggestions(String prefix) {
        return getFilteredSuggestions(prefix);
    }
    
    public void addCustomSuggestion(String suggestion) {
        if (!suggestions.contains(suggestion)) {
            suggestions.add(suggestion);
        }
    }
    
    public void removeCustomSuggestion(String suggestion) {
        suggestions.remove(suggestion);
    }
}