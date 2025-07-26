package com.gorai.sniprun;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class FindReplaceDialog {
    
    private final Context context;
    private final CodeEditor codeEditor;
    private AlertDialog dialog;
    
    private EditText findEditText;
    private EditText replaceEditText;
    private CheckBox caseSensitiveCheckBox;
    private CheckBox regexCheckBox;
    private CheckBox wholeWordCheckBox;
    private TextView resultCountText;
    
    private String lastSearchQuery = "";
    private int currentMatchIndex = -1;
    private int totalMatches = 0;
    
    public FindReplaceDialog(Context context, CodeEditor codeEditor) {
        this.context = context;
        this.codeEditor = codeEditor;
    }
    
    public void show() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_find_replace, null);
        
        initializeViews(dialogView);
        setupListeners();
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Find & Replace")
               .setView(dialogView)
               .setPositiveButton("Close", null)
               .setNeutralButton("Find Next", (d, w) -> findNext())
               .setNegativeButton("Replace All", (d, w) -> replaceAll());
        
        dialog = builder.create();
        dialog.show();
        
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> findNext());
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> replaceAll());
        
        findEditText.requestFocus();
    }
    
    private void initializeViews(View view) {
        findEditText = view.findViewById(R.id.edit_text_find);
        replaceEditText = view.findViewById(R.id.edit_text_replace);
        caseSensitiveCheckBox = view.findViewById(R.id.checkbox_case_sensitive);
        regexCheckBox = view.findViewById(R.id.checkbox_regex);
        wholeWordCheckBox = view.findViewById(R.id.checkbox_whole_word);
        resultCountText = view.findViewById(R.id.text_result_count);
        
        view.findViewById(R.id.button_find_previous).setOnClickListener(v -> findPrevious());
        view.findViewById(R.id.button_find_next).setOnClickListener(v -> findNext());
        view.findViewById(R.id.button_replace).setOnClickListener(v -> replaceOne());
        view.findViewById(R.id.button_replace_all).setOnClickListener(v -> replaceAll());
    }
    
    private void setupListeners() {
        TextWatcher searchWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                updateSearch();
            }
        };
        
        findEditText.addTextChangedListener(searchWatcher);
        
        caseSensitiveCheckBox.setOnCheckedChangeListener((b, checked) -> updateSearch());
        regexCheckBox.setOnCheckedChangeListener((b, checked) -> updateSearch());
        wholeWordCheckBox.setOnCheckedChangeListener((b, checked) -> updateSearch());
    }
    
    private void updateSearch() {
        String query = findEditText.getText().toString();
        if (query.isEmpty()) {
            resultCountText.setText("");
            currentMatchIndex = -1;
            totalMatches = 0;
            return;
        }
        
        if (!query.equals(lastSearchQuery)) {
            lastSearchQuery = query;
            currentMatchIndex = -1;
            totalMatches = countMatches(query);
            
            if (totalMatches > 0) {
                resultCountText.setText("0 of " + totalMatches);
            } else {
                resultCountText.setText("No matches");
            }
        }
    }
    
    private int countMatches(String query) {
        String text = codeEditor.getText().toString();
        if (text.isEmpty() || query.isEmpty()) {
            return 0;
        }
        
        try {
            Pattern pattern = createPattern(query);
            Matcher matcher = pattern.matcher(text);
            
            int count = 0;
            while (matcher.find()) {
                count++;
            }
            return count;
        } catch (PatternSyntaxException e) {
            return 0;
        }
    }
    
    private Pattern createPattern(String query) throws PatternSyntaxException {
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
    
    private void findNext() {
        String query = findEditText.getText().toString();
        if (query.isEmpty()) {
            return;
        }
        
        try {
            String text = codeEditor.getText().toString();
            Pattern pattern = createPattern(query);
            Matcher matcher = pattern.matcher(text);
            
            int startPos = Math.max(0, codeEditor.getSelectionEnd());
            
            boolean found = false;
            int matchIndex = 0;
            
            while (matcher.find()) {
                if (matcher.start() >= startPos) {
                    codeEditor.setSelection(matcher.start(), matcher.end());
                    currentMatchIndex = matchIndex;
                    found = true;
                    break;
                }
                matchIndex++;
            }
            
            if (!found && totalMatches > 0) {
                matcher.reset();
                if (matcher.find()) {
                    codeEditor.setSelection(matcher.start(), matcher.end());
                    currentMatchIndex = 0;
                    found = true;
                }
            }
            
            if (found) {
                resultCountText.setText((currentMatchIndex + 1) + " of " + totalMatches);
            }
            
        } catch (PatternSyntaxException e) {
            Toast.makeText(context, "Invalid regex pattern", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void findPrevious() {
        String query = findEditText.getText().toString();
        if (query.isEmpty()) {
            return;
        }
        
        try {
            String text = codeEditor.getText().toString();
            Pattern pattern = createPattern(query);
            Matcher matcher = pattern.matcher(text);
            
            int startPos = codeEditor.getSelectionStart();
            
            int lastMatchStart = -1;
            int matchIndex = -1;
            int currentIndex = 0;
            
            while (matcher.find()) {
                if (matcher.start() < startPos) {
                    lastMatchStart = matcher.start();
                    matchIndex = currentIndex;
                } else {
                    break;
                }
                currentIndex++;
            }
            
            if (lastMatchStart == -1 && totalMatches > 0) {
                matcher.reset();
                while (matcher.find()) {
                    lastMatchStart = matcher.start();
                    matchIndex = currentIndex;
                    currentIndex++;
                }
            }
            
            if (lastMatchStart != -1) {
                matcher.reset();
                while (matcher.find()) {
                    if (matcher.start() == lastMatchStart) {
                        codeEditor.setSelection(matcher.start(), matcher.end());
                        currentMatchIndex = matchIndex;
                        resultCountText.setText((currentMatchIndex + 1) + " of " + totalMatches);
                        break;
                    }
                }
            }
            
        } catch (PatternSyntaxException e) {
            Toast.makeText(context, "Invalid regex pattern", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void replaceOne() {
        String findQuery = findEditText.getText().toString();
        String replaceText = replaceEditText.getText().toString();
        
        if (findQuery.isEmpty()) {
            return;
        }
        
        int selectionStart = codeEditor.getSelectionStart();
        int selectionEnd = codeEditor.getSelectionEnd();
        
        if (selectionStart != selectionEnd) {
            String selectedText = codeEditor.getText().toString().substring(selectionStart, selectionEnd);
            
            try {
                Pattern pattern = createPattern(findQuery);
                if (pattern.matcher(selectedText).matches()) {
                    codeEditor.getText().replace(selectionStart, selectionEnd, replaceText);
                    updateSearch();
                    findNext();
                }
            } catch (PatternSyntaxException e) {
                Toast.makeText(context, "Invalid regex pattern", Toast.LENGTH_SHORT).show();
            }
        } else {
            findNext();
        }
    }
    
    private void replaceAll() {
        String findQuery = findEditText.getText().toString();
        String replaceText = replaceEditText.getText().toString();
        
        if (findQuery.isEmpty()) {
            return;
        }
        
        try {
            String text = codeEditor.getText().toString();
            Pattern pattern = createPattern(findQuery);
            
            String newText;
            if (regexCheckBox.isChecked()) {
                newText = pattern.matcher(text).replaceAll(replaceText);
            } else {
                newText = text.replace(findQuery, replaceText);
            }
            
            int replacements = totalMatches;
            codeEditor.setText(newText);
            
            Toast.makeText(context, replacements + " replacements made", Toast.LENGTH_SHORT).show();
            updateSearch();
            
        } catch (PatternSyntaxException e) {
            Toast.makeText(context, "Invalid regex pattern", Toast.LENGTH_SHORT).show();
        }
    }
}
