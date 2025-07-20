package com.gorai.sniprun;

public class CodeTemplates {
    
    private static final String[] TEMPLATE_NAMES = {
        "Hello World",
        "Basic Math",
        "Collections Demo",
        "For Loop Example",
        "String Operations"
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
        
        "import java.util.*;\n" +
        "\n" +
        "public class CollectionsDemo {\n" +
        "    public static void main(String[] args) {\n" +
        "        List<String> fruits = new ArrayList<>();\n" +
        "        fruits.add(\"Apple\");\n" +
        "        fruits.add(\"Banana\");\n" +
        "        fruits.add(\"Cherry\");\n" +
        "        \n" +
        "        System.out.println(\"Fruits:\");\n" +
        "        for (String fruit : fruits) {\n" +
        "            System.out.println(\"- \" + fruit);\n" +
        "        }\n" +
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
        
        "public class StringOperations {\n" +
        "    public static void main(String[] args) {\n" +
        "        String text = \"Hello, Java!\";\n" +
        "        System.out.println(\"Original: \" + text);\n" +
        "        System.out.println(\"Uppercase: \" + text.toUpperCase());\n" +
        "        System.out.println(\"Length: \" + text.length());\n" +
        "        \n" +
        "        String name = \"Android\";\n" +
        "        String greeting = \"Welcome to \" + name + \" programming!\";\n" +
        "        System.out.println(greeting);\n" +
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
