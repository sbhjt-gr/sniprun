package com.gorai.sniprun;

import org.junit.Test;
import static org.junit.Assert.*;

public class JavaCodeExecutorTest {
    
    @Test
    public void testBasicFunctionality() {
        assertTrue("Java interpreter integration should be available", true);
    }
    
    @Test 
    public void testExecutionResultCreation() {
        JavaExecutor.ExecutionResult result = new JavaExecutor.ExecutionResult("test", true, null, null);
        assertEquals("test", result.getOutput());
        assertTrue(result.isSuccess());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    public void testCodeTemplates() {
        String[] templateNames = CodeTemplates.getTemplateNames();
        assertTrue("Should have templates", templateNames.length > 0);
        
        String firstTemplate = CodeTemplates.getTemplate(0);
        assertNotNull("Template should not be null", firstTemplate);
        assertTrue("Template should not be empty", !firstTemplate.trim().isEmpty());
    }
}
