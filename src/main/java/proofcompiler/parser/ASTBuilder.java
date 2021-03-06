package proofcompiler.parser;

import java.io.InputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import static proofcompiler.Util.foldl;
import proofcompiler.ast.Proof;
import proofcompiler.ast.Declarations;
import proofcompiler.ast.Line;
import proofcompiler.ast.Number;
import proofcompiler.ast.Rule;
import proofcompiler.ast.logic.Proposition;
import proofcompiler.ast.logic.PropositionVisitor;
import proofcompiler.ast.logic.Atomic;
import proofcompiler.ast.logic.UnaryOp;
import proofcompiler.ast.logic.BinaryOp;

public class ASTBuilder extends ProofParserBaseListener {

    public Proof parse(InputStream input) throws ParserException, IOException {
        var chars = CharStreams.fromStream(input);
        var lexer = new ProofLexer(chars);
        var tokens = new CommonTokenStream(lexer);
        var parser = new ProofParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());
        ProofParser.RootContext root;
        try {
            root = parser.root();
            ParseTreeWalker.DEFAULT.walk(this, root);
        } catch (ParseCancellationException e) {
            throw new ParserException(e.getCause());
        }
        return root.value;
    }

    @Override
    public void exitProof(ProofParser.ProofContext ctx) {
        ctx.value = new Proof(false, ctx.decls().value, ctx.proofBody().value);
    }

    @Override
    public void exitEquiv(ProofParser.EquivContext ctx) {
        var vars = ctx.decls().value.atomics();
        var given = Collections.singleton(ctx.equivBody().proposition().value);
        var body = ctx.equivBody().value;
        ctx.value = new Proof(true, new Declarations(vars, given), body);
    }

    @Override
    public void exitDecls(ProofParser.DeclsContext ctx) {
        ctx.value = foldl((x, y) -> x.merge(y), new Declarations(null, null), ctx.declLine().stream()
                .map(ctxSub -> ctxSub.value)
                .collect(toUnmodifiableList())).freeze();
    }

    @Override
    public void exitDeclLet(ProofParser.DeclLetContext ctx) {
        var let = Collections.singleton(ctx.ID().getText());
        ctx.value = new Declarations(let, null).freeze();
    }

    @Override
    public void exitDeclGiven(ProofParser.DeclGivenContext ctx) {
        var given = Collections.singleton(ctx.proposition().value);
        ctx.value = new Declarations(null, given).freeze();
    }

    @Override
    public void exitProofBody(ProofParser.ProofBodyContext ctx) {
        ctx.value = ctx.proofLine().stream()
            .map(ctxSub -> ctxSub.value)
            .collect(toUnmodifiableList());
    }

    @Override
    public void exitEquivBody(ProofParser.EquivBodyContext ctx) {
        int number = 1;
        ctx.value = new ArrayList<>();
        ctx.value.add(new Line(new Number(List.of(number)), ctx.proposition().value, new Rule("given", List.of())));
        number++;
        for (var ctxSub : ctx.equivLine()) {
            var l = ctxSub.value;
            ctx.value.add(new Line(new Number(List.of(number)), l.proposition, new Rule(l.rule.name, List.of(new Number(List.of(number - 1))))));
            number++;
        }
        ctx.value = Collections.unmodifiableList(ctx.value);
    }

    @Override
    public void exitProofLine(ProofParser.ProofLineContext ctx) {
        ctx.value = new Line(
                ctx.number().value,
                ctx.proposition().value,
                ctx.ruleRef().value);
    }

    @Override
    public void exitEquivLine(ProofParser.EquivLineContext ctx) {
        ctx.value = new Line(null, ctx.proposition().value, new Rule(ctx.ruleName().value, List.of()));
    }

    @Override
    public void exitNumber(ProofParser.NumberContext ctx) {
        ctx.value = new Number(ctx.POSINT()
                .stream()
                .map(terminal -> Integer.parseInt(terminal.getText()))
                .collect(toUnmodifiableList()));
    }

    @Override
    public void exitRuleRef(ProofParser.RuleRefContext ctx) {
        ctx.value = new Rule(ctx.ruleName().value, ctx.number().stream()
                .map(ctxSub -> ctxSub.value)
                .collect(toUnmodifiableList()));
    }

    @Override
    public void exitRuleName(ProofParser.RuleNameContext ctx) {
        ctx.value = String.join(" ", ctx.ID().stream()
                .map(terminal -> terminal.getText())
                .collect(toUnmodifiableList()));
        Map<String, String> operators = Map.of(
            "\\land",           "and",
            "\\wedge",          "and",
            "∧",                "and",
            "\\lor",            "or",
            "\\vee",            "or",
            "∨",                "or",
            "\\oplus",          "xor",
            "⊕",                "xor",
            "\\leftrightarrow", "biconditional",
            "↔",                "biconditional"
            );
        if (ctx.operator() != null) {
            String op = ctx.operator().getText();
            ctx.value += " " + operators.getOrDefault(op, op);
        }
    }

    @Override
    public void exitPropositionImpl(ProofParser.PropositionImplContext ctx) {
        ctx.value = Proposition.implies(ctx.propSum(0).value, ctx.propSum(1).value);
    }

    @Override
    public void exitPropositionIff(ProofParser.PropositionIffContext ctx) {
        ctx.value = Proposition.biconditional(ctx.propSum(0).value, ctx.propSum(1).value);
    }

    @Override
    public void exitPropositionSum(ProofParser.PropositionSumContext ctx) {
        ctx.value = ctx.propSum().value;
    }

    @Override
    public void exitPropSum(ProofParser.PropSumContext ctx) {
        ctx.value = foldl(Proposition::or, null, ctx.propXor().stream()
                .map(ctxSub -> ctxSub.value)
                .collect(toUnmodifiableList()));
    }

    @Override
    public void exitPropXor(ProofParser.PropXorContext ctx) {
        ctx.value = foldl(Proposition::xor, null, ctx.propProd().stream()
                .map(ctxSub -> ctxSub.value)
                .collect(toUnmodifiableList()));
    }

    @Override
    public void exitPropProd(ProofParser.PropProdContext ctx) {
        ctx.value = foldl(Proposition::and, null, ctx.propAtom().stream()
                .map(ctxSub -> ctxSub.value)
                .collect(toUnmodifiableList()));
    }

    @Override
    public void exitPropAtomID(ProofParser.PropAtomIDContext ctx) {
        String name = ctx.ID().getText();
        if (name.equals("T"))
            ctx.value = Proposition.TRUE;
        else if (name.equals("F"))
            ctx.value = Proposition.FALSE;
        else
            ctx.value = Proposition.atomic(name);
    }

    @Override
    public void exitPropAtomNeg(ProofParser.PropAtomNegContext ctx) {
        ctx.value = Proposition.not(ctx.propAtom().value);
    }

    @Override
    public void exitPropAtomParen(ProofParser.PropAtomParenContext ctx) {
        ctx.value = ctx.proposition().value;
    }

    public static class ParserException extends Exception {
        public static final long serialVersionUID = 0;
        ParserException(Throwable cause) {
            super(cause);
        }
    }

}
