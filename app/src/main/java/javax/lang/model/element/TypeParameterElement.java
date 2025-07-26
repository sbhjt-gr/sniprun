package javax.lang.model.element;

/**
 * Minimal implementation for Android compatibility with Eclipse JDT
 */
public interface TypeParameterElement extends Element {
    @Override
    default ElementKind getKind() {
        return ElementKind.TYPE_PARAMETER;
    }
}
