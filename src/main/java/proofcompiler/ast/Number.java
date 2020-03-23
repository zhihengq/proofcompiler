package proofcompiler.ast;

import java.util.Collections;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.List;

/**
 * A line number. Immutable.
 */
public class Number implements Comparable<Number> {
    private final List<Integer> number;

    public static final Number ZERO = new Number(List.of(0));

    public Number(List<Integer> number) {
        this.number = Collections.unmodifiableList(number);
    }

    public int levels() {
        return number.size();
    }

    private List<Integer> nextAsList(int extra) {
        List<Integer> list = new ArrayList<>(levels() + extra);
        list.addAll(number);
        list.set(levels() - 1, list.get(levels() - 1) + 1);
        return list;
    }

    public Number next() {
        return new Number(nextAsList(0));
    }

    public Number increaseLevel() {
        List<Integer> list = nextAsList(1);
        list.add(1);
        return new Number(list);
    }

    public Number decreaseLevel() {
        return new Number(number.subList(0, levels() - 1));
    }

    public boolean hasAccess(Number that) {
        if (this.levels() < that.levels())
            return false;
        for (int i = 0; i < that.levels() - 1; i++)
            if (!this.number.get(i).equals(that.number.get(i)))
                return false;
        int last = that.levels() - 1;
        return that.number.get(last) < this.number.get(last);
    }

    @Override
    public int compareTo(Number that) {
        int thisSize = this.levels();
        int thatSize = that.levels();
        for (int i = 0; i < thisSize && i < thatSize; i++) {
            int thisNum = this.number.get(i);
            int thatNum = that.number.get(i);
            if (thisNum != thatNum)
                return Integer.compare(thisNum, thatNum);
        }
        return Integer.compare(thatSize, thisSize);
    }

    @Override
    public String toString() {
        return String.join(".", number.stream().
                map(x -> x.toString())
                .collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Number))
            return false;
        Number other = (Number) o;
        return number.equals(other.number);
    }

    @Override
    public int hashCode() {
        return number.hashCode();
    }
}
