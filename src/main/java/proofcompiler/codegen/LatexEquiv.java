package proofcompiler.codegen;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toUnmodifiableList;
import proofcompiler.ast.Line;
import proofcompiler.ast.Rule;
import static proofcompiler.graph.Step.GIVEN;
import static proofcompiler.graph.Step.ASSUMPTION;
import static proofcompiler.graph.Step.DPR;

public class LatexEquiv implements Codegen {
    private StringBuilder sb;
    private int indentLevel;
    private boolean newLine;

    @Override
    public String generate(List<Line> lines) {
        sb = new StringBuilder();
        indentLevel = 0;
        newLine = true;
        begin("tabular", "r@{\\hskip0.5\\tabcolsep}ll"); brk();
        for (Line line : lines) {
            assert !line.rule.name.equals(ASSUMPTION);
            assert !line.rule.name.equals(DPR);
            if (line.rule.name.equals(GIVEN))
                add(String.format("\\( %s \\)", Latex.prop(line.proposition)));
            else
                add(String.format("& \\( \\equiv %s \\) & [ %s ] \\\\",
                            Latex.prop(line.proposition),
                            rule(line.rule)));
            brk();
        }
        end("tabular"); add("\\par"); brk();
        String result = sb.toString();
        sb = null;
        return result;
    }

    public static String rule(Rule r) {
        return Latex.capitalize(r.name);
    }

    private void add(String s) {
        if (newLine)
            sb.append("    ".repeat(indentLevel));
        else
            sb.append(" ");
        sb.append(s);
        newLine = false;
    }

    private void brk() {
        sb.append("\n");
        newLine = true;
    }

    private void begin(String env, String arg) {
        String param = arg == null ? "" : String.format("{%s}", arg);
        add(String.format("\\begin{%s}%s", env, param));
        indentLevel++;
    }

    private void end(String env) {
        indentLevel--;
        add(String.format("\\end{%s}", env));
    }
}
