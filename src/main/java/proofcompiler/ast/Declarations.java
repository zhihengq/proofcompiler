package proofcompiler.ast;

import java.util.Collections;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import proofcompiler.ast.logic.Proposition;

/**
 * Proof declarations.
 */
public class Declarations {
    private Set<String> atomics;
    private Set<Proposition> givens;

    /**
     * Constructor.
     */
    public Declarations(Collection<String> atomics, Collection<Proposition> givens) {
        if (atomics == null)
            this.atomics = new HashSet<>();
        else
            this.atomics = new HashSet<>(atomics);
        if (givens == null)
            this.givens = new HashSet<>();
        else
            this.givens = new HashSet<>(givens);
    }

    /**
     * Merge two declarations.
     */
    public Declarations merge(Declarations other) {
        atomics.addAll(other.atomics);
        givens.addAll(other.givens);
        return this;
    }

    /**
     * Freeze the declaration.
     */
    public Declarations freeze() {
        atomics = Collections.unmodifiableSet(atomics);
        givens = Collections.unmodifiableSet(givens);
        return this;
    }

    /**
     * Get an unmodifiable view of atomics.
     */
    public Set<String> atomics() {
        return Collections.unmodifiableSet(atomics);
    }

    /**
     * Get an unmodifiable view of givens.
     */
    public Set<Proposition> givens() {
        return Collections.unmodifiableSet(givens);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String id : atomics)
            sb.append("Let " + id + " be a proposition\n");
        for (Proposition prop : givens)
            sb.append("Given " + prop + "\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Declarations))
            return false;
        Declarations other = (Declarations) o;
        return atomics.equals(other.atomics) && givens.equals(other.givens);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(atomics, givens);
    }
}
