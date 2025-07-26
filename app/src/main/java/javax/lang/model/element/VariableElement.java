package javax.lang.model.element;

/**
 * Minimal implementation for Android compatibility with Eclipse JDT
 */
public interface VariableElement extends Element {
    @Override
    default ElementKind getKind() {
        return ElementKind.FIELD;
    }
}
