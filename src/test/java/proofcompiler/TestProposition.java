package proofcompiler;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import proofcompiler.ast.logic.Proposition;
import proofcompiler.ast.logic.UnaryOp;
import proofcompiler.ast.logic.BinaryOp;

public class TestProposition {
    @Test
    public void testAtomic() {
        Proposition p = Proposition.atomic("p");
        Proposition q = Proposition.atomic("q");
        assertEquals(p, Proposition.atomic("p"));
        assertEquals(q, Proposition.atomic("q"));
        assertNotEquals(p, q);
        assertEquals("p", p.toString());
        assertEquals("q", q.toString());
    }

    @Test
    public void testUnaryOp() {
        UnaryOp np = Proposition.not(Proposition.atomic("p"));
        assertNotEquals(np, Proposition.atomic("p"));
        assertEquals(np.arg, Proposition.atomic("p"));
        assertEquals("¬p", np.toString());
    }

    @Test
    public void testBinaryOp() {
        BinaryOp pq = Proposition.and(
                Proposition.atomic("p"),
                Proposition.atomic("q")
            );
        assertEquals(pq.lhs, Proposition.atomic("p"));
        assertEquals(pq.rhs, Proposition.atomic("q"));
        assertEquals(
                "p∧q",
                Proposition.and(
                    Proposition.atomic("p"),
                    Proposition.atomic("q")
                ).toString()
            );
        assertEquals(
                "p∨q",
                Proposition.or(
                    Proposition.atomic("p"),
                    Proposition.atomic("q")
                ).toString()
            );
        assertEquals(
                "p⊕q",
                Proposition.xor(
                    Proposition.atomic("p"),
                    Proposition.atomic("q")
                ).toString()
            );
        assertEquals(
                "p→q",
                Proposition.implies(
                    Proposition.atomic("p"),
                    Proposition.atomic("q")
                ).toString()
            );
        assertEquals(
                "p↔q",
                Proposition.biconditional(
                    Proposition.atomic("p"),
                    Proposition.atomic("q")
                ).toString()
            );
    }

    @Test
    public void testComplex1() {
        assertEquals(
                "¬(p∧q)→¬p∨¬q",
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
            );
    }

    @Test
    public void testComplex2() {
        assertEquals(
                "T→x∨F",
                Proposition.implies(
                    Proposition.TRUE,
                    Proposition.or(
                        Proposition.atomic("x"),
                        Proposition.FALSE
                    )
                ).toString()
            );
    }
}
