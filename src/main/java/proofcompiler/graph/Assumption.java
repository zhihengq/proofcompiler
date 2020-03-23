package proofcompiler.graph;

import java.util.Collection;
import proofcompiler.ast.Number;
import proofcompiler.ast.logic.Proposition;

public class Assumption extends Step {
    public Assumption(Number number, Proposition proposition, Collection<Step> dependencies)
            throws Step.RuleCheckException {
        super(number, proposition, dependencies, 0);
    }

    @Override
    public String ruleName() {
        return Step.ASSUMPTION;
    }

    @Override
    public Number number(Number prev) {
        return prev.increaseLevel();
    }
}
