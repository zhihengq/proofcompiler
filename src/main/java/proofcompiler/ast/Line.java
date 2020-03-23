package proofcompiler.ast;

import java.util.Objects;
import proofcompiler.ast.logic.Proposition;

/**
 * A line of proof. Immutable.
 */
public class Line {
    public final Number number;
    public final Proposition proposition;
    public final Rule rule;

    public Line(Number number, Proposition prop, Rule rule) {
        this.number = number;
        this.proposition = prop;
        this.rule = rule;
    }

    @Override
    public String toString() {
        return String.format("%s. %s [%s]", number, proposition, rule);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Line))
            return false;
        Line other = (Line) o;
        return number.equals(other.number)
            && proposition.equals(other.proposition)
            && rule.equals(other.rule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, proposition, rule);
    }
}
