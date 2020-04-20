package proofcompiler.ast.logic;

import java.util.Objects;

/**
 * Unary operator, such as logical NOT. Immutable.
 */
public class UnaryOp extends Operator {
    public final Type type;
    public final Proposition arg;

    public static enum Type {
        NOT {
            @Override public String toString() { return "¬"; }
        }
    }

    public UnaryOp(Type type, Proposition arg) {
        this.type = type;
        this.arg = arg;
    }

    @Override
    public <T> T accept(PropositionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Associativity associativity() { return Associativity.UNARY; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UnaryOp))
            return false;
        UnaryOp that = (UnaryOp) o;
        return this.type == that.type && this.arg.equals(that.arg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, arg);
    }

    @Override
    public String toString() {
        return type + wrap(arg, Operator.Associativity.UNARY);
    }
}
