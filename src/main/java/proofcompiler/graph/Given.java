package proofcompiler.graph;

import java.util.Collection;
import proofcompiler.ast.Declarations;
import proofcompiler.ast.Number;
import proofcompiler.ast.logic.Proposition;

public class Given extends Assumption {

    public class RuleCheckException extends Step.RuleCheckException {
        public static final long serialVersionUID = 0;

        @Override
        public String getMessage() {
            return String.format("%s: proposition is not given", super.getMessage());
        }
    }

    public Given(Number number, Proposition proposition, Collection<Step> dependencies, Declarations decls)
            throws Step.RuleCheckException {
        super(number, proposition, dependencies);
        if (!decls.givens().contains(proposition))
            throw new RuleCheckException();
    }

    @Override
    public String ruleName() {
        return Step.GIVEN;
    }

    @Override
    public Number number(Number prev) {
        return prev.next();
    }
}
