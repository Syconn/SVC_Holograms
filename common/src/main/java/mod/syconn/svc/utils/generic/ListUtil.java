package mod.syconn.svc.utils.generic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ListUtil {

    @SafeVarargs
    public static <T> List<T> add(T element, List<T>... lists) {
        var list = join(lists);
        list.add(element);
        return list;
    }

    @SafeVarargs
    public static <T> List<T> add(T element, Collection<T>... lists) {
        var list = join(lists);
        list.add(element);
        return list;
    }

    @SafeVarargs
    public static <T> List<T> append(T element, List<T>... lists) {
        var list = join(lists);
        list.add(0, element);
        return list;
    }

    @SafeVarargs
    public static <T> List<T> remove(T element, List<T>... lists) {
        var list = join(lists);
        list.remove(element);
        return list;
    }

    @SafeVarargs
    public static <T> List<T> join(List<T>... lists) {
        return Arrays.stream(lists).flatMap(List::stream).collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T> List<T> join(Collection<T>... lists) {
        return Arrays.stream(lists).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static <T> List<T> empty() {
        return new ArrayList<>();
    }
}
