package proofcompiler.ast;

import java.util.Collections;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A equivalence/inference rule with reference line(s).
 * Immutable.
 */
public class Rule {
    public final String name;
    public final Set<Number> refs;

    public Rule(String name, Collection<Number> refs) {
        this.name = name;
        this.refs = refs.stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String toString() {
        var refs = String.join(", ", this.refs.stream()
                .sorted()
                .map(n -> n.toString())
                .collect(Collectors.toUnmodifiableList()));
        return name + (refs.isEmpty() ? "" : ": ") + refs;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Rule))
            return false;
        Rule other = (Rule) o;
        return name.equals(other.name) && refs.equals(other.refs);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, refs);
    }
}
