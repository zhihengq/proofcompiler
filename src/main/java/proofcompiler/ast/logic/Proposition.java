package proofcompiler.ast.logic;

/**
 * Proposition AST. Immutable.
 */
public interface Proposition {
    public static final Literal TRUE = Literal.TRUE;
    public static final Literal FALSE = Literal.FALSE;

    public static enum Literal implements Proposition {
        TRUE {
            @Override
            public <T> T accept(PropositionVisitor<T> visitor) {
                return visitor.visitTrue();
            }

            @Override
            public String toString() {
                return "T";
            }
        },
        FALSE {
            @Override
            public <T> T accept(PropositionVisitor<T> visitor) {
                return visitor.visitFalse();
            }

            @Override
            public String toString() {
                return "F";
            }
        },
    }

    <T> T accept(PropositionVisitor<T> visitor);

    default int precedence() { return 0; }

    public static Atomic atomic(String name) {
        return new Atomic(name);
    }

    public static Meta meta(String name) {
        return new Meta(name);
    }

    public static UnaryOp not(Proposition arg) {
        return new UnaryOp(UnaryOp.Type.NOT, arg);
    }

    public static BinaryOp and(Proposition lhs, Proposition rhs) {
        return new BinaryOp(BinaryOp.Type.AND, lhs, rhs);
    }

    public static BinaryOp or(Proposition lhs, Proposition rhs) {
        return new BinaryOp(BinaryOp.Type.OR, lhs, rhs);
    }

    public static BinaryOp xor(Proposition lhs, Proposition rhs) {
        return new BinaryOp(BinaryOp.Type.XOR, lhs, rhs);
    }

    public static BinaryOp implies(Proposition lhs, Proposition rhs) {
        return new BinaryOp(BinaryOp.Type.IMPLIES, lhs, rhs);
    }

    public static BinaryOp biconditional(Proposition lhs, Proposition rhs) {
        return new BinaryOp(BinaryOp.Type.BICONDITIONAL, lhs, rhs);
    }
}
