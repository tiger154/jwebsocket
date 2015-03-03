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
 * @author Marcos Antonio Gonzalez Huerta
 */
public class DynaDAO implements IDAO {

	private IDatabase mDB;
	private String mTable;

	/**
	 * Constructor
	 *
	 * @param mDB
	 * @param mTable
	 */
	public DynaDAO(IDatabase mDB, String mTable) {
		this.mDB = mDB;
		this.mTable = mTable;
	}

	/**
	 * Returns the database instance that contains.
	 *
	 * @return the database instance.
	 */
	public IDatabase getDB() {
		return mDB;
	}

	@Override
	public void insert(Map<String, Object> aItem) {
		mDB.insert(mTable, aItem);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(Map<String, Object> aItem) {
		mDB.update(mTable, aItem);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Map<String, Object> aItem) {
		mDB.delete(mTable, aItem);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(IDeleteQuery aQuery) {
		mDB.delete(aQuery);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		mDB.clearTable(mTable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDeleteQuery getBasicDeleteQuery() {
		return new DynaDeleteQuery(mDB, mTable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISelectQuery getBasicSelectQuery() {
		return new DynaSelectQuery(mDB, mTable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DynaBean> fetch(ISelectQuery aQuery, Integer aOffset, Integer aLimit) {
		return mDB.fetch(aQuery, aOffset, aLimit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DynaBean> fetch(ISelectQuery aQuery) {
		return mDB.fetch(aQuery);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DynaBean fetchOne(ISelectQuery aQuery) {
		return mDB.fetchOne(aQuery);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator execute(ISelectQuery aQuery) {
		return mDB.execute(aQuery);
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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
