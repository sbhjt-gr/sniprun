package javax.lang.model;

/**
 * Minimal implementation of SourceVersion for Android compatibility with Eclipse JDT
 */
public enum SourceVersion {
    RELEASE_0,
    RELEASE_1,
    RELEASE_2,
    RELEASE_3,
    RELEASE_4,
    RELEASE_5,
    RELEASE_6,
    RELEASE_7,
    RELEASE_8,
    RELEASE_9,
    RELEASE_10,
    RELEASE_11,
    RELEASE_12,
    RELEASE_13,
    RELEASE_14,
    RELEASE_15,
    RELEASE_16,
    RELEASE_17,
    RELEASE_18,
    RELEASE_19,
    RELEASE_20,
    RELEASE_21;
    
    public static SourceVersion latest() {
        return RELEASE_11; // We'll target Java 11 for Android compatibility
    }
    
    public static SourceVersion latestSupported() {
        return latest();
    }
    
    public static boolean isIdentifier(CharSequence name) {
        if (name == null || name.length() == 0) {
            return false;
        }
        
        char first = name.charAt(0);
        if (!Character.isJavaIdentifierStart(first)) {
            return false;
        }
        
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean isKeyword(CharSequence s) {
        String str = s.toString();
        switch (str) {
            case "abstract": case "assert": case "boolean": case "break": case "byte":
            case "case": case "catch": case "char": case "class": case "const": 
            case "continue": case "default": case "do": case "double": case "else":
            case "enum": case "extends": case "final": case "finally": case "float":
            case "for": case "goto": case "if": case "implements": case "import":
            case "instanceof": case "int": case "interface": case "long": case "native":
            case "new": case "package": case "private": case "protected": case "public":
            case "return": case "short": case "static": case "strictfp": case "super":
            case "switch": case "synchronized": case "this": case "throw": case "throws":
            case "transient": case "try": case "void": case "volatile": case "while":
            case "true": case "false": case "null":
                return true;
            default:
                return false;
        }
    }
    
    public static boolean isName(CharSequence name) {
        if (name == null || name.length() == 0) {
            return false;
        }
        
        String[] parts = name.toString().split("\\.");
        for (String part : parts) {
            if (!isIdentifier(part) || isKeyword(part)) {
                return false;
            }
        }
        return true;
    }
}
