package proofcompiler;

import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toUnmodifiableList;

import proofcompiler.parser.ASTBuilder;
import proofcompiler.graph.Step;
import proofcompiler.codegen.Latex;

@RunWith(Parameterized.class)
public class TestIntegration {
    private static final ClassLoader loader = TestIntegration.class.getClassLoader();

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Parameter(0) public String path;
    @Parameter(1) public Class<? extends Throwable> exception;

    @Parameters(name = "{0}")
    public static List<Object[]> data() throws URISyntaxException {
        return Stream.concat(Stream.concat(Stream.concat(
            baseNames("valid").map(name -> new Object[]{String.format("valid/%s", name), null}),
            baseNames("parser_error").map(name -> new Object[]{String.format("parser_error/%s", name), ASTBuilder.ParserException.class})),
            baseNames("format_error").map(name -> new Object[]{String.format("format_error/%s", name), FormatChecker.FormatCheckException.class})),
            baseNames("rule_error").map(name -> new Object[]{String.format("rule_error/%s", name), Step.RuleCheckException.class}))
            .collect(toUnmodifiableList());
    }

    private static Stream<String> baseNames(String directory) throws URISyntaxException {
        File dir= new File(loader.getResource(directory).toURI());
        return Stream.of(dir.listFiles())
            .map(f -> f.getName())
            .filter(name -> name.endsWith(".proof"))
            .map(name -> name.substring(0, name.length() - ".proof".length()));
    }

    @Test
    public void test() throws Exception {
        if (exception != null)
            expected.expect(exception);
        var ast = new ASTBuilder().parse(loader.getResourceAsStream(path + ".proof"));
        var conclusion = FormatChecker.check(ast);
        var lines = new Optimizer().optimize(conclusion);
        String latex = new Latex().generate(lines);
        var expected = new String(loader.getResourceAsStream(path + ".tex").readAllBytes());
        assertEquals(expected, latex);
    }
}
