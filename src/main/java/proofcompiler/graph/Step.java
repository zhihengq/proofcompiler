package proofcompiler.graph;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import proofcompiler.ast.Number;
import proofcompiler.ast.logic.Proposition;
import static proofcompiler.ast.logic.Proposition.TRUE;
import static proofcompiler.ast.logic.Proposition.FALSE;
import static proofcompiler.ast.logic.Proposition.meta;
import static proofcompiler.ast.logic.Proposition.not;
import static proofcompiler.ast.logic.Proposition.and;
import static proofcompiler.ast.logic.Proposition.or;
import static proofcompiler.ast.logic.Proposition.xor;
import static proofcompiler.ast.logic.Proposition.implies;

public abstract class Step implements Comparable<Step> {
    public final Number number;
    public final Proposition proposition;

    @FunctionalInterface
    public interface StepConstructor {
        Step apply(Number number, Proposition proposition, String ruleName, Collection<Step> dependencies)
                throws RuleCheckException;
    }

    public static final Map<String, StepConstructor> constructors;
    public static final String GIVEN = "given";
    public static final String ASSUMPTION = "assumption";
    public static final String DPR = "direct proof rule";

    static {
        constructors = new HashMap<>();

        // Inference rules
        Proposition A = meta("A");
        Proposition B = meta("B");
        constructors.put("excluded middle", Inference.rule(
                    List.of(),
                    List.of(or(A, not(A)))));
        constructors.put("modus ponens",    Inference.rule(
                    List.of(A, implies(A, B)),
                    List.of(B)));
        constructors.put("intro and",       Inference.rule(
                    List.of(A, B),
                    List.of(and(A, B))));
        constructors.put("elim and",        Inference.rule(
                    List.of(and(A, B)),
                    List.of(A, B)));
        constructors.put("intro or",        Inference.rule(
                    List.of(A),
                    List.of(or(A, B), or(B, A))));
        constructors.put("elim or",         Inference.rule(
                    List.of(or(A, B), not(A)),
                    List.of(B)));

        // Equivalence rules
        Proposition p = meta("p");
        Proposition q = meta("q");
        Proposition r = meta("r");
        constructors.put("identity",        Equivalence.rule(List.of(
                        Equivalence.equ(and(p, TRUE), p),
                        Equivalence.equ(or(p, FALSE), p)
                    )));
        constructors.put("domination",      Equivalence.rule(List.of(
                        Equivalence.equ(or(p, TRUE), TRUE),
                        Equivalence.equ(and(p, FALSE), FALSE)
                    )));
        constructors.put("idempotency",     Equivalence.rule(List.of(
                        Equivalence.equ( or(p, p), p),
                        Equivalence.equ(and(p, p), p)
                    )));
        constructors.put("commutativity",   Equivalence.rule(List.of(
                        Equivalence.equ( or(p, q),  or(q, p)),
                        Equivalence.equ(and(p, q), and(q, p))
                    )));
        constructors.put("associativity",   Equivalence.rule(List.of(
                        Equivalence.equ( or( or(p, q), r),  or(p,  or(q, r))),
                        Equivalence.equ(and(and(p, q), r), and(p, and(q, r)))
                    )));
        constructors.put("distributivity",  Equivalence.rule(List.of(
                        Equivalence.equ(and(p,  or(q, r)),  or(and(p, q), and(p, r))),
                        Equivalence.equ( or(p, and(q, r)), and( or(p, q),  or(q, r)))
                    )));
        constructors.put("absorption",      Equivalence.rule(List.of(
                        Equivalence.equ( or(p, and(p, q)), p),
                        Equivalence.equ(and(p,  or(p, q)), p)
                    )));
        constructors.put("negation",        Equivalence.rule(List.of(
                        Equivalence.equ( or(p, not(p)), TRUE),
                        Equivalence.equ(and(p, not(p)), FALSE)
                    )));
        constructors.put("demorgan's law",  Equivalence.rule(List.of(
                        Equivalence.equ(not( or(p, q)), and(not(p), not(q))),
                        Equivalence.equ(not(and(p, q)),  or(not(p), not(q)))
                    )));
        constructors.put("double negation", Equivalence.rule(List.of(
                        Equivalence.equ(not(not(p)), p)
                    )));
        constructors.put("law of implication", Equivalence.rule(List.of(
                        Equivalence.equ(implies(p, q), or(not(p), q))
                    )));
        constructors.put("contrapositive",  Equivalence.rule(List.of(
                        Equivalence.equ(implies(p, q), implies(not(q), not(p)))
                    )));

        // Equivalence rules by boolean algebra names
        Proposition X = meta("X");
        Proposition Y = meta("Y");
        Proposition Z = meta("Z");
        constructors.put("complementarity", constructors.get("negation"));
        constructors.put("null",            constructors.get("domination"));
        constructors.put("involution",      constructors.get("double negation"));
        constructors.put("uniting",         Equivalence.rule(List.of(
                        Equivalence.equ( or(and(X, Y), and(X, not(Y))), X),
                        Equivalence.equ(and( or(X, Y),  or(X, not(Y))), X)
                    )));
        constructors.put("absorption",      Equivalence.rule(List.of(
                        Equivalence.equ( or(X, and(X, Y)), X),
                        Equivalence.equ(and(X,  or(X, Y)), X),
                        Equivalence.equ(and( or(X, not(Y)), Y), and(X, Y)),
                        Equivalence.equ( or(and(X, not(Y)), Y),  or(X, Y))
                    )));
        constructors.put("consensus",       Equivalence.rule(List.of(
                        Equivalence.equ( or( or(and(X, Y), and(Y, Z)), and(not(X), Z)),  or(and(X, Y), and(not(X), Z))),
                        Equivalence.equ(and(and( or(X, Y),  or(Y, Z)),  or(not(X), Z)), and( or(X, Y),  or(not(X), Z)))
                    )));
        constructors.put("factoring",       Equivalence.rule(List.of(
                        Equivalence.equ(and( or(X, Y),  or(not(X), Z)),  or(and(X, Y), and(not(X), Z))),
                        Equivalence.equ( or(and(X, Y), and(not(X), Z)), and( or(X, Y),  or(not(X), Z)))
                    )));
    }

    public abstract class RuleCheckException extends Exception {
        protected static final long serialVersionUID = 0;

        protected String number() {
            return Step.this.number.toString();
        }

        protected String proposition() {
            return Step.this.proposition.toString();
        }

        @Override
        public String getMessage() {
            return String.format("at line %s [%s]", number(), proposition());
        }
    };

    public class InvalidDependenciesException extends RuleCheckException {
        protected static final long serialVersionUID = 0;
        private final int expected;
        private final int actual;

        public InvalidDependenciesException(int expected, int actual) {
            this.expected = expected;
            this.actual = actual;
        }

        @Override
        public String getMessage() {
            return String.format(
                    "%s: expect %s dependencies but got %s",
                    super.getMessage(), expected, actual);
        }
    }

    protected Step(Number number, Proposition proposition,
            Collection<Step> dependencies, int expectedNumberDependencies)
            throws RuleCheckException {
        this.number = number;
        this.proposition = proposition;
        if (expectedNumberDependencies != dependencies.size())
            throw new InvalidDependenciesException(
                    expectedNumberDependencies,
                    dependencies.size());
    }

    public abstract String ruleName();
    public Collection<Step> dependencies() { return List.of(); }
    public Number number(Number prev) { return prev.next(); }

    @Override
    public int compareTo(Step that) {
        return this.number.compareTo(that.number);
    }
}
