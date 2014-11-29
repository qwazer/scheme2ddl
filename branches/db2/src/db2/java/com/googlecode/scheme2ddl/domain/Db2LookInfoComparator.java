package com.googlecode.scheme2ddl.domain;

import java.util.Comparator;

/**
 * @author ar
 * @since Date: 29.11.2014
 */
public class Db2LookInfoComparator implements Comparator<Db2LookInfo> {
    public int compare(Db2LookInfo o1, Db2LookInfo o2) {
        return Long.compare(o1.getOpSequence(), o2.getOpSequence());
    }
}
