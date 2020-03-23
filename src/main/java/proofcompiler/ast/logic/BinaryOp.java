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
        },
        OR {
            @Override public String toString() { return "∨"; }
            @Override int precedence() { return PRECEDENCE_OR; }
        },
        XOR {
            @Override public String toString() { return "⊕"; }
            @Override int precedence() { return PRECEDENCE_XOR; }
        },
        IMPLIES {
            @Override public String toString() { return "→"; }
            @Override int precedence() { return PRECEDENCE_IMPLIES; }
        },
        BICONDITIONAL {
            @Override public String toString() { return "↔"; }
            @Override int precedence() { return PRECEDENCE_IFF; }
        };
        abstract int precedence();
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
    public int precedence() {
        return type.precedence();
    }

    @Override
    public boolean associative() {
        return type != Type.IMPLIES && type != Type.BICONDITIONAL;
    }

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
        return wrap(lhs) + type + wrap(rhs);
    }
}
