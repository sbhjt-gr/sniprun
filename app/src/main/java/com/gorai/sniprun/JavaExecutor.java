package com.gorai.sniprun;

import android.content.Context;
import android.util.Log;

import com.gorai.sniprun.compiler.ProfessionalJavaCompiler;
import com.gorai.sniprun.compiler.AndroidCompatibleJavaCompiler;

import java.util.List;

public class JavaExecutor {
    
    private static final String TAG = "JavaExecutor";
    private final ProfessionalJavaCompiler compiler;
    private final AndroidCompatibleJavaCompiler fallbackCompiler;
    
    public static class ExecutionResult {
        private final boolean success;
        private final String output;
        private final String errorMessage;
        private final List<String> compilationErrors;
        private final long executionTimeMs;
        
        public ExecutionResult(boolean success, String output, String errorMessage, 
                             List<String> compilationErrors, long executionTimeMs) {
            this.success = success;
            this.output = output;
            this.errorMessage = errorMessage;
            this.compilationErrors = compilationErrors;
            this.executionTimeMs = executionTimeMs;
        }
        
        public boolean isSuccess() { 
            return success; 
        }
        
        public String getOutput() { 
            return output != null ? output : ""; 
        }
        
        public String getErrorMessage() { 
            return errorMessage; 
        }
        
        public List<String> getCompilationErrors() { 
            return compilationErrors; 
        }
        
        public long getExecutionTimeMs() { 
            return executionTimeMs; 
        }
        
        public String getFormattedErrorMessage() {
            if (errorMessage == null && (compilationErrors == null || compilationErrors.isEmpty())) {
                return null;
            }
            
            StringBuilder formatted = new StringBuilder();
            
            if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                formatted.append(errorMessage);
                if (compilationErrors != null && !compilationErrors.isEmpty()) {
                    formatted.append("\n\nCompilation errors:\n");
                }
            }
            
            if (compilationErrors != null && !compilationErrors.isEmpty()) {
                for (String error : compilationErrors) {
                    formatted.append("• ").append(error).append("\n");
                }
            }
            
            return formatted.toString().trim();
        }
    }
    
    public JavaExecutor(Context context) {
        this.compiler = new ProfessionalJavaCompiler(context);
        this.fallbackCompiler = new AndroidCompatibleJavaCompiler(context);
    }
    
    public ExecutionResult executeJavaCode(String sourceCode) {
        try {
            Log.d(TAG, "Attempting to compile and execute Java code");
            
            ProfessionalJavaCompiler.CompilationResult result = compiler.compileAndExecute(sourceCode);
            
            if (result.isSuccess()) {
                Log.d(TAG, "ProfessionalJavaCompiler succeeded");
                return new ExecutionResult(
                    result.isSuccess(),
                    result.getOutput(),
                    result.getErrorMessage(),
                    result.getCompilationErrors(),
                    result.getExecutionTimeMs()
                );
            } else {
                Log.d(TAG, "ProfessionalJavaCompiler failed, using AndroidCompatibleJavaCompiler as fallback");
                
                AndroidCompatibleJavaCompiler.CompilationResult fallbackResult = 
                    fallbackCompiler.compileAndExecute(sourceCode);
                
                if (fallbackResult.isSuccess()) {
                    Log.d(TAG, "AndroidCompatibleJavaCompiler fallback succeeded");
                    return new ExecutionResult(
                        fallbackResult.isSuccess(),
                        fallbackResult.getOutput(),
                        null,
                        null,
                        fallbackResult.getExecutionTimeMs()
                    );
                } else {
                    Log.e(TAG, "Both compilers failed");
                    return new ExecutionResult(
                        false,
                        "",
                        "Compilation failed: " + (result.getErrorMessage() != null ? result.getErrorMessage() : "Unknown error"),
                        result.getCompilationErrors(),
                        result.getExecutionTimeMs()
                    );
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error executing Java code", e);
            return new ExecutionResult(
                false,
                "",
                "Execution error: " + e.getMessage(),
                null,
                0
            );
        }
    }
}
