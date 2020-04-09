package proofcompiler.codegen;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toUnmodifiableList;
import proofcompiler.ast.Line;
import proofcompiler.ast.Rule;
import proofcompiler.ast.logic.Proposition;
import proofcompiler.ast.logic.Operator;
import proofcompiler.ast.logic.Atomic;
import proofcompiler.ast.logic.UnaryOp;
import proofcompiler.ast.logic.BinaryOp;
import proofcompiler.ast.logic.PropositionVisitor;
import static proofcompiler.graph.Step.ASSUMPTION;
import static proofcompiler.graph.Step.DPR;

public class Latex implements Codegen {
    private StringBuilder sb;
    private int indentLevel;
    private boolean newLine;

    private static final Map<UnaryOp.Type, String> UNARY = Map.of(
            UnaryOp.Type.NOT,               "\\neg"
            );

    private static final Map<BinaryOp.Type, String> BINARY = Map.of(
            BinaryOp.Type.AND,              "\\land",
            BinaryOp.Type.OR,               "\\lor",
            BinaryOp.Type.XOR,              "\\oplus",
            BinaryOp.Type.IMPLIES,          "\\to",
            BinaryOp.Type.BICONDITIONAL,    "\\leftrightarrow"
            );

    private static final Map<String, String> RULE = Map.of(
            "intro and",                    "Intro \\(\\land\\)",
            "intro or",                     "Intro \\(\\lor\\)",
            "elim and",                     "Elim \\(\\land\\)",
            "elim or",                      "Elim \\(\\lor\\)",
            "definition of xor",            "Definition of \\(\\oplus\\)",
            "definition of biconditional",  "Definition of \\(\\leftrightarrow\\)"
            );

    @Override
    public String generate(List<Line> lines) {
        sb = new StringBuilder();
        indentLevel = 0;
        newLine = true;
        begin("proof", null); add("\\hfill\\par"); brk();
        begin("tabular", "rll"); brk();
        for (Line line : lines) {
            if (line.rule.name.equals(ASSUMPTION)) {
                add("\\multicolumn{3}{l}{\\quad"); begin("tabular", "rll"); brk();
            } else if (line.rule.name.equals(DPR)) {
                end("tabular"); add("} \\\\"); brk();
            }
            add(String.format("%s. & \\( %s \\) & [ %s ] \\\\",
                        line.number,
                        prop(line.proposition),
                        rule(line.rule)));
            brk();
        }
        end("tabular"); add("\\par"); brk();
        end("proof"); brk();
        String result = sb.toString();
        sb = null;
        return result;
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

    public static String ruleName(Rule rule) {
        String ruleName = RULE.get(rule.name);
        if (ruleName == null)
            ruleName = capitalize(rule.name);
        return ruleName;
    }

    public static String rule(Rule rule) {
        return new Rule(ruleName(rule), rule.refs).toString();
    }

    public static String capitalize(String s) {
        return String.join(" ", Stream.of(s.split(" "))
            .map(w -> w.equals("of") ? w : (w.substring(0, 1).toUpperCase() + w.substring(1)))
            .collect(toUnmodifiableList()));
    }

    public static String prop(Proposition p) {
        class PropVisitor extends PropositionVisitor<String> {
            private String wrap(Operator parent, Proposition child) {
                String format;
                if (parent.precedence() < child.precedence())
                    format = "(%s)";
                else if (parent.precedence() > child.precedence())
                    format = "%s";
                else if (parent.associative())
                    format = "%s";
                else
                    format = "(%s)";
                return String.format(format, visit(child));
            }

            @Override
            public String visitTrue() {
                return "\\mathsf{T}";
            }

            @Override
            public String visitFalse() {
                return "\\mathsf{F}";
            }

            @Override
            public String visit(Atomic p) {
                return p.name;
            }

            @Override
            public String visit(UnaryOp p) {
                return String.format("%s %s", UNARY.get(p.type), wrap(p, p.arg));
            }

            @Override
            public String visit(BinaryOp p) {
                return String.format("%s %s %s", wrap(p, p.lhs), BINARY.get(p.type), wrap(p, p.rhs));
            }
        }
        return new PropVisitor().visit(p);
    }
}
