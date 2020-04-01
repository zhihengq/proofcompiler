package proofcompiler.codegen;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Deque;
import java.util.ArrayDeque;
import com.google.gson.Gson;
import proofcompiler.ast.Line;
import static proofcompiler.graph.Step.ASSUMPTION;
import static proofcompiler.graph.Step.DPR;

public class MathJaxJson implements Codegen {
    @Override
    public String generate(List<Line> lines) {
        Deque<List<Object>> stack = new ArrayDeque<>();
        List<Object> current = new ArrayList<>();
        for (Line line : lines) {
            if (line.rule.name.equals(ASSUMPTION)) {
                stack.addLast(current);
                current = new ArrayList<>();
            } else if (line.rule.name.equals(DPR)) {
                Map<String, Object> sub = Map.of("subproof", current);
                current = stack.removeLast();
                current.add(sub);
            }
            current.add(Map.of(
                        "number", line.number,
                        "prop", Latex.prop(line.proposition),
                        "rule", Latex.rule(line.rule)));
        }
        return new Gson().toJson(current);
    }
}
