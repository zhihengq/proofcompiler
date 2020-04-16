package proofcompiler;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

import proofcompiler.ast.logic.Proposition;
import proofcompiler.ast.logic.UnaryOp;
import proofcompiler.ast.logic.BinaryOp;

public class TestProposition {
    @Test
    public void testAtomic() {
        Proposition p = Proposition.atomic("p");
        Proposition q = Proposition.atomic("q");

        assertThat(p).isEqualTo(Proposition.atomic("p"));
        assertThat(q).isEqualTo(Proposition.atomic("q"));
        assertThat(p).isNotEqualTo(q);

        assertThat(p.toString()).isEqualTo("p");
        assertThat(q.toString()).isEqualTo("q");
    }

    @Test
    public void testUnaryOp() {
        UnaryOp np = Proposition.not(Proposition.atomic("p"));

        assertThat(np).isNotEqualTo(Proposition.atomic("p"));
        assertThat(np.arg).isEqualTo(Proposition.atomic("p"));
        assertThat(np.toString()).isEqualTo("¬p");
    }

    @Test
    public void testBinaryOp() {
        BinaryOp pq = Proposition.and(
                Proposition.atomic("p"),
                Proposition.atomic("q")
            );

        assertThat(pq.lhs).isEqualTo(Proposition.atomic("p"));
        assertThat(pq.rhs).isEqualTo(Proposition.atomic("q"));
        assertThat(
                Proposition.and(
                    Proposition.atomic("p"),
                    Proposition.atomic("q")
                ).toString()
            ).isEqualTo("p∧q");
        assertThat(
                Proposition.or(
                    Proposition.atomic("p"),
                    Proposition.atomic("q")
                ).toString()
            ).isEqualTo("p∨q");
        assertThat(
                Proposition.xor(
                    Proposition.atomic("p"),
                    Proposition.atomic("q")
                ).toString()
            ).isEqualTo("p⊕q");
        assertThat(
                Proposition.implies(
                    Proposition.atomic("p"),
                    Proposition.atomic("q")
                ).toString()
            ).isEqualTo("p→q");
        assertThat(
                Proposition.biconditional(
                    Proposition.atomic("p"),
                    Proposition.atomic("q")
                ).toString()
            ).isEqualTo("p↔q");
    }

    @Test
    public void testComplex1() {
        assertThat(
                Proposition.implies(
                    Proposition.not(Proposition.and(
                            Proposition.atomic("p"),
                            Proposition.atomic("q")
                        )
                    ),
                    Proposition.or(
                        Proposition.not(Proposition.atomic("p")),
                        Proposition.not(Proposition.atomic("q"))
                    )
                ).toString()
            ).isEqualTo("¬(p∧q)→¬p∨¬q");
    }

    @Test
    public void testComplex2() {
        assertThat(
                Proposition.implies(
                    Proposition.TRUE,
                    Proposition.or(
                        Proposition.atomic("x"),
                        Proposition.FALSE
                    )
                ).toString()
            ).isEqualTo("T→x∨F");
    }
}
