package proofcompiler;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;
import static java.util.stream.Collectors.toUnmodifiableList;

import proofcompiler.ast.Line;
import proofcompiler.ast.Number;
import proofcompiler.ast.Rule;
import proofcompiler.graph.Step;

public class Optimizer {
    private PriorityQueue<Vertex> next= new PriorityQueue<>();
    private Map<Step, Vertex> vertices = new HashMap<>();

    public List<Line> optimize(Step conclusion) {
        toVertex(conclusion);
        Map<Step, Number> numbers = new HashMap<>();
        List<Line> lines = new ArrayList<>();
        Number number = Number.ZERO;
        while (!next.isEmpty()) {
            Vertex v = next.remove();
            number = v.step.number(number);
            numbers.put(v.step, number);
            Collection<Number> deps;
            if (v.step.ruleName().equals(Step.DPR))
                deps = List.of();
            else
                deps = v.step.dependencies().stream()
                    .map(s -> numbers.get(s))
                    .collect(toUnmodifiableList());
            Rule rule = new Rule(v.step.ruleName(), deps);
            lines.add(new Line(number, v.step.proposition, rule));
            v.release();
        }
        next.clear();
        vertices.clear();
        return lines;
    }

    private Vertex toVertex(Step step) {
        Vertex v = vertices.get(step);
        if (v == null) {
            Collection<Vertex> deps = step.dependencies().stream()
                .map(d -> toVertex(d))
                .collect(toUnmodifiableList());
            v = new Vertex(step, deps);
            vertices.put(step, v);
        }
        return v;
    }

    private class Vertex implements Comparable<Vertex> {
        public final Step step;
        private final Collection<Vertex> unblocks = new ArrayList<>();
        private int blocking;

        Vertex(Step step, Collection<Vertex> dependencies) {
            this.step = step;
            this.blocking = dependencies.size();
            dependencies.forEach(d -> d.unblocks.add(this));
            if (blocking == 0)
                Optimizer.this.next.add(this);
        }

        private void unblock() {
            if (--blocking == 0)
                Optimizer.this.next.add(this);
        }

        void release() {
            unblocks.forEach(v -> v.unblock());
        }

        @Override
        public int compareTo(Vertex that) {
            return this.step.compareTo(that.step);
        }
    }
}
