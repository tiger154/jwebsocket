/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.gaming.pingpong.objects;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.jwebsocket.gaming.pingpong.api.IMongoDocument;
import java.util.Date;

/**
 *
 * @author armando
 */
public class User implements IMongoDocument {

	private String mUserName;
	private String mPwdName;
	private int mWins, mLost, mRanking;

	/**
	 *
	 * @param username
	 * @param pwsname
	 * @param aWins
	 * @param aLost
	 */
	public User(String username, String pwsname, int aWins, int aLost) {
		this.mUserName = username;
		this.mPwdName = pwsname;
		this.mWins = aWins;
		this.mLost = aLost;
	}

	/**
	 *
	 * @return
	 */
	public String getUserName() {
		return this.mUserName;
	}

	/**
	 *
	 * @return
	 */
	public String getPwdName() {
		return this.mUserName;
	}

	/**
	 *
	 * @return
	 */
	public int getWins() {
		return this.mWins;
	}

	/**
	 *
	 * @return
	 */
	public int getLost() {
		return this.mLost;
	}

	@Override
	public DBObject asDocument() {
		//throw new UnsupportedOperationException("Not supported yet.");
		BasicDBObject doc = new BasicDBObject();
		doc.put("user", mUserName);
		doc.put("pwd", mPwdName);
		doc.put("wins", mWins);
		doc.put("lost", mLost);
		doc.put("date", new Date());
		return doc;
	}
}