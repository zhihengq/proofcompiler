package proofcompiler;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import proofcompiler.parser.ASTBuilder;
import proofcompiler.ast.Proof;
import proofcompiler.ast.Declarations;
import proofcompiler.graph.Step;
import proofcompiler.codegen.Codegen;
import proofcompiler.codegen.Latex;

public class Main {

    private static final String LATEX_FORMAT =
        "\\documentclass{article}\n" +
        "\\usepackage{amsmath, amsthm}\n" +
        "\\begin{document}\n" +
        "%s" +
        "\\end{document}\n";

	public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: no input file");
            System.exit(1);
        }
        for (String file : args) {
            try {
                System.err.println(String.format("compiling '%s'...", file));
                var proof = new ASTBuilder().parse(new FileInputStream(file));
                Step conclusion = FormatChecker.check(proof);
                var lines = new Optimizer().optimize(conclusion);
                String latex = new Latex().generate(lines);
                var baseName = file.endsWith(".proof") ? file.substring(0, file.length() - ".proof".length()) : file;
                var output = new FileOutputStream(baseName + ".tex");
                output.write(String.format(LATEX_FORMAT, latex).getBytes());
                output.close();
                System.err.println(String.format("'%s' compilation complete", file));
            } catch (IOException e) {
                System.err.println("IO Error: " + e.getMessage());
            } catch (ASTBuilder.ParserException e) {
                System.err.println("Parser Error");
            } catch (FormatChecker.FormatCheckException e) {
                System.err.println(String.format("Format Error: %s", e.getMessage()));
            } catch (Step.RuleCheckException e) {
                System.err.println(String.format("Rule Error: %s", e.getMessage()));
            }
        }
    }
}
