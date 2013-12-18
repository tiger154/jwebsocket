package org.jwebsocket.dynamicsql.query;

/**
 *
 * @author markos
 */
public enum Ordering {

    ASCENDING(" ASC"),
    DESCENDING(" DESC");
    private final String mDirStr;

    private Ordering(String aDirStr) {
        mDirStr = aDirStr;
    }

    @Override
    public String toString() {
        return mDirStr;
    }
}
