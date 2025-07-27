package com.gorai.sniprun;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;

public class LineNumberView extends View {
    
    private Paint paint;
    private Paint backgroundPaint;
    private Rect textBounds;
    
    private CodeEditor codeEditor;
    private int lineCount = 1;
    private int maxDigits = 1;
    private float textSize = 12f;
    private int textColor = Color.parseColor("#888888");
    private int backgroundColor = Color.parseColor("#2D2D2D");
    private int currentLineColor = Color.parseColor("#FFAA00");
    private int padding = 8;
    
    private boolean showCurrentLine = true;
    private int currentLine = 1;
    
    public LineNumberView(Context context) {
        super(context);
        init();
    }
    
    public LineNumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public LineNumberView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        
        backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
        
        textBounds = new Rect();
        
        setBackgroundColor(backgroundColor);
    }
    
    public void attachToCodeEditor(CodeEditor codeEditor) {
        this.codeEditor = codeEditor;
        
        codeEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLineCount(s.toString());
                updateCurrentLine();
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                invalidate();
            }
        });
        
        codeEditor.setOnSelectionChangedListener(new CodeEditor.OnSelectionChangedListener() {
            @Override
            public void onSelectionChanged(int selStart, int selEnd) {
                updateCurrentLine();
                invalidate();
            }
        });
        
        updateLineCount(codeEditor.getText().toString());
    }
    
    private void updateLineCount(String text) {
        lineCount = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lineCount++;
            }
        }
        
        int newMaxDigits = String.valueOf(lineCount).length();
        if (newMaxDigits != maxDigits) {
            maxDigits = newMaxDigits;
            requestLayout();
        }
    }
    
    private void updateCurrentLine() {
        if (codeEditor != null) {
            String text = codeEditor.getText().toString();
            int cursorPosition = codeEditor.getSelectionStart();
            
            currentLine = 1;
            for (int i = 0; i < Math.min(cursorPosition, text.length()); i++) {
                if (text.charAt(i) == '\n') {
                    currentLine++;
                }
            }
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        paint.getTextBounds("0", 0, 1, textBounds);
        int width = (maxDigits * textBounds.width()) + (padding * 2);
        
        setMeasuredDimension(width, MeasureSpec.getSize(heightMeasureSpec));
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (codeEditor == null) {
            return;
        }
        
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        
        int scrollY = codeEditor.getEditorScrollY();
        float lineHeight = codeEditor.getLineHeight();
        
        int firstVisibleLine = Math.max(1, (int) (scrollY / lineHeight) + 1);
        int lastVisibleLine = Math.min(lineCount, 
            (int) ((scrollY + getHeight()) / lineHeight) + 2);
        
        for (int line = firstVisibleLine; line <= lastVisibleLine; line++) {
            float y = (line - 1) * lineHeight - scrollY + textBounds.height() + padding;
            
            if (showCurrentLine && line == currentLine) {
                paint.setColor(currentLineColor);
            } else {
                paint.setColor(textColor);
            }
            
            String lineNumber = String.valueOf(line);
            float x = getWidth() - padding - paint.measureText(lineNumber);
            
            canvas.drawText(lineNumber, x, y, paint);
        }
    }
    
    public void setTextSize(float textSize) {
        this.textSize = textSize;
        paint.setTextSize(textSize);
        requestLayout();
        invalidate();
    }
    
    public void setTextColor(int textColor) {
        this.textColor = textColor;
        invalidate();
    }
    
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        backgroundPaint.setColor(backgroundColor);
        super.setBackgroundColor(backgroundColor);
        invalidate();
    }
    
    public void setCurrentLineColor(int currentLineColor) {
        this.currentLineColor = currentLineColor;
        invalidate();
    }
    
    public void setShowCurrentLine(boolean showCurrentLine) {
        this.showCurrentLine = showCurrentLine;
        invalidate();
    }
    
    public void setPadding(int padding) {
        this.padding = padding;
        requestLayout();
        invalidate();
    }
    
    public int getLineCount() {
        return lineCount;
    }
    
    public int getCurrentLine() {
        return currentLine;
    }
    
    public int getLineNumberWidth() {
        return (maxDigits * textBounds.width()) + (padding * 2);
    }
}
