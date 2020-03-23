package proofcompiler.ast.logic;

/**
 * Proposition Visitor.
 */
public abstract class PropositionVisitor<T> {

    public final T visit(Proposition prop) {
        return prop.accept(this);
    }

    public T visitTrue()            { return null; }
    public T visitFalse()           { return null; }
    public T visit(Atomic atomic)   { return null; }
    public T visit(UnaryOp unary)   { return null; }
    public T visit(BinaryOp binary) { return null; }

    public T visit(Meta meta) {
        throw new UnsupportedOperationException(
                String.format("Unexpected meta variable: %s", meta));
    }
}
