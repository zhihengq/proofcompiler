package proofcompiler.ast.logic;

import java.util.Objects;

/**
 * Binary operators, such as logical AND, OR, XOR, and implication.
 * Immutable.
 */
public class BinaryOp extends Operator {
    public final Type type;
    public final Proposition lhs;
    public final Proposition rhs;

    public static enum Type {
        AND {
            @Override public String toString() { return "∧"; }
            @Override int precedence() { return PRECEDENCE_AND; }
            @Override Associativity associativity() { return Associativity.LEFT; }
        },
        OR {
            @Override public String toString() { return "∨"; }
            @Override int precedence() { return PRECEDENCE_OR; }
            @Override Associativity associativity() { return Associativity.LEFT; }
        },
        XOR {
            @Override public String toString() { return "⊕"; }
            @Override int precedence() { return PRECEDENCE_XOR; }
            @Override Associativity associativity() { return Associativity.LEFT; }
        },
        IMPLIES {
            @Override public String toString() { return "→"; }
            @Override int precedence() { return PRECEDENCE_IMPLIES; }
            @Override Associativity associativity() { return Associativity.NONE; }
        },
        BICONDITIONAL {
            @Override public String toString() { return "↔"; }
            @Override int precedence() { return PRECEDENCE_IFF; }
            @Override Associativity associativity() { return Associativity.NONE; }
        };
        abstract int precedence();
        abstract Associativity associativity();
    }

    public BinaryOp(Type type, Proposition lhs, Proposition rhs) {
        this.type = type;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public <T> T accept(PropositionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int precedence() { return type.precedence(); }

    @Override
    public Associativity associativity() { return type.associativity(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BinaryOp))
            return false;
        BinaryOp that = (BinaryOp) o;
        return this.type == that.type
            && this.lhs.equals(that.lhs)
            && this.rhs.equals(that.rhs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, lhs, rhs);
    }

    @Override
    public String toString() {
        return wrap(lhs, Associativity.LEFT) + type + wrap(rhs, Associativity.RIGHT);
    }
}
