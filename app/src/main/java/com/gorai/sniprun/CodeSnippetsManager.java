package com.gorai.sniprun;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeSnippetsManager {
    
    public static class CodeSnippet {
        private String name;
        private String trigger;
        private String code;
        private String description;
        private String category;
        
        public CodeSnippet() {}
        
        public CodeSnippet(String name, String trigger, String code, String description, String category) {
            this.name = name;
            this.trigger = trigger;
            this.code = code;
            this.description = description;
            this.category = category;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getTrigger() { return trigger; }
        public void setTrigger(String trigger) { this.trigger = trigger; }
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
    
    private static final String PREFS_NAME = "CodeSnippets";
    private static final String KEY_SNIPPETS = "snippets";
    
    private final SharedPreferences prefs;
    private final Gson gson;
    private List<CodeSnippet> snippets;
    
    public CodeSnippetsManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.snippets = new ArrayList<>();
        
        loadSnippets();
        initializeDefaultSnippets();
    }
    
    private void loadSnippets() {
        String json = prefs.getString(KEY_SNIPPETS, "");
        if (!json.isEmpty()) {
            Type type = new TypeToken<List<CodeSnippet>>(){}.getType();
            snippets = gson.fromJson(json, type);
            if (snippets == null) {
                snippets = new ArrayList<>();
            }
        }
    }
    
    private void saveSnippets() {
        String json = gson.toJson(snippets);
        prefs.edit().putString(KEY_SNIPPETS, json).apply();
    }
    
    private void initializeDefaultSnippets() {
        if (snippets.isEmpty()) {
            addDefaultSnippets();
            saveSnippets();
        }
    }
    
    private void addDefaultSnippets() {
        
        snippets.add(new CodeSnippet(
            "Main Method",
            "main",
            "public static void main(String[] args) {\n    ${cursor}\n}",
            "Main method template",
            "Methods"
        ));
        
        snippets.add(new CodeSnippet(
            "System.out.println",
            "sout",
            "System.out.println(${cursor});",
            "Print line to console",
            "Output"
        ));
        
        snippets.add(new CodeSnippet(
            "For Loop",
            "for",
            "for (int ${i} = 0; ${i} < ${length}; ${i}++) {\n    ${cursor}\n}",
            "For loop template",
            "Loops"
        ));
        
        snippets.add(new CodeSnippet(
            "Enhanced For Loop",
            "foreach",
            "for (${type} ${item} : ${collection}) {\n    ${cursor}\n}",
            "Enhanced for loop template",
            "Loops"
        ));
        
        snippets.add(new CodeSnippet(
            "While Loop",
            "while",
            "while (${condition}) {\n    ${cursor}\n}",
            "While loop template",
            "Loops"
        ));
        
        snippets.add(new CodeSnippet(
            "If Statement",
            "if",
            "if (${condition}) {\n    ${cursor}\n}",
            "If statement template",
            "Conditionals"
        ));
        
        snippets.add(new CodeSnippet(
            "If-Else Statement",
            "ifelse",
            "if (${condition}) {\n    ${cursor}\n} else {\n    \n}",
            "If-else statement template",
            "Conditionals"
        ));
        
        snippets.add(new CodeSnippet(
            "Try-Catch",
            "try",
            "try {\n    ${cursor}\n} catch (${Exception} e) {\n    e.printStackTrace();\n}",
            "Try-catch block template",
            "Exception Handling"
        ));
        
        snippets.add(new CodeSnippet(
            "Class Template",
            "class",
            "public class ${ClassName} {\n    \n    public ${ClassName}() {\n        ${cursor}\n    }\n    \n}",
            "Basic class template",
            "Classes"
        ));
        
        snippets.add(new CodeSnippet(
            "Method Template",
            "method",
            "public ${returnType} ${methodName}(${parameters}) {\n    ${cursor}\n}",
            "Method template",
            "Methods"
        ));
        
        snippets.add(new CodeSnippet(
            "Switch Statement",
            "switch",
            "switch (${variable}) {\n    case ${value1}:\n        ${cursor}\n        break;\n    case ${value2}:\n        \n        break;\n    default:\n        \n        break;\n}",
            "Switch statement template",
            "Conditionals"
        ));
        
        snippets.add(new CodeSnippet(
            "ArrayList Declaration",
            "arraylist",
            "List<${Type}> ${listName} = new ArrayList<>();",
            "ArrayList declaration",
            "Collections"
        ));
        
        snippets.add(new CodeSnippet(
            "HashMap Declaration",
            "hashmap",
            "Map<${KeyType}, ${ValueType}> ${mapName} = new HashMap<>();",
            "HashMap declaration",
            "Collections"
        ));
    }
    
    public void addSnippet(CodeSnippet snippet) {
        snippets.add(snippet);
        saveSnippets();
    }
    
    public void updateSnippet(int index, CodeSnippet snippet) {
        if (index >= 0 && index < snippets.size()) {
            snippets.set(index, snippet);
            saveSnippets();
        }
    }
    
    public void removeSnippet(int index) {
        if (index >= 0 && index < snippets.size()) {
            snippets.remove(index);
            saveSnippets();
        }
    }
    
    public List<CodeSnippet> getAllSnippets() {
        return new ArrayList<>(snippets);
    }
    
    public List<CodeSnippet> getSnippetsByCategory(String category) {
        List<CodeSnippet> filtered = new ArrayList<>();
        for (CodeSnippet snippet : snippets) {
            if (category.equals(snippet.getCategory())) {
                filtered.add(snippet);
            }
        }
        return filtered;
    }
    
    public CodeSnippet findSnippetByTrigger(String trigger) {
        for (CodeSnippet snippet : snippets) {
            if (trigger.equals(snippet.getTrigger())) {
                return snippet;
            }
        }
        return null;
    }
    
    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        for (CodeSnippet snippet : snippets) {
            if (!categories.contains(snippet.getCategory())) {
                categories.add(snippet.getCategory());
            }
        }
        return categories;
    }
    
    public String expandSnippet(String snippetCode, Map<String, String> variables) {
        String expanded = snippetCode;
        
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            expanded = expanded.replace(placeholder, entry.getValue());
        }
        
        expanded = expanded.replace("${cursor}", "");
        
        return expanded;
    }
    
    public String expandSnippet(String snippetCode) {
        return expandSnippet(snippetCode, new HashMap<>());
    }
    
    public int getCursorPosition(String snippetCode) {
        int cursorIndex = snippetCode.indexOf("${cursor}");
        if (cursorIndex != -1) {
            return cursorIndex;
        }
        return snippetCode.length();
    }
    
    public boolean hasSnippetWithTrigger(String trigger) {
        return findSnippetByTrigger(trigger) != null;
    }
    
    public void clearAllSnippets() {
        snippets.clear();
        saveSnippets();
    }
    
    public void resetToDefaults() {
        snippets.clear();
        addDefaultSnippets();
        saveSnippets();
    }
}
