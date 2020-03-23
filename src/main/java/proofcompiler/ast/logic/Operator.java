package proofcompiler.ast.logic;

public abstract class Operator implements Proposition {
    protected static int PRECEDENCE_AND     = 1;
    protected static int PRECEDENCE_XOR     = 2;
    protected static int PRECEDENCE_OR      = 3;
    protected static int PRECEDENCE_IMPLIES = 4;
    protected static int PRECEDENCE_IFF     = 5;

    public boolean associative() { return true; }

    protected String wrap(Proposition that) {
        String format;
        if (this.precedence() < that.precedence())
            format = "(%s)";
        else if (this.precedence() > that.precedence())
            format = "%s";
        else if (associative())
            format = "%s";
        else
            format = "(%s)";
        return String.format(format, that);
    }
}
