package proofcompiler.ast.logic;

/**
 * Meta variable in a propositional rule. Immutable.
 */
public class Meta extends Atomic {

    public Meta(String name) {
        super(name);
    }

    @Override
    public <T> T accept(PropositionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
