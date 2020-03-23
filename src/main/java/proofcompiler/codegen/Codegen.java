package proofcompiler.codegen;

import java.util.List;
import proofcompiler.ast.Line;

public interface Codegen {
    String generate(List<Line> lines);
}
