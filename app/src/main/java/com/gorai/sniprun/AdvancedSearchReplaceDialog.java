package com.gorai.sniprun;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class AdvancedSearchReplaceDialog {
    
    public static class SearchResult {
        private final int startIndex;
        private final int endIndex;
        private final String matchText;
        private final int lineNumber;
        
        public SearchResult(int startIndex, int endIndex, String matchText, int lineNumber) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.matchText = matchText;
            this.lineNumber = lineNumber;
        }
        
        public int getStartIndex() { return startIndex; }
        public int getEndIndex() { return endIndex; }
        public String getMatchText() { return matchText; }
        public int getLineNumber() { return lineNumber; }
    }
    
    private final Context context;
    private final CodeEditor codeEditor;
    private AlertDialog dialog;
    
    private EditText searchEditText;
    private EditText replaceEditText;
    private EditText scopeEditText;
    
    private CheckBox caseSensitiveCheckBox;
    private CheckBox wholeWordCheckBox;
    private CheckBox regexCheckBox;
    private CheckBox wrapSearchCheckBox;
    private CheckBox searchInSelectionCheckBox;
    
    private TextView searchResultsText;
    private TextView searchStatisticsText;
    
    private Button findAllButton;
    private Button replaceAllButton;
    private Button findNextButton;
    private Button findPreviousButton;
    private Button replaceButton;
    
    private LinearLayout advancedOptionsLayout;
    private Button toggleAdvancedButton;
    
    private List<SearchResult> searchResults;
    private int currentResultIndex = -1;
    private boolean showingAdvanced = false;
    
    public AdvancedSearchReplaceDialog(Context context, CodeEditor codeEditor) {
        this.context = context;
        this.codeEditor = codeEditor;
        this.searchResults = new ArrayList<>();
    }
    
    public void show() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_advanced_search_replace, null);
        
        initializeViews(dialogView);
        setupListeners();
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Advanced Search & Replace")
               .setView(dialogView)
               .setNegativeButton("Close", null);
        
        dialog = builder.create();
        dialog.show();
        
        searchEditText.requestFocus();
        
        String selectedText = getSelectedText();
        if (!selectedText.isEmpty()) {
            searchEditText.setText(selectedText);
            performSearch();
        }
    }
    
    private void initializeViews(View view) {
        searchEditText = view.findViewById(R.id.edit_text_search);
        replaceEditText = view.findViewById(R.id.edit_text_replace);
        scopeEditText = view.findViewById(R.id.edit_text_scope);
        
        caseSensitiveCheckBox = view.findViewById(R.id.checkbox_case_sensitive);
        wholeWordCheckBox = view.findViewById(R.id.checkbox_whole_word);
        regexCheckBox = view.findViewById(R.id.checkbox_regex);
        wrapSearchCheckBox = view.findViewById(R.id.checkbox_wrap_search);
        searchInSelectionCheckBox = view.findViewById(R.id.checkbox_search_in_selection);
        
        searchResultsText = view.findViewById(R.id.text_search_results);
        searchStatisticsText = view.findViewById(R.id.text_search_statistics);
        
        findAllButton = view.findViewById(R.id.button_find_all);
        replaceAllButton = view.findViewById(R.id.button_replace_all);
        findNextButton = view.findViewById(R.id.button_find_next);
        findPreviousButton = view.findViewById(R.id.button_find_previous);
        replaceButton = view.findViewById(R.id.button_replace);
        
        advancedOptionsLayout = view.findViewById(R.id.layout_advanced_options);
        toggleAdvancedButton = view.findViewById(R.id.button_toggle_advanced);
        
        advancedOptionsLayout.setVisibility(View.GONE);
        wrapSearchCheckBox.setChecked(true);
    }
    
    private void setupListeners() {
        TextWatcher searchWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                performSearch();
            }
        };
        
        searchEditText.addTextChangedListener(searchWatcher);
        
        caseSensitiveCheckBox.setOnCheckedChangeListener((b, checked) -> performSearch());
        wholeWordCheckBox.setOnCheckedChangeListener((b, checked) -> performSearch());
        regexCheckBox.setOnCheckedChangeListener((b, checked) -> performSearch());
        searchInSelectionCheckBox.setOnCheckedChangeListener((b, checked) -> performSearch());
        
        findNextButton.setOnClickListener(v -> findNext());
        findPreviousButton.setOnClickListener(v -> findPrevious());
        findAllButton.setOnClickListener(v -> findAll());
        replaceButton.setOnClickListener(v -> replaceOne());
        replaceAllButton.setOnClickListener(v -> replaceAll());
        
        toggleAdvancedButton.setOnClickListener(v -> toggleAdvancedOptions());
    }
    
    private void performSearch() {
        String query = searchEditText.getText().toString();
        if (query.isEmpty()) {
            clearSearchResults();
            return;
        }
        
        searchResults.clear();
        currentResultIndex = -1;
        
        try {
            String text = getSearchText();
            Pattern pattern = createSearchPattern(query);
            Matcher matcher = pattern.matcher(text);
            
            int searchOffset = getSearchOffset();
            
            while (matcher.find()) {
                int startIndex = matcher.start() + searchOffset;
                int endIndex = matcher.end() + searchOffset;
                String matchText = matcher.group();
                int lineNumber = getLineNumber(startIndex);
                
                searchResults.add(new SearchResult(startIndex, endIndex, matchText, lineNumber));
            }
            
            updateSearchResultsDisplay();
            
        } catch (PatternSyntaxException e) {
            searchResultsText.setText("Invalid regex pattern: " + e.getMessage());
            searchStatisticsText.setText("");
        }
    }
    
    private String getSearchText() {
        String fullText = codeEditor.getText().toString();
        
        if (searchInSelectionCheckBox.isChecked()) {
            int selStart = codeEditor.getSelectionStart();
            int selEnd = codeEditor.getSelectionEnd();
            if (selEnd > selStart) {
                return fullText.substring(selStart, selEnd);
            }
        }
        
        String scope = scopeEditText.getText().toString().trim();
        if (!scope.isEmpty()) {
            return filterTextByScope(fullText, scope);
        }
        
        return fullText;
    }
    
    private int getSearchOffset() {
        if (searchInSelectionCheckBox.isChecked()) {
            return codeEditor.getSelectionStart();
        }
        return 0;
    }
    
    private String filterTextByScope(String text, String scope) {
        if (scope.equals("methods")) {
            return extractMethods(text);
        } else if (scope.equals("comments")) {
            return extractComments(text);
        } else if (scope.equals("strings")) {
            return extractStrings(text);
        }
        return text;
    }
    
    private String extractMethods(String text) {
        StringBuilder methods = new StringBuilder();
        Pattern methodPattern = Pattern.compile(
            "(public|private|protected|static|final)*\\s*\\w+\\s+\\w+\\s*\\([^)]*\\)\\s*\\{[^}]*\\}",
            Pattern.DOTALL
        );
        
        Matcher matcher = methodPattern.matcher(text);
        while (matcher.find()) {
            methods.append(matcher.group()).append("\n");
        }
        
        return methods.toString();
    }
    
    private String extractComments(String text) {
        StringBuilder comments = new StringBuilder();
        
        Pattern singleLinePattern = Pattern.compile("//.*$", Pattern.MULTILINE);
        Matcher matcher = singleLinePattern.matcher(text);
        while (matcher.find()) {
            comments.append(matcher.group()).append("\n");
        }
        
        Pattern multiLinePattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
        matcher = multiLinePattern.matcher(text);
        while (matcher.find()) {
            comments.append(matcher.group()).append("\n");
        }
        
        return comments.toString();
    }
    
    private String extractStrings(String text) {
        StringBuilder strings = new StringBuilder();
        Pattern stringPattern = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");
        Matcher matcher = stringPattern.matcher(text);
        
        while (matcher.find()) {
            strings.append(matcher.group()).append("\n");
        }
        
        return strings.toString();
    }
    
    private Pattern createSearchPattern(String query) throws PatternSyntaxException {
        int flags = 0;
        
        if (!caseSensitiveCheckBox.isChecked()) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        
        String patternString = query;
        
        if (!regexCheckBox.isChecked()) {
            patternString = Pattern.quote(query);
        }
        
        if (wholeWordCheckBox.isChecked() && !regexCheckBox.isChecked()) {
            patternString = "\\b" + patternString + "\\b";
        }
        
        return Pattern.compile(patternString, flags);
    }
    
    private int getLineNumber(int index) {
        String text = codeEditor.getText().toString();
        int line = 1;
        for (int i = 0; i < Math.min(index, text.length()); i++) {
            if (text.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }
    
    private void updateSearchResultsDisplay() {
        if (searchResults.isEmpty()) {
            searchResultsText.setText("No matches found");
            searchStatisticsText.setText("");
        } else {
            searchResultsText.setText(String.format("Found %d matches", searchResults.size()));
            
            if (currentResultIndex >= 0 && currentResultIndex < searchResults.size()) {
                searchStatisticsText.setText(String.format("Match %d of %d", 
                    currentResultIndex + 1, searchResults.size()));
            } else {
                searchStatisticsText.setText(String.format("%d matches found", searchResults.size()));
            }
        }
        
        updateButtonStates();
    }
    
    private void updateButtonStates() {
        boolean hasResults = !searchResults.isEmpty();
        findNextButton.setEnabled(hasResults);
        findPreviousButton.setEnabled(hasResults);
        replaceButton.setEnabled(hasResults && currentResultIndex >= 0);
        replaceAllButton.setEnabled(hasResults);
        findAllButton.setEnabled(hasResults);
    }
    
    private void findNext() {
        if (searchResults.isEmpty()) return;
        
        int currentCursor = codeEditor.getSelectionStart();
        
        for (int i = 0; i < searchResults.size(); i++) {
            SearchResult result = searchResults.get(i);
            if (result.getStartIndex() > currentCursor) {
                selectResult(i);
                return;
            }
        }
        
        if (wrapSearchCheckBox.isChecked() && !searchResults.isEmpty()) {
            selectResult(0);
        }
    }
    
    private void findPrevious() {
        if (searchResults.isEmpty()) return;
        
        int currentCursor = codeEditor.getSelectionStart();
        
        for (int i = searchResults.size() - 1; i >= 0; i--) {
            SearchResult result = searchResults.get(i);
            if (result.getStartIndex() < currentCursor) {
                selectResult(i);
                return;
            }
        }
        
        if (wrapSearchCheckBox.isChecked() && !searchResults.isEmpty()) {
            selectResult(searchResults.size() - 1);
        }
    }
    
    private void selectResult(int index) {
        if (index >= 0 && index < searchResults.size()) {
            SearchResult result = searchResults.get(index);
            codeEditor.setSelection(result.getStartIndex(), result.getEndIndex());
            currentResultIndex = index;
            updateSearchResultsDisplay();
        }
    }
    
    private void findAll() {
        if (searchResults.isEmpty()) return;
        
        StringBuilder allResults = new StringBuilder();
        allResults.append("Search Results:\n\n");
        
        for (int i = 0; i < searchResults.size(); i++) {
            SearchResult result = searchResults.get(i);
            allResults.append(String.format("Line %d: %s\n", 
                result.getLineNumber(), result.getMatchText()));
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("All Search Results")
               .setMessage(allResults.toString())
               .setPositiveButton("Close", null)
               .show();
    }
    
    private void replaceOne() {
        if (currentResultIndex < 0 || currentResultIndex >= searchResults.size()) {
            findNext();
            return;
        }
        
        SearchResult result = searchResults.get(currentResultIndex);
        String replaceText = replaceEditText.getText().toString();
        
        Editable editable = codeEditor.getText();
        if (editable != null) {
            editable.replace(result.getStartIndex(), result.getEndIndex(), replaceText);
            
            performSearch();
            
            if (currentResultIndex < searchResults.size()) {
                findNext();
            }
        }
    }
    
    private void replaceAll() {
        if (searchResults.isEmpty()) return;
        
        String replaceText = replaceEditText.getText().toString();
        int replacementCount = searchResults.size();
        
        Editable editable = codeEditor.getText();
        if (editable != null) {
            for (int i = searchResults.size() - 1; i >= 0; i--) {
                SearchResult result = searchResults.get(i);
                editable.replace(result.getStartIndex(), result.getEndIndex(), replaceText);
            }
            
            Toast.makeText(context, String.format("Replaced %d occurrences", replacementCount), 
                Toast.LENGTH_SHORT).show();
            
            performSearch();
        }
    }
    
    private void toggleAdvancedOptions() {
        showingAdvanced = !showingAdvanced;
        
        if (showingAdvanced) {
            advancedOptionsLayout.setVisibility(View.VISIBLE);
            toggleAdvancedButton.setText("Hide Advanced");
        } else {
            advancedOptionsLayout.setVisibility(View.GONE);
            toggleAdvancedButton.setText("Show Advanced");
        }
    }
    
    private String getSelectedText() {
        int selStart = codeEditor.getSelectionStart();
        int selEnd = codeEditor.getSelectionEnd();
        
        if (selEnd > selStart) {
            return codeEditor.getText().toString().substring(selStart, selEnd);
        }
        
        return "";
    }
    
    private void clearSearchResults() {
        searchResults.clear();
        currentResultIndex = -1;
        searchResultsText.setText("");
        searchStatisticsText.setText("");
        updateButtonStates();
    }
}
