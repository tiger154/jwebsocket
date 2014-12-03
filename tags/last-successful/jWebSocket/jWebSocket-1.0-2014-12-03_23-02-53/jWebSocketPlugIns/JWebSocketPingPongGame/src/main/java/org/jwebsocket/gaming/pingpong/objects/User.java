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

	private final String mUserName;
	private final String mPwdName;
	private final int mWins, mLost;

	/**
	 *
	 * @param aUsername
	 * @param aPwsName
	 * @param aWins
	 * @param aLost
	 */
	public User(String aUsername, String aPwsName, int aWins, int aLost) {
		mUserName = aUsername;
		mPwdName = aPwsName;
		mWins = aWins;
		mLost = aLost;
	}

	/**
	 *
	 * @return
	 */
	public String getUserName() {
		return mUserName;
	}

	/**
	 *
	 * @return
	 */
	public String getPwdName() {
		return mUserName;
	}

	/**
	 *
	 * @return
	 */
	public int getWins() {
		return mWins;
	}

	/**
	 *
	 * @return
	 */
	public int getLost() {
		return mLost;
	}

	@Override
	public DBObject asDocument() {
		BasicDBObject doc = new BasicDBObject();
		doc.put("user", mUserName);
		doc.put("pwd", mPwdName);
		doc.put("wins", mWins);
		doc.put("lost", mLost);
		doc.put("date", new Date());
		return doc;
	}
}