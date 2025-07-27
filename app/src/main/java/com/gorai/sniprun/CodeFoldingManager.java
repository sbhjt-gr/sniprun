package com.gorai.sniprun;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.ReplacementSpan;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeFoldingManager {
    
    public static class FoldRegion {
        private final int startLine;
        private final int endLine;
        private final int startIndex;
        private final int endIndex;
        private final String foldType;
        private boolean folded;
        private String summary;
        
        public FoldRegion(int startLine, int endLine, int startIndex, int endIndex, String foldType) {
            this.startLine = startLine;
            this.endLine = endLine;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.foldType = foldType;
            this.folded = false;
            this.summary = "...";
        }
        
        public int getStartLine() { return startLine; }
        public int getEndLine() { return endLine; }
        public int getStartIndex() { return startIndex; }
        public int getEndIndex() { return endIndex; }
        public String getFoldType() { return foldType; }
        public boolean isFolded() { return folded; }
        public void setFolded(boolean folded) { this.folded = folded; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        
        public boolean contains(int line) {
            return line >= startLine && line <= endLine;
        }
        
        public boolean overlaps(FoldRegion other) {
            return !(endLine < other.startLine || startLine > other.endLine);
        }
    }
    
    private static class FoldSpan extends ReplacementSpan {
        private final String text;
        private final Paint paint;
        private final Paint backgroundPaint;
        private final int padding = 8;
        
        public FoldSpan(String text) {
            this.text = text;
            this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            this.paint.setColor(Color.parseColor("#FFFFFF"));
            this.paint.setTextSize(12f);
            
            this.backgroundPaint = new Paint();
            this.backgroundPaint.setColor(Color.parseColor("#555555"));
        }
        
        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            return (int) this.paint.measureText(this.text) + padding * 2;
        }
        
        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            float width = this.paint.measureText(this.text) + padding * 2;
            float height = bottom - top;
            
            Rect rect = new Rect((int)x, top + 2, (int)(x + width), bottom - 2);
            canvas.drawRoundRect(rect.left, rect.top, rect.right, rect.bottom, 4, 4, backgroundPaint);
            
            canvas.drawText(this.text, x + padding, y, this.paint);
        }
    }
    
    private final EditText codeEditor;
    private final List<FoldRegion> foldRegions;
    private boolean foldingEnabled = true;
    
    public CodeFoldingManager(EditText codeEditor) {
        this.codeEditor = codeEditor;
        this.foldRegions = new ArrayList<>();
    }
    
    public void analyzeFoldRegions(String code) {
        if (!foldingEnabled) {
            return;
        }
        
        foldRegions.clear();
        
        findMethodFolds(code);
        findClassFolds(code);
        findCommentFolds(code);
        findBraceFolds(code);
        findImportFolds(code);
    }
    
    private void findMethodFolds(String code) {
        Pattern methodPattern = Pattern.compile(
            "(public|private|protected|static|final|synchronized|abstract)*\\s*" +
            "([a-zA-Z_][a-zA-Z0-9_<>\\[\\]]*\\s+)*" +
            "([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\([^)]*\\)\\s*\\{",
            Pattern.MULTILINE
        );
        
        Matcher matcher = methodPattern.matcher(code);
        while (matcher.find()) {
            int startIndex = matcher.start();
            int braceIndex = code.indexOf('{', startIndex);
            
            if (braceIndex != -1) {
                int endIndex = findMatchingBrace(code, braceIndex);
                if (endIndex != -1) {
                    int startLine = getLineNumber(code, startIndex);
                    int endLine = getLineNumber(code, endIndex);
                    
                    if (endLine - startLine > 2) {
                        String methodName = matcher.group(3);
                        FoldRegion region = new FoldRegion(startLine, endLine, braceIndex + 1, endIndex, "method");
                        region.setSummary("..." + methodName + "()");
                        foldRegions.add(region);
                    }
                }
            }
        }
    }
    
    private void findClassFolds(String code) {
        Pattern classPattern = Pattern.compile(
            "(public|private|protected|static|final|abstract)*\\s*" +
            "(class|interface|enum)\\s+([a-zA-Z_][a-zA-Z0-9_]*).*?\\{",
            Pattern.MULTILINE | Pattern.DOTALL
        );
        
        Matcher matcher = classPattern.matcher(code);
        while (matcher.find()) {
            int startIndex = matcher.start();
            int braceIndex = code.indexOf('{', startIndex);
            
            if (braceIndex != -1) {
                int endIndex = findMatchingBrace(code, braceIndex);
                if (endIndex != -1) {
                    int startLine = getLineNumber(code, startIndex);
                    int endLine = getLineNumber(code, endIndex);
                    
                    if (endLine - startLine > 3) {
                        String className = matcher.group(3);
                        FoldRegion region = new FoldRegion(startLine, endLine, braceIndex + 1, endIndex, "class");
                        region.setSummary("..." + className);
                        foldRegions.add(region);
                    }
                }
            }
        }
    }
    
    private void findCommentFolds(String code) {
        Pattern multiLineCommentPattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
        Matcher matcher = multiLineCommentPattern.matcher(code);
        
        while (matcher.find()) {
            int startIndex = matcher.start();
            int endIndex = matcher.end();
            int startLine = getLineNumber(code, startIndex);
            int endLine = getLineNumber(code, endIndex);
            
            if (endLine - startLine > 2) {
                FoldRegion region = new FoldRegion(startLine, endLine, startIndex, endIndex, "comment");
                region.setSummary("/*...*/");
                foldRegions.add(region);
            }
        }
    }
    
    private void findBraceFolds(String code) {
        for (int i = 0; i < code.length(); i++) {
            if (code.charAt(i) == '{') {
                int endIndex = findMatchingBrace(code, i);
                if (endIndex != -1) {
                    int startLine = getLineNumber(code, i);
                    int endLine = getLineNumber(code, endIndex);
                    
                    if (endLine - startLine > 3) {
                        final int currentIndex = i;
                        final int currentEndIndex = endIndex;
                        boolean alreadyExists = foldRegions.stream()
                            .anyMatch(region -> region.getStartIndex() <= currentIndex && region.getEndIndex() >= currentEndIndex);
                        
                        if (!alreadyExists) {
                            FoldRegion region = new FoldRegion(startLine, endLine, i + 1, endIndex, "block");
                            region.setSummary("{...}");
                            foldRegions.add(region);
                        }
                    }
                }
            }
        }
    }
    
    private void findImportFolds(String code) {
        Pattern importPattern = Pattern.compile("import\\s+[^;]+;", Pattern.MULTILINE);
        Matcher matcher = importPattern.matcher(code);
        
        int firstImportStart = -1;
        int lastImportEnd = -1;
        int importCount = 0;
        
        while (matcher.find()) {
            if (firstImportStart == -1) {
                firstImportStart = matcher.start();
            }
            lastImportEnd = matcher.end();
            importCount++;
        }
        
        if (importCount > 3 && firstImportStart != -1) {
            int startLine = getLineNumber(code, firstImportStart);
            int endLine = getLineNumber(code, lastImportEnd);
            
            FoldRegion region = new FoldRegion(startLine, endLine, firstImportStart, lastImportEnd, "imports");
            region.setSummary("..." + importCount + " imports");
            foldRegions.add(region);
        }
    }
    
    private int findMatchingBrace(String code, int openBraceIndex) {
        int braceCount = 1;
        boolean inString = false;
        boolean inChar = false;
        boolean inComment = false;
        
        for (int i = openBraceIndex + 1; i < code.length(); i++) {
            char c = code.charAt(i);
            char prev = i > 0 ? code.charAt(i - 1) : '\0';
            
            if (!inString && !inChar && !inComment) {
                if (c == '"' && prev != '\\') {
                    inString = true;
                } else if (c == '\'' && prev != '\\') {
                    inChar = true;
                } else if (c == '/' && i + 1 < code.length()) {
                    if (code.charAt(i + 1) == '*') {
                        inComment = true;
                        i++;
                    } else if (code.charAt(i + 1) == '/') {
                        while (i < code.length() && code.charAt(i) != '\n') {
                            i++;
                        }
                    }
                } else if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        return i;
                    }
                }
            } else if (inString && c == '"' && prev != '\\') {
                inString = false;
            } else if (inChar && c == '\'' && prev != '\\') {
                inChar = false;
            } else if (inComment && c == '*' && i + 1 < code.length() && code.charAt(i + 1) == '/') {
                inComment = false;
                i++;
            }
        }
        
        return -1;
    }
    
    private int getLineNumber(String code, int index) {
        int line = 1;
        for (int i = 0; i < Math.min(index, code.length()); i++) {
            if (code.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }
    
    public void applyFolding() {
        if (!foldingEnabled) {
            return;
        }
        
        Editable editable = codeEditor.getText();
        if (editable == null) return;
        
        clearFoldSpans();
        
        for (FoldRegion region : foldRegions) {
            if (region.isFolded()) {
                int start = Math.max(0, Math.min(region.getStartIndex(), editable.length()));
                int end = Math.max(start, Math.min(region.getEndIndex(), editable.length()));
                
                FoldSpan span = new FoldSpan(region.getSummary());
                editable.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
    
    private void clearFoldSpans() {
        Editable editable = codeEditor.getText();
        if (editable == null) return;
        
        FoldSpan[] spans = editable.getSpans(0, editable.length(), FoldSpan.class);
        for (FoldSpan span : spans) {
            editable.removeSpan(span);
        }
    }
    
    public void toggleFold(int line) {
        for (FoldRegion region : foldRegions) {
            if (region.contains(line)) {
                region.setFolded(!region.isFolded());
                applyFolding();
                break;
            }
        }
    }
    
    public void foldAll() {
        for (FoldRegion region : foldRegions) {
            region.setFolded(true);
        }
        applyFolding();
    }
    
    public void unfoldAll() {
        for (FoldRegion region : foldRegions) {
            region.setFolded(false);
        }
        applyFolding();
    }
    
    public List<FoldRegion> getFoldRegions() {
        return new ArrayList<>(foldRegions);
    }
    
    public List<FoldRegion> getFoldableRegionsForLine(int line) {
        List<FoldRegion> regions = new ArrayList<>();
        for (FoldRegion region : foldRegions) {
            if (region.getStartLine() == line) {
                regions.add(region);
            }
        }
        return regions;
    }
    
    public void setFoldingEnabled(boolean enabled) {
        this.foldingEnabled = enabled;
        if (!enabled) {
            unfoldAll();
        }
    }
    
    public boolean isFoldingEnabled() {
        return foldingEnabled;
    }
    
    public int getFoldedRegionCount() {
        int count = 0;
        for (FoldRegion region : foldRegions) {
            if (region.isFolded()) {
                count++;
            }
        }
        return count;
    }
}
