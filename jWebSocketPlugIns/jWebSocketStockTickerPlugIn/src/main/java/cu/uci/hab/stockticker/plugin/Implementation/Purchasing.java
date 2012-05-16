/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.hab.stockticker.plugin.Implementation;

import cu.uci.hab.stockticker.plugin.api.IPurchasing;

/**
 *
 * @author Roy
 */
public class Purchasing implements IPurchasing {

    private String mUser;
    private String mName;
    private Integer mCant;
    private Double mInversion;
    private Double mValue;

    public Purchasing(String aUser, String aName, Integer aCant, Double aInversion, Double aValue) {
        this.mName = aName;
        this.mCant = aCant;
        this.mInversion = aInversion;
        this.mValue = aValue;
        this.mUser = aUser;
    }

    /**
     * @return the mName
     */
    @Override
    public String getName() {
        return mName;
    }

    /**
     * @param mName the mName to set
     */
    @Override
    public void setName(String mName) {
        this.mName = mName;
    }

    /**
     * @return the mCant
     */
    @Override
    public Integer getCant() {
        return mCant;
    }

    /**
     * @param mCant the mCant to set
     */
    @Override
    public void setCant(Integer mCant) {
        this.mCant = mCant;
    }

    /**
     * @return the mInversion
     */
    @Override
    public Double getInversion() {
        return mInversion;
    }

    /**
     * @param mInversion the mInversion to set
     */
    @Override
    public void setInversion(Double mInversion) {
        this.mInversion = mInversion;
    }

    /**
     * @return the mValue
     */
    @Override
    public Double getValue() {
        return mValue;
    }

    /**
     * @param mValue the mValue to set
     */
    @Override
    public void setValue(Double mValue) {
        this.mValue = mValue;
    }

    /**
     * @return the mUser
     */
    @Override
    public String getUser() {
        return mUser;
    }

    /**
     * @param mUser the mUser to set
     */
    @Override
    public void setUser(String mUser) {
        this.mUser = mUser;
    }
}
