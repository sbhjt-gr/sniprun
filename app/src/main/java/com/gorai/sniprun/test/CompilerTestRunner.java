package com.gorai.sniprun.test;

import android.content.Context;
import android.util.Log;
import com.gorai.sniprun.compiler.ProfessionalJavaCompiler;
import com.gorai.sniprun.compiler.AndroidCompatibleJavaCompiler;

public class CompilerTestRunner {
    private static final String TAG = "CompilerTest";
    
    public static void testCompilers(Context context) {
        
        String simpleCode = "System.out.println(\"Hello World!\");";
        String complexCode = "import java.util.*;\n" +
                           "public class Test {\n" +
                           "    public static void main(String[] args) {\n" +
                           "        List<String> list = new ArrayList<>();\n" +
                           "        list.add(\"Java\");\n" +
                           "        System.out.println(\"Size: \" + list.size());\n" +
                           "    }\n" +
                           "}";
        
        testProfessionalCompiler(context, simpleCode);
        testProfessionalCompiler(context, complexCode);
        
        testAndroidCompatibleCompiler(context, simpleCode);
        testAndroidCompatibleCompiler(context, complexCode);
        
        Log.d(TAG, "Compiler tests completed");
    }
    
    private static void testProfessionalCompiler(Context context, String code) {
        try {
            Log.d(TAG, "Testing ProfessionalJavaCompiler with code: " + code.substring(0, Math.min(50, code.length())));
            ProfessionalJavaCompiler compiler = new ProfessionalJavaCompiler(context);
            ProfessionalJavaCompiler.CompilationResult result = compiler.compileAndExecute(code);
            
            if (result.isSuccess()) {
                Log.d(TAG, "✓ ProfessionalJavaCompiler SUCCESS - Output: " + result.getOutput());
            } else {
                Log.d(TAG, "✗ ProfessionalJavaCompiler FAILED - Error: " + result.getErrorMessage());
                if (!result.getCompilationErrors().isEmpty()) {
                    for (String error : result.getCompilationErrors()) {
                        Log.d(TAG, "  Compilation Error: " + error);
                    }
                }
            }
            Log.d(TAG, "  Execution time: " + result.getExecutionTimeMs() + "ms");
            
        } catch (Exception e) {
            Log.e(TAG, "ProfessionalJavaCompiler test failed with exception", e);
        }
    }
    
    private static void testAndroidCompatibleCompiler(Context context, String code) {
        try {
            Log.d(TAG, "Testing AndroidCompatibleJavaCompiler with code: " + code.substring(0, Math.min(50, code.length())));
            AndroidCompatibleJavaCompiler compiler = new AndroidCompatibleJavaCompiler(context);
            AndroidCompatibleJavaCompiler.CompilationResult result = compiler.compileAndExecute(code);
            
            if (result.isSuccess()) {
                Log.d(TAG, "✓ AndroidCompatibleJavaCompiler SUCCESS - Output: " + result.getOutput());
            } else {
                Log.d(TAG, "✗ AndroidCompatibleJavaCompiler FAILED - Error: " + result.getErrorMessage());
                if (!result.getCompilationErrors().isEmpty()) {
                    for (String error : result.getCompilationErrors()) {
                        Log.d(TAG, "  Compilation Error: " + error);
                    }
                }
            }
            Log.d(TAG, "  Execution time: " + result.getExecutionTimeMs() + "ms");
            
        } catch (Exception e) {
            Log.e(TAG, "AndroidCompatibleJavaCompiler test failed with exception", e);
        }
    }
}
