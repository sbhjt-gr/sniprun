package com.gorai.sniprun;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatEditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeEditor extends AppCompatEditText {
    
    private boolean isHighlighting = false;
    private static final int COLOR_KEYWORD = Color.parseColor("#569CD6");
    private static final int COLOR_STRING = Color.parseColor("#CE9178");
    private static final int COLOR_COMMENT = Color.parseColor("#6A9955");
    private static final int COLOR_NUMBER = Color.parseColor("#B5CEA8");
    private static final int COLOR_TYPE = Color.parseColor("#4EC9B0");
    private static final int COLOR_ANNOTATION = Color.parseColor("#DCDCAA");
    
    private static final String[] JAVA_KEYWORDS = {
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native", "new", "package",
        "private", "protected", "public", "return", "short", "static", "strictfp",
        "super", "switch", "synchronized", "this", "throw", "throws", "transient",
        "try", "void", "volatile", "while", "true", "false", "null"
    };
    
    private static final String[] JAVA_TYPES = {
        "String", "Integer", "Double", "Float", "Boolean", "Character", "Byte",
        "Short", "Long", "Object", "Class", "List", "ArrayList", "HashMap", "Map",
        "Set", "HashSet", "LinkedList", "Vector", "Stack", "Queue", "Deque",
        "Collection", "Iterator", "Comparator", "Exception", "RuntimeException",
        "Thread", "Runnable", "StringBuilder", "StringBuffer", "Scanner", "File",
        "InputStream", "OutputStream", "BufferedReader", "FileReader", "PrintWriter"
    };
    
    public CodeEditor(Context context) {
        super(context);
        init();
    }
    
    public CodeEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CodeEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (!isHighlighting) {
                    highlightSyntax(s);
                }
            }
        });
        
        setHorizontallyScrolling(true);
        setSingleLine(false);
        setTextIsSelectable(true);
    }
    
    private void highlightSyntax(Editable editable) {
        isHighlighting = true;
        
        ForegroundColorSpan[] spans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : spans) {
            editable.removeSpan(span);
        }
        
        String text = editable.toString();
        
        highlightComments(editable, text);
        highlightStrings(editable, text);
        highlightNumbers(editable, text);
        highlightKeywords(editable, text);
        highlightTypes(editable, text);
        highlightAnnotations(editable, text);
        
        isHighlighting = false;
    }
    
    private void highlightComments(Editable editable, String text) {
        Pattern singleLineComment = Pattern.compile("//.*$", Pattern.MULTILINE);
        Matcher matcher = singleLineComment.matcher(text);
        while (matcher.find()) {
            editable.setSpan(new ForegroundColorSpan(COLOR_COMMENT),
                    matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        Pattern multiLineComment = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
        matcher = multiLineComment.matcher(text);
        while (matcher.find()) {
            editable.setSpan(new ForegroundColorSpan(COLOR_COMMENT),
                    matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    private void highlightStrings(Editable editable, String text) {
        Pattern stringPattern = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");
        Matcher matcher = stringPattern.matcher(text);
        while (matcher.find()) {
            editable.setSpan(new ForegroundColorSpan(COLOR_STRING),
                    matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        Pattern charPattern = Pattern.compile("'([^'\\\\]|\\\\.)*'");
        matcher = charPattern.matcher(text);
        while (matcher.find()) {
            editable.setSpan(new ForegroundColorSpan(COLOR_STRING),
                    matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    private void highlightNumbers(Editable editable, String text) {
        Pattern numberPattern = Pattern.compile("\\b\\d+(\\.\\d+)?[fFdDlL]?\\b");
        Matcher matcher = numberPattern.matcher(text);
        while (matcher.find()) {
            editable.setSpan(new ForegroundColorSpan(COLOR_NUMBER),
                    matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    private void highlightKeywords(Editable editable, String text) {
        for (String keyword : JAVA_KEYWORDS) {
            Pattern pattern = Pattern.compile("\\b" + keyword + "\\b");
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                editable.setSpan(new ForegroundColorSpan(COLOR_KEYWORD),
                        matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
    
    private void highlightTypes(Editable editable, String text) {
        for (String type : JAVA_TYPES) {
            Pattern pattern = Pattern.compile("\\b" + type + "\\b");
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                editable.setSpan(new ForegroundColorSpan(COLOR_TYPE),
                        matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
    
    private void highlightAnnotations(Editable editable, String text) {
        Pattern annotationPattern = Pattern.compile("@\\w+");
        Matcher matcher = annotationPattern.matcher(text);
        while (matcher.find()) {
            editable.setSpan(new ForegroundColorSpan(COLOR_ANNOTATION),
                    matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    public void insertText(String text) {
        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();
        
        Editable editable = getText();
        if (editable != null) {
            editable.replace(selectionStart, selectionEnd, text);
        }
    }
    
    public void autoIndent() {
        int cursorPosition = getSelectionStart();
        String text = getText().toString();
        
        if (cursorPosition > 0 && text.charAt(cursorPosition - 1) == '\n') {
            String lines[] = text.substring(0, cursorPosition).split("\n");
            if (lines.length > 1) {
                String previousLine = lines[lines.length - 2];
                String indent = "";
                
                for (char c : previousLine.toCharArray()) {
                    if (c == ' ' || c == '\t') {
                        indent += c;
                    } else {
                        break;
                    }
                }
                
                if (previousLine.trim().endsWith("{")) {
                    indent += "    ";
                }
                
                insertText(indent);
            }
        }
    }
    
    public void formatCode() {
        String text = getText().toString();
        String formattedText = JavaCodeFormatter.format(text);
        setText(formattedText);
    }
}
