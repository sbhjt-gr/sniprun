package javax.lang.model.element;

/**
 * Minimal implementation for Android compatibility with Eclipse JDT
 */
public interface Element {
    ElementKind getKind();
    
    default <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visit(this, p);
    }
}
