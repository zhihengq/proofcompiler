package proofcompiler.graph;

import java.util.Collection;

import proofcompiler.ast.Number;
import proofcompiler.ast.logic.Proposition;
import proofcompiler.ast.logic.Atomic;
import proofcompiler.ast.logic.UnaryOp;
import proofcompiler.ast.logic.BinaryOp;
import proofcompiler.ast.logic.PropositionVisitor;

public class Equivalence extends Step {
    private final String ruleName;
    private final Collection<Step> dependencies;

    public static Proposition equ(Proposition lhs, Proposition rhs) {
        return Proposition.biconditional(lhs, rhs);
    }

    public static class EquivalenceConstructor implements Step.StepConstructor {
        private final Collection<Proposition> equivalences;

        EquivalenceConstructor(Collection<Proposition> equivalences) {
            this.equivalences = equivalences;
        }

        @Override
        public Equivalence apply(Number number, Proposition proposition, String ruleName, Collection<Step> dependencies)
            throws Step.RuleCheckException {
            return new Equivalence(number, proposition, ruleName, dependencies, equivalences);
        }
    }

    public static EquivalenceConstructor rule(Collection<Proposition> equivalences) {
        return new EquivalenceConstructor(equivalences);
    }

    public class RuleCheckException extends Step.RuleCheckException {
        public static final long serialVersionUID = 0;

        @Override
        public String getMessage() {
            return String.format("%s: equivalence rule does not match", super.getMessage());
        }
    }

    private Equivalence(
            Number number, Proposition proposition, String ruleName, Collection<Step> dependencies,
            Collection<Proposition> equivalences)
            throws Step.RuleCheckException {
        super(number, proposition, dependencies, 1);
        if (!match(equivalences, dependencies.stream().findAny().get().proposition, proposition))
            throw new RuleCheckException();
        this.ruleName = ruleName;
        this.dependencies = dependencies;
    }

    @Override
    public String ruleName() {
        return ruleName;
    }

    @Override
    public Collection<Step> dependencies() {
        return dependencies;
    }

    public static boolean match(
            Collection<Proposition> equivalences,
            Proposition lhs,
            Proposition rhs) {
        return new MatcherCreator(equivalences).visit(lhs).visit(rhs);
    }

    private static abstract class Matcher extends PropositionVisitor<Boolean> {
        protected final Collection<Proposition> equivalences;
        Matcher(Collection<Proposition> equivalences) { this.equivalences = equivalences; }
        protected abstract Proposition lhs();
        @Override public Boolean visitTrue()         { return unify(Proposition.TRUE); }
        @Override public Boolean visitFalse()        { return unify(Proposition.FALSE); }
        @Override public Boolean visit(Atomic   rhs) { return unify(rhs); }
        @Override public Boolean visit(UnaryOp  rhs) { return unify(rhs); }
        @Override public Boolean visit(BinaryOp rhs) { return unify(rhs); }

        protected boolean unify(Proposition rhs) {
            return equivalences.stream()
                .filter(e ->
                        Inference.unify(e, equ(lhs(), rhs)) != null ||
                        Inference.unify(e, equ(rhs, lhs())) != null)
                .findAny()
                .isPresent();
        }
    }

    private static class TrueMatcher extends Matcher {
        TrueMatcher(Collection<Proposition> equivalences) { super(equivalences); }
        @Override protected Proposition lhs() { return Proposition.TRUE; }
        @Override public Boolean visitTrue() { return true; }
    }

    private static class FalseMatcher extends Matcher {
        FalseMatcher(Collection<Proposition> equivalences) { super(equivalences); }
        @Override protected Proposition lhs() { return Proposition.FALSE; }
        @Override public Boolean visitFalse() { return true; }
    }

    private static class AtomicMatcher extends Matcher {
        private final Atomic lhs;
        AtomicMatcher(Collection<Proposition> equivalences, Atomic lhs) {
            super(equivalences);
            this.lhs = lhs;
        }
        @Override protected Proposition lhs() { return lhs; }
        @Override public Boolean visit(Atomic rhs) {
            return lhs.equals(rhs) || unify(rhs);
        }
    }

    private static class UnaryMatcher extends Matcher {
        private final UnaryOp lhs;
        public UnaryMatcher(Collection<Proposition> equivalences, UnaryOp lhs) {
            super(equivalences);
            this.lhs = lhs;
        }
        @Override protected Proposition lhs() { return lhs; }
        @Override public Boolean visit(UnaryOp rhs) {
            return (lhs.type == rhs.type && match(equivalences, lhs.arg, rhs.arg))
                || unify(rhs);
        }
    }

    private static class BinaryMatcher extends Matcher {
        private final BinaryOp lhs;
        public BinaryMatcher(Collection<Proposition> equivalences, BinaryOp lhs) {
            super(equivalences);
            this.lhs = lhs;
        }
        @Override protected Proposition lhs() { return lhs; }
        @Override public Boolean visit(BinaryOp rhs) {
            return (lhs.type == rhs.type &&
                    match(equivalences, lhs.lhs, rhs.lhs) &&
                    match(equivalences, lhs.rhs, rhs.rhs))
                || unify(rhs);
        }
    }

    private static class MatcherCreator extends PropositionVisitor<Matcher> {
        private final Collection<Proposition> equivalences;
        MatcherCreator(Collection<Proposition> equivalences) { this.equivalences = equivalences; }
        @Override public Matcher visitTrue()         { return new   TrueMatcher(equivalences); }
        @Override public Matcher visitFalse()        { return new  FalseMatcher(equivalences); }
        @Override public Matcher visit(Atomic   lhs) { return new AtomicMatcher(equivalences, lhs); }
        @Override public Matcher visit(UnaryOp  lhs) { return new  UnaryMatcher(equivalences, lhs); }
        @Override public Matcher visit(BinaryOp lhs) { return new BinaryMatcher(equivalences, lhs); }
    }
}
