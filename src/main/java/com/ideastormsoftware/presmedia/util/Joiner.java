package com.ideastormsoftware.presmedia.util;

import java.util.Collection;
import java.util.Iterator;

public class Joiner {

    private final String separator;

    private Joiner(String separator) {
        this.separator = separator;
    }

    public static Joiner on(String separator) {
        return new Joiner(separator);
    }

    public String join(Collection stuffToJoin) {
        StringBuilder sb = new StringBuilder();
        if (stuffToJoin != null) {
            Iterator iterator = stuffToJoin.iterator();
            if (iterator.hasNext()) {
                sb.append(iterator.next());
            }
            while (iterator.hasNext()) {
                sb.append(separator).append(iterator.next());
            }
        }
        return sb.toString();
    }
}
