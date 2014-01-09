/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.dynamicsql.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.DynaBean;
import org.jwebsocket.dynamicsql.api.IDAO;
import org.jwebsocket.dynamicsql.api.IDatabase;
import org.jwebsocket.dynamicsql.api.IDeleteQuery;
import org.jwebsocket.dynamicsql.api.ISelectQuery;
import org.jwebsocket.dynamicsql.query.DynaDeleteQuery;
import org.jwebsocket.dynamicsql.query.DynaSelectQuery;

/**
 *
 * @author markos
 */
public class DynaDAO implements IDAO {
    
    private IDatabase mDB;
    private String mTable;

    public DynaDAO(IDatabase mDB, String mTable) {
        this.mDB = mDB;
        this.mTable = mTable;
    }

    public IDatabase getDB() {
        return mDB;
    }

    @Override
    public void insert(Map<String, Object> aItem) {
        mDB.insert(mTable, aItem);
    }

    @Override
    public void update(Map<String, Object> aItem) {
        mDB.update(mTable, aItem);
    }

    @Override
    public void delete(Map<String, Object> aItem) {
        mDB.delete(mTable, aItem);
    }
    
    @Override
    public void delete(IDeleteQuery aQuery) {
        mDB.delete(aQuery);
    }

    @Override
    public void clear() {
        mDB.clearTable(mTable);
    }

    @Override
    public IDeleteQuery getBasicDeleteQuery() {
        return new DynaDeleteQuery(mDB, mTable);
    }
    
    @Override
    public ISelectQuery getBasicSelectQuery() {
        return new DynaSelectQuery(mDB, mTable);
    }

    @Override
    public List<DynaBean> fetch(ISelectQuery aQuery, Integer aOffset, Integer aLimit) {
        return mDB.fetch(aQuery, aOffset, aLimit);
    }

    @Override
    public List<DynaBean> fetch(ISelectQuery aQuery) {
        return mDB.fetch(aQuery);
    }

    @Override
    public Iterator execute(ISelectQuery aQuery) {
        return mDB.execute(aQuery);
    }

    @Override
    public Long count() {
        Iterator lIterator = execute(getBasicSelectQuery());
        Long lCount = new Long(0);
        while (lIterator.hasNext()) {
            lIterator.next();
            lCount++;
        }
        return lCount;
    }

    @Override
    public Long countResult(ISelectQuery aQuery) {
        Iterator lIterator = execute(aQuery);
        Long lCount = new Long(0);
        while (lIterator.hasNext()) {
            lIterator.next();
            lCount++;
        }
        return lCount;
    }
}
