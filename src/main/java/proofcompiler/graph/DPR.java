package proofcompiler.graph;

import java.util.Collection;
import java.util.List;
import proofcompiler.ast.Number;
import proofcompiler.ast.logic.Proposition;

public class DPR extends Assumption {
    private final Step assumption;
    private final Step conclusion;

    public class RuleCheckException extends Step.RuleCheckException {
        public static final long serialVersionUID = 0;

        @Override
        public String getMessage() {
            return String.format("%s: invalid use of Direct Proof Rule", super.getMessage());
        }
    }

    public DPR(Number number, Proposition proposition, Collection<Step> dependencies,
            Step assumption, Step conclusion)
            throws Step.RuleCheckException {
        super(number, proposition, dependencies);
        if (!proposition.equals(Proposition.implies(assumption.proposition, conclusion.proposition)))
            throw new RuleCheckException();
        this.assumption = assumption;
        this.conclusion = conclusion;
    }

    @Override
    public String ruleName() {
        return Step.DPR;
    }

    @Override
    public Collection<Step> dependencies() {
        return List.of(assumption, conclusion);
    }

    @Override
    public Number number(Number prev) {
        return prev.decreaseLevel();
    }
}
