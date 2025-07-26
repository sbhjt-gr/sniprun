package com.gorai.sniprun.test;

import android.content.Context;
import android.util.Log;

import com.gorai.sniprun.JavaExecutor;

public class JavaExecutorTest {
    
    private static final String TAG = "JavaExecutorTest";
    
    public static void runBasicTests(Context context) {
        Log.d(TAG, "Starting JavaExecutor tests");
        
        JavaExecutor executor = new JavaExecutor(context);
        
        testSimpleHelloWorld(executor);
        testBasicMath(executor);
        testVariableDeclaration(executor);
        testSimpleLoop(executor);
        
        Log.d(TAG, "JavaExecutor tests completed");
    }
    
    private static void testSimpleHelloWorld(JavaExecutor executor) {
        Log.d(TAG, "Testing Hello World");
        
        String code = "System.out.println(\"Hello, World!\");";
        JavaExecutor.ExecutionResult result = executor.executeJavaCode(code);
        
        if (result.isSuccess()) {
            Log.d(TAG, "✓ Hello World SUCCESS - Output: " + result.getOutput());
        } else {
            Log.e(TAG, "✗ Hello World FAILED - Error: " + result.getErrorMessage());
            if (result.getFormattedErrorMessage() != null) {
                Log.e(TAG, "Formatted error: " + result.getFormattedErrorMessage());
            }
        }
    }
    
    private static void testBasicMath(JavaExecutor executor) {
        Log.d(TAG, "Testing Basic Math");
        
        String code = "int a = 5; int b = 10; System.out.println(\"Sum: \" + (a + b));";
        JavaExecutor.ExecutionResult result = executor.executeJavaCode(code);
        
        if (result.isSuccess()) {
            Log.d(TAG, "✓ Basic Math SUCCESS - Output: " + result.getOutput());
        } else {
            Log.e(TAG, "✗ Basic Math FAILED - Error: " + result.getErrorMessage());
            if (result.getFormattedErrorMessage() != null) {
                Log.e(TAG, "Formatted error: " + result.getFormattedErrorMessage());
            }
        }
    }
    
    private static void testVariableDeclaration(JavaExecutor executor) {
        Log.d(TAG, "Testing Variable Declaration");
        
        String code = "String message = \"Test\"; System.out.println(message);";
        JavaExecutor.ExecutionResult result = executor.executeJavaCode(code);
        
        if (result.isSuccess()) {
            Log.d(TAG, "✓ Variable Declaration SUCCESS - Output: " + result.getOutput());
        } else {
            Log.e(TAG, "✗ Variable Declaration FAILED - Error: " + result.getErrorMessage());
            if (result.getFormattedErrorMessage() != null) {
                Log.e(TAG, "Formatted error: " + result.getFormattedErrorMessage());
            }
        }
    }
    
    private static void testSimpleLoop(JavaExecutor executor) {
        Log.d(TAG, "Testing Simple Loop");
        
        String code = "for(int i = 1; i <= 3; i++) { System.out.println(\"Number: \" + i); }";
        JavaExecutor.ExecutionResult result = executor.executeJavaCode(code);
        
        if (result.isSuccess()) {
            Log.d(TAG, "✓ Simple Loop SUCCESS - Output: " + result.getOutput());
        } else {
            Log.e(TAG, "✗ Simple Loop FAILED - Error: " + result.getErrorMessage());
            if (result.getFormattedErrorMessage() != null) {
                Log.e(TAG, "Formatted error: " + result.getFormattedErrorMessage());
            }
        }
    }
}
