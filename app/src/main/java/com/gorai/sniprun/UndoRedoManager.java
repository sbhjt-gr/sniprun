package com.gorai.sniprun;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.Stack;

public class UndoRedoManager {
    
    private static class TextChange {
        final String text;
        final int start;
        final int end;
        final int before;
        final int count;
        final long timestamp;
        
        TextChange(String text, int start, int end, int before, int count) {
            this.text = text;
            this.start = start;
            this.end = end;
            this.before = before;
            this.count = count;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    private final EditText codeEditor;
    private final Stack<TextChange> undoStack;
    private final Stack<TextChange> redoStack;
    private final int maxHistorySize;
    
    private boolean isApplyingChange = false;
    private String lastText = "";
    private long lastChangeTime = 0;
    private static final long MERGE_INTERVAL = 1000;
    
    public UndoRedoManager(EditText codeEditor) {
        this(codeEditor, 50);
    }
    
    public UndoRedoManager(EditText codeEditor, int maxHistorySize) {
        this.codeEditor = codeEditor;
        this.maxHistorySize = maxHistorySize;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        
        setupTextWatcher();
    }
    
    private void setupTextWatcher() {
        codeEditor.addTextChangedListener(new TextWatcher() {
            private String beforeText;
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!isApplyingChange) {
                    beforeText = s.toString();
                }
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isApplyingChange && beforeText != null) {
                    String currentText = s.toString();
                    long currentTime = System.currentTimeMillis();
                    
                    boolean shouldMerge = shouldMergeWithLastChange(currentTime, start, before, count);
                    
                    if (!shouldMerge) {
                        TextChange change = new TextChange(beforeText, start, start + before, before, count);
                        addToUndoStack(change);
                        redoStack.clear();
                    } else if (!undoStack.isEmpty()) {
                        TextChange lastChange = undoStack.peek();
                        TextChange mergedChange = new TextChange(
                            lastChange.text, 
                            Math.min(lastChange.start, start), 
                            Math.max(lastChange.end, start + before),
                            lastChange.before, 
                            count
                        );
                        undoStack.pop();
                        undoStack.push(mergedChange);
                    }
                    
                    lastText = currentText;
                    lastChangeTime = currentTime;
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private boolean shouldMergeWithLastChange(long currentTime, int start, int before, int count) {
        if (undoStack.isEmpty()) {
            return false;
        }
        
        long timeDiff = currentTime - lastChangeTime;
        if (timeDiff > MERGE_INTERVAL) {
            return false;
        }
        
        TextChange lastChange = undoStack.peek();
        
        boolean isSimpleTyping = (before == 0 && count == 1) || (before == 1 && count == 0);
        boolean isConsecutive = Math.abs(start - lastChange.end) <= 1;
        
        return isSimpleTyping && isConsecutive;
    }
    
    private void addToUndoStack(TextChange change) {
        undoStack.push(change);
        
        while (undoStack.size() > maxHistorySize) {
            undoStack.removeElementAt(0);
        }
    }
    
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    public void undo() {
        if (!canUndo()) {
            return;
        }
        
        isApplyingChange = true;
        
        try {
            TextChange change = undoStack.pop();
            
            String currentText = codeEditor.getText().toString();
            TextChange redoChange = new TextChange(
                currentText, 
                change.start, 
                change.start + change.count, 
                change.count, 
                change.before
            );
            redoStack.push(redoChange);
            
            codeEditor.setText(change.text);
            codeEditor.setSelection(Math.min(change.start, change.text.length()));
            
        } finally {
            isApplyingChange = false;
        }
    }
    
    public void redo() {
        if (!canRedo()) {
            return;
        }
        
        isApplyingChange = true;
        
        try {
            TextChange change = redoStack.pop();
            
            String currentText = codeEditor.getText().toString();
            TextChange undoChange = new TextChange(
                currentText, 
                change.start, 
                change.start + change.before, 
                change.before, 
                change.count
            );
            undoStack.push(undoChange);
            
            codeEditor.setText(change.text);
            codeEditor.setSelection(Math.min(change.start + change.count, change.text.length()));
            
        } finally {
            isApplyingChange = false;
        }
    }
    
    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
    }
    
    public int getUndoStackSize() {
        return undoStack.size();
    }
    
    public int getRedoStackSize() {
        return redoStack.size();
    }
    
    public void saveCheckpoint() {
        if (!isApplyingChange) {
            String currentText = codeEditor.getText().toString();
            if (!currentText.equals(lastText)) {
                TextChange checkpoint = new TextChange(lastText, 0, lastText.length(), lastText.length(), currentText.length());
                addToUndoStack(checkpoint);
                redoStack.clear();
                lastText = currentText;
            }
        }
    }
}
