/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cu.uci.hab.stockticker.plugin.api;

/**
 *
 * @author Roy
 */
public interface IPurchasing {

    String getUser();

    void setUser(String aUser);

    String getName();

    void setName(String mName);

    Integer getCant();

    void setCant(Integer mCant);

    Double getInversion();

    void setInversion(Double mInversion);

    Double getValue();

    void setValue(Double mValue);
}
