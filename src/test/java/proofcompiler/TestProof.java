package proofcompiler;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;
import proofcompiler.ast.Proof;
import proofcompiler.ast.Declarations;
import proofcompiler.ast.Line;
import proofcompiler.ast.Number;
import proofcompiler.ast.Rule;
import proofcompiler.ast.logic.Proposition;

public class TestProof {
    @Test
    public void testSimple() {
        String expected =
            "Let p be a proposition\n" +
            "Let q be a proposition\n" +
            "Given p\n" +
            "Given q\n" +
            "proof.\n" +
            "1. p [Given]\n" +
            "2. q [Given]\n" +
            "3. p∧q [Intro And: 1, 2]\n";
        Proof ast = new Proof(
                false,
                new Declarations(
                    List.of("p", "q"),
                    List.of(
                        Proposition.atomic("p"),
                        Proposition.atomic("q"))),
                List.of(
                    new Line(new Number(List.of(1)), Proposition.atomic("p"), new Rule("Given", List.of())),
                    new Line(new Number(List.of(2)), Proposition.atomic("q"), new Rule("Given", List.of())),
                    new Line(new Number(List.of(3)), Proposition.and(
                            Proposition.atomic("p"),
                            Proposition.atomic("q")),
                        new Rule("Intro And", List.of(new Number(List.of(1)), new Number(List.of(2)))))));

        assertThat(ast.toString()).isEqualTo(expected);
    }

    @Test
    public void testComplex() {
        String expected =
            "Let p be a proposition\n" +
            "Let q be a proposition\n" +
            "Given p\n" +
            "Given q\n" +
            "proof.\n" +
            "1.1. p [Assumption]\n" +
            "1.2. q [Given]\n" +
            "1. p→q [Direct Proof Rule]\n";
        Proof ast = new Proof(
                false,
                new Declarations(
                    List.of("p", "q"),
                    List.of(
                        Proposition.atomic("p"),
                        Proposition.atomic("q"))),
                List.of(
                    new Line(new Number(List.of(1,1)), Proposition.atomic("p"), new Rule("Assumption", List.of())),
                    new Line(new Number(List.of(1,2)), Proposition.atomic("q"), new Rule("Given", List.of())),
                    new Line(new Number(List.of(1)), Proposition.implies(
                            Proposition.atomic("p"),
                            Proposition.atomic("q")),
                        new Rule("Direct Proof Rule", List.of()))));

        assertThat(ast.toString()).isEqualTo(expected);
    }
}
