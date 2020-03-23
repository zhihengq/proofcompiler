package proofcompiler;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Deque;
import java.util.ArrayDeque;
import proofcompiler.ast.Proof;
import proofcompiler.ast.Declarations;
import proofcompiler.ast.Line;
import proofcompiler.ast.Number;
import proofcompiler.ast.logic.PropositionVisitor;
import proofcompiler.ast.logic.Proposition;
import proofcompiler.ast.logic.Atomic;
import proofcompiler.ast.logic.UnaryOp;
import proofcompiler.ast.logic.BinaryOp;
import proofcompiler.graph.Step;
import proofcompiler.graph.Assumption;
import proofcompiler.graph.Given;
import proofcompiler.graph.DPR;

public enum FormatChecker {
    /* no instantiation */;

    public static class FormatCheckException extends Exception {
        protected static final long serialVersionUID = 0;

        public FormatCheckException(Number number, String message) {
            super(String.format("at line %s: %s", number, message));
        }
    }

    public static Step check(Proof ast)
            throws FormatCheckException, Step.RuleCheckException {
        Map<Number, Step> steps = new HashMap<>();
        Deque<Step> assumptions = new ArrayDeque<>();
        Number number = Number.ZERO;
        Step lastStep = null;
        for (Line line : ast.lines) {
            checkAtomics(ast.decls, line);

            var result = checkNumber(number, line.number);
            number = result.next;

            String ruleName = line.rule.name.toLowerCase();
            Collection<Step> deps = new ArrayList<>(line.rule.refs.size());
            for (Number dep : line.rule.refs) {
                Step step = steps.get(dep);
                if (step == null)
                    throw new FormatCheckException(
                            line.number,
                            String.format("referred line '%s' does not exist", dep));
                if (!line.number.hasAccess(dep))
                    throw new FormatCheckException(
                            line.number,
                            String.format("cannot refer to line '%s'", dep));
                deps.add(step);
            }
            Step current = null;
            switch (result.type) {
                case ASSUMPTION:
                    if (!ruleName.equals(Step.ASSUMPTION))
                        throw new FormatCheckException(
                                line.number,
                                "sub-proofs must start with an assumption");
                    current = new Assumption(line.number, line.proposition, deps);
                    assumptions.addLast(current);
                    break;
                case DPR:
                    if (!ruleName.equals(Step.DPR))
                        throw new FormatCheckException(
                                line.number,
                                "sub-proofs must be followed by a Direct Proof Rule");
                    Step assumption = assumptions.removeLast();
                    current = new DPR(line.number, line.proposition, deps, assumption, lastStep);
                    break;
                case NORMAL:
                    if (ruleName.equals(Step.GIVEN)) {
                        current = new Given(line.number, line.proposition, deps, ast.decls);
                    } else {
                        var constructor = Step.constructors.get(ruleName);
                        if (constructor == null)
                            throw new FormatCheckException(
                                    line.number,
                                    String.format("referred rule '%s' does not exist", line.rule.name));
                        current = constructor.apply(line.number, line.proposition, ruleName, deps);
                    }
                    break;
            }
            steps.put(line.number, current);
            lastStep = current;
        }
        if (!assumptions.isEmpty())
            throw new FormatCheckException(lastStep.number, "proof ends in a sub-proof");
        return lastStep;
    }

    private static void checkAtomics(Declarations decls, Line line) throws FormatCheckException {

        /**
         * Returns the offending Atomic if one exists, or null otherwise.
         */
        class AtomicVisitor extends PropositionVisitor<Atomic> {
            @Override
            public Atomic visit(Atomic atomic) {
                if (!decls.atomics().contains(atomic.name))
                    return atomic;
                return null;
            }

            @Override
            public Atomic visit(UnaryOp unary) {
                return visit(unary.arg);
            }

            @Override
            public Atomic visit(BinaryOp binary) {
                Atomic lhs = visit(binary.lhs);
                return lhs != null ? lhs : visit(binary.rhs);
            }
        }

        Atomic offending = new AtomicVisitor().visit(line.proposition);
        if (offending != null)
            throw new FormatCheckException(
                line.number,
                String.format(
                    "undefined atomic proposition: '%s'",
                    offending.name));
    }

    private static class CheckNumberResult {
        Number next;
        Type type;
        enum Type { ASSUMPTION, NORMAL, DPR }
    }

    private static CheckNumberResult checkNumber(Number prev, Number line) throws FormatCheckException {
        CheckNumberResult result = new CheckNumberResult();
        if (line.levels() < prev.levels()) {
            result.next = prev.decreaseLevel();
            result.type = CheckNumberResult.Type.DPR;
        } else if (line.levels() > prev.levels()) {
            result.next = prev.increaseLevel();
            result.type = CheckNumberResult.Type.ASSUMPTION;
        } else {
            result.next = prev.next();
            result.type = CheckNumberResult.Type.NORMAL;
        }
        if (!line.equals(result.next))
            throw new FormatCheckException(line, "incorrect line number");
        return result;
    }
}
