package javax.lang.model.element;

/**
 * Minimal implementation for Android compatibility with Eclipse JDT
 */
public interface PackageElement extends Element {
    @Override
    default ElementKind getKind() {
        return ElementKind.PACKAGE;
    }
}
