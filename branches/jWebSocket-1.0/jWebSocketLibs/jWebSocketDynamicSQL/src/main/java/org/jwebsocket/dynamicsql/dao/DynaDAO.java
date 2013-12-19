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
import org.jwebsocket.dynamicsql.api.IQuery;
import org.jwebsocket.dynamicsql.query.DynaQuery;

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
    public IQuery getBasicQuery() {
        return new DynaQuery(mDB, mTable);
    }

    @Override
    public List<DynaBean> fetch(IQuery aQuery, Integer aOffset, Integer aLimit) {
        return mDB.fetch(aQuery, aOffset, aLimit);
    }

    @Override
    public List<DynaBean> fetch(IQuery aQuery) {
        return mDB.fetch(aQuery);
    }

    @Override
    public Iterator execute(IQuery aQuery) {
        return mDB.execute(aQuery);
    }

    @Override
    public Integer count() {
        Iterator lIterator = execute(getBasicQuery());
        Integer lCount = 0;
        while (lIterator.hasNext()) {
            lIterator.next();
            lCount++;
        }
        return lCount;
    }
}
