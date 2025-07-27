package com.gorai.sniprun;

public class CodeTemplates {
    
    private static final String[] TEMPLATE_NAMES = {
        "Hello World",
        "Basic Math",
        "Simple Variables",
        "For Loop Example",
        "If-Else Example"
    };
    
    private static final String[] TEMPLATES = {
        "public class HelloWorld {\n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(\"Hello, World!\");\n" +
        "    }\n" +
        "}",
        
        "public class BasicMath {\n" +
        "    public static void main(String[] args) {\n" +
        "        int a = 10;\n" +
        "        int b = 20;\n" +
        "        int sum = a + b;\n" +
        "        int product = a * b;\n" +
        "        \n" +
        "        System.out.println(\"a = \" + a);\n" +
        "        System.out.println(\"b = \" + b);\n" +
        "        System.out.println(\"Sum: \" + sum);\n" +
        "        System.out.println(\"Product: \" + product);\n" +
        "    }\n" +
        "}",
        
        "public class SimpleVariables {\n" +
        "    public static void main(String[] args) {\n" +
        "        int number = 42;\n" +
        "        String text = \"Hello\";\n" +
        "        \n" +
        "        System.out.println(\"Number: \" + number);\n" +
        "        System.out.println(\"Text: \" + text);\n" +
        "        System.out.println(\"Sum: \" + (number + 8));\n" +
        "    }\n" +
        "}",
        
        "public class ForLoopExample {\n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(\"Counting from 1 to 10:\");\n" +
        "        for (int i = 1; i <= 10; i++) {\n" +
        "            System.out.println(\"Number: \" + i);\n" +
        "        }\n" +
        "        \n" +
        "        int sum = 0;\n" +
        "        for (int i = 1; i <= 100; i++) {\n" +
        "            sum += i;\n" +
        "        }\n" +
        "        System.out.println(\"Sum 1-100: \" + sum);\n" +
        "    }\n" +
        "}",
        
        "public class IfElseExample {\n" +
        "    public static void main(String[] args) {\n" +
        "        int number = 15;\n" +
        "        \n" +
        "        if (number > 10) {\n" +
        "            System.out.println(number + \" is greater than 10\");\n" +
        "        } else {\n" +
        "            System.out.println(number + \" is not greater than 10\");\n" +
        "        }\n" +
        "        \n" +
        "        if (number % 2 == 0) {\n" +
        "            System.out.println(number + \" is even\");\n" +
        "        } else {\n" +
        "            System.out.println(number + \" is odd\");\n" +
        "        }\n" +
        "    }\n" +
        "}"
    };
    
    public static String[] getTemplateNames() {
        return TEMPLATE_NAMES;
    }
    
    public static String getTemplate(int index) {
        if (index >= 0 && index < TEMPLATES.length) {
            return TEMPLATES[index];
        }
        return TEMPLATES[0];
    }
}
