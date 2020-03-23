package proofcompiler;

import java.util.function.BiFunction;

public final class Util {
    private Util() {}

    public static <T> T foldl(BiFunction<? super T, ? super T, ? extends T> f, T init, Iterable<T> list) {
        for (var x : list) {
            if (init == null)
                init = x;
            else
                init = f.apply(init, x);
        }
        return init;
    }

    /**
     * A unit type that has no instance.
     * This type has only one value, namely `null`.
     */
    public enum Unit {}

    /**
     * A binary type that has only two values, null and not null.
     */
    public enum Flag { value }
}
