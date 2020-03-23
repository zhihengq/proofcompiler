package proofcompiler.graph;

import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.function.Supplier;

import static proofcompiler.Util.foldl;
import proofcompiler.ast.Number;
import proofcompiler.ast.logic.Proposition;
import proofcompiler.ast.logic.Atomic;
import proofcompiler.ast.logic.UnaryOp;
import proofcompiler.ast.logic.BinaryOp;
import proofcompiler.ast.logic.Meta;
import proofcompiler.ast.logic.PropositionVisitor;

public class Inference extends Step {
    private final String ruleName;
    private final Collection<Step> dependencies;

    public static class InferenceConstructor implements Step.StepConstructor {
        private final Collection<Proposition> antecedents;
        private final Collection<Proposition> consequents;

        InferenceConstructor(Collection<Proposition> antecedents, Collection<Proposition> consequents) {
            this.antecedents = antecedents;
            this.consequents = consequents;
        }

        @Override
        public Inference apply(Number number, Proposition proposition, String ruleName, Collection<Step> dependencies)
            throws Step.RuleCheckException {
            return new Inference(number, proposition, ruleName, dependencies, antecedents, consequents);
        }
    }

    public static InferenceConstructor rule(Collection<Proposition> antecedents, Collection<Proposition> consequents) {
        return new InferenceConstructor(antecedents, consequents);
    }

    public class RuleCheckException extends Step.RuleCheckException {
        public static final long serialVersionUID = 0;

        @Override
        public String getMessage() {
            return String.format("%s: inference rule does not match", super.getMessage());
        }
    }

    private static Proposition and(Collection<Proposition> props) {
        return foldl((l, r) -> Proposition.and(l, r), Proposition.TRUE, props);
    }

    private Inference(
            Number number, Proposition proposition, String ruleName, Collection<Step> dependencies,
            Collection<Proposition> antecedents, Collection<Proposition> consequents)
            throws Step.RuleCheckException {
        super(number, proposition, dependencies, antecedents.size());
        var deps = dependencies.stream().map(s -> s.proposition).collect(Collectors.toList());
        boolean valid = findPermutation(deps, () -> {
            for (Proposition consequent : consequents) {
                Proposition target = Proposition.implies(and(deps), proposition);
                Proposition rule = Proposition.implies(and(antecedents), consequent);
                if (unify(rule, target) != null)
                    return true;
            }
            return false;
        });
        if (!valid)
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

    private static <T, R> boolean findPermutation(List<T> range, Supplier<Boolean> f) {
        if (range.isEmpty())
            return f.get();
        for (int i = 0; i < range.size(); i++) {
            T tmp = range.get(0);
            range.set(0, range.get(i));
            range.set(i, tmp);
            if (findPermutation(range.subList(1, range.size()), f))
                return true;
            tmp = range.get(0);
            range.set(0, range.get(i));
            range.set(i, tmp);
        }
        return false;
    }

    public static Map<Meta, Proposition> unify(Proposition rule, Proposition target) {
        return new UnifierCreator().visit(rule).visit(target);
    }

    private static class MetaUnifier
            extends PropositionVisitor<Map<Meta, Proposition>> {
        private final Meta rule;
        public MetaUnifier(Meta rule) { this.rule = rule; }
        @Override public Map<Meta, Proposition> visitTrue()            { return Map.of(rule, Proposition.TRUE); }
        @Override public Map<Meta, Proposition> visitFalse()           { return Map.of(rule, Proposition.FALSE); }
        @Override public Map<Meta, Proposition> visit(Atomic   target) { return Map.of(rule, target); }
        @Override public Map<Meta, Proposition> visit(UnaryOp  target) { return Map.of(rule, target); }
        @Override public Map<Meta, Proposition> visit(BinaryOp target) { return Map.of(rule, target); }
    }

    private static class TrueUnifier
            extends PropositionVisitor<Map<Meta, Proposition>> {
        @Override public Map<Meta, Proposition> visitTrue() { return Map.of(); }
    }

    private static class FalseUnifier
            extends PropositionVisitor<Map<Meta, Proposition>> {
        @Override public Map<Meta, Proposition> visitFalse() { return Map.of(); }
    }

    private static class AtomicUnifier
            extends PropositionVisitor<Map<Meta, Proposition>> {
        private final Atomic rule;
        public AtomicUnifier(Atomic rule) { this.rule = rule; }
        @Override public Map<Meta, Proposition> visit(Atomic target) {
            return rule.equals(target) ? Map.of() : null;
        }
    }

    private static class UnaryUnifier
            extends PropositionVisitor<Map<Meta, Proposition>> {
        private final UnaryOp rule;
        public UnaryUnifier(UnaryOp rule) { this.rule = rule; }
        @Override public Map<Meta, Proposition> visit(UnaryOp target) {
            return rule.type == target.type ? unify(rule.arg, target.arg) : null;
        }
    }

    private static class BinaryUnifier
            extends PropositionVisitor<Map<Meta, Proposition>> {
        private final BinaryOp rule;
        public BinaryUnifier(BinaryOp rule) { this.rule = rule; }
        @Override public Map<Meta, Proposition> visit(BinaryOp target) {
            if (rule.type != target.type)
                return null;
            var lhs = unify(rule.lhs, target.lhs);
            if (lhs == null)
                return null;
            var rhs = unify(rule.rhs, target.rhs);
            if (rhs == null)
                return null;
            var result = new HashMap<Meta, Proposition>(lhs);
            for (var entry : rhs.entrySet()) {
                Proposition prev = result.get(entry.getKey());
                if (prev == null)
                    result.put(entry.getKey(), entry.getValue());
                else if (!prev.equals(entry.getValue()))
                    return null;
            }
            return Collections.unmodifiableMap(result);
        }
    }

    private static class UnifierCreator
            extends PropositionVisitor<PropositionVisitor<Map<Meta, Proposition>>> {
        @Override public PropositionVisitor<Map<Meta, Proposition>> visitTrue()          { return new   TrueUnifier(); }
        @Override public PropositionVisitor<Map<Meta, Proposition>> visitFalse()         { return new  FalseUnifier(); }
        @Override public PropositionVisitor<Map<Meta, Proposition>> visit(Meta     rule) { return new   MetaUnifier(rule); }
        @Override public PropositionVisitor<Map<Meta, Proposition>> visit(Atomic   rule) { return new AtomicUnifier(rule); }
        @Override public PropositionVisitor<Map<Meta, Proposition>> visit(UnaryOp  rule) { return new  UnaryUnifier(rule); }
        @Override public PropositionVisitor<Map<Meta, Proposition>> visit(BinaryOp rule) { return new BinaryUnifier(rule); }
    }
}
