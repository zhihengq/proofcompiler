package proofcompiler.ast.logic;

/**
 * Atomic variable. Immutable.
 */
public class Atomic implements Proposition {
    public final String name;

    public Atomic(String name) {
        this.name = name;
    }

    @Override
    public <T> T accept(PropositionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Atomic))
            return false;
        Atomic that = (Atomic) o;
        return this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
