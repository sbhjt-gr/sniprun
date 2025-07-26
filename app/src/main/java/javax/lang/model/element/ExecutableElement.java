package javax.lang.model.element;

/**
 * Minimal implementation for Android compatibility with Eclipse JDT
 */
public interface ExecutableElement extends Element {
    @Override
    default ElementKind getKind() {
        return ElementKind.METHOD;
    }
}
