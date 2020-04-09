package proofcompiler;

import org.junit.Test;
import org.junit.Before;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.List;
import proofcompiler.ast.Proof;
import proofcompiler.ast.Declarations;
import proofcompiler.ast.Line;
import proofcompiler.ast.Number;
import proofcompiler.ast.Rule;
import proofcompiler.ast.logic.Proposition;
import proofcompiler.parser.ASTBuilder;

@RunWith(Parameterized.class)
public class TestASTBuilder {
    private static final ClassLoader loader = TestASTBuilder.class.getClassLoader();

    @org.junit.Rule
    public ExpectedException expected = ExpectedException.none();

    @Parameter(0) public String name;
    @Parameter(1) public Class<? extends Throwable> exception;
    @Parameter(2) public InputStream input;
    @Parameter(3) public Proof proof;

    @Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        var tests = List.of(
                valid("simple", new Proof(
                        false,
                        new Declarations(
                            List.of("p", "q"),
                            List.of(
                                Proposition.atomic("p"),
                                Proposition.atomic("q")
                                )
                            ),
                        List.of(
                            new Line(new Number(List.of(1)), Proposition.atomic("p"), new Rule("Given", List.of())),
                            new Line(new Number(List.of(2)), Proposition.atomic("q"), new Rule("Given", List.of())),
                            new Line(
                                new Number(List.of(3)),
                                Proposition.and(
                                    Proposition.atomic("p"),
                                    Proposition.atomic("q")
                                    ),
                                new Rule("Intro And", List.of(new Number(List.of(1)), new Number(List.of(2))))
                                )
                            )
                        )
                    ),
                valid("comments_heavy", new Proof(
                        false,
                        new Declarations(
                            List.of("p", "q"),
                            List.of(
                                Proposition.atomic("p"),
                                Proposition.atomic("q")
                                )
                            ),
                        List.of(
                            new Line(new Number(List.of(1)), Proposition.atomic("p"), new Rule("Given", List.of())),
                            new Line(new Number(List.of(2)), Proposition.atomic("q"), new Rule("Given", List.of())),
                            new Line(
                                new Number(List.of(3)),
                                Proposition.and(
                                    Proposition.atomic("p"),
                                    Proposition.atomic("q")
                                    ),
                                new Rule("Intro And", List.of(new Number(List.of(1)), new Number(List.of(2))))
                                )
                            )
                        )
                    ),
                valid("literal", new Proof(
                        false,
                        new Declarations(
                            List.of("p"),
                            List.of(Proposition.atomic("p"))
                            ),
                        List.of(
                            new Line(new Number(List.of(1)), Proposition.atomic("p"), new Rule("Given", List.of())),
                            new Line(
                                new Number(List.of(2)),
                                Proposition.and(
                                    Proposition.atomic("p"),
                                    Proposition.TRUE
                                    ),
                                new Rule("Identity", List.of(new Number(List.of(1))))
                                ),
                            new Line(
                                new Number(List.of(3)),
                                Proposition.or(
                                    Proposition.atomic("p"),
                                    Proposition.FALSE
                                    ),
                                new Rule("Identity", List.of(new Number(List.of(1))))
                                )
                            )
                        )
                    ),
                valid("dpr", new Proof(
                        false,
                        new Declarations(
                            List.of("p", "q"),
                            List.of(
                                Proposition.atomic("p"),
                                Proposition.atomic("q")
                                )
                            ),
                        List.of(
                            new Line(new Number(List.of(1, 1)), Proposition.atomic("p"), new Rule("Assumption", List.of())),
                            new Line(new Number(List.of(1, 2)), Proposition.atomic("q"), new Rule("Given", List.of())),
                            new Line(
                                new Number(List.of(1)),
                                Proposition.implies(
                                    Proposition.atomic("p"),
                                    Proposition.atomic("q")
                                    ),
                                new Rule("Direct Proof Rule", List.of())
                                )
                            )
                        )
                    ),
                error("decls"),
                error("prop")
                );
        return tests;
    }

    private static Object[] valid(String name, Proof expected) {
        return new Object[]{
            String.format("valid: %s", name),
            null,
            loader.getResourceAsStream(String.format("valid/%s.proof", name)),
            expected,
        };
    }

    private static Object[] error(String name) {
        return new Object[]{
            String.format("parser error: %s", name),
            ASTBuilder.ParserException.class,
            loader.getResourceAsStream(String.format("parser_error/%s.proof", name)),
            null,
        };
    }

    @Test
    public void test() throws Exception {
        if (exception != null)
            expected.expect(exception);
        if (input == null)
            throw new FileNotFoundException(name);
        Proof actual = new ASTBuilder().parse(input);
        if (proof != null)
            assertEquals(proof, actual);
    }
}
