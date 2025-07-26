package javax.lang.model.element;

/**
 * Minimal implementation for Android compatibility with Eclipse JDT
 */
public enum ElementKind {
    PACKAGE,
    ENUM,
    CLASS,
    ANNOTATION_TYPE,
    INTERFACE,
    ENUM_CONSTANT,
    FIELD,
    PARAMETER,
    LOCAL_VARIABLE,
    EXCEPTION_PARAMETER,
    METHOD,
    CONSTRUCTOR,
    STATIC_INIT,
    INSTANCE_INIT,
    TYPE_PARAMETER,
    OTHER,
    RESOURCE_VARIABLE,
    MODULE,
    RECORD,
    RECORD_COMPONENT,
    BINDING_VARIABLE;
    
    public boolean isClass() {
        return this == CLASS;
    }
    
    public boolean isInterface() {
        return this == INTERFACE;
    }
    
    public boolean isField() {
        return this == FIELD || this == ENUM_CONSTANT;
    }
}
