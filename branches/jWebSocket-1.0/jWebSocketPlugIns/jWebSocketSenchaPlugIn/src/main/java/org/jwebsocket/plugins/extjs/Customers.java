//	---------------------------------------------------------------------------
//	jWebSocket - ExtJS Plugin
//	Copyright (c) 2011 Innotrade GmbH, jWebSocket.org, Alexander Schulze,
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.extjs;

import java.util.LinkedList;
import java.util.List;



/**
 *
 * @author Osvaldo Aguilar Lauzurique, Alexander Rojas Hernandez
 */
public class Customers {

    private LinkedList<CustomerDef> customers;
    private Integer count;

    public Customers() {
        count = 0;
        customers = new LinkedList<CustomerDef>();
        try{
            
            add(new CustomerDef(count, "Alexander", "arojash@uci.cu", 25));
            add(new CustomerDef(count, "Osvaldo", "oaguilar@hab.uci.cu", 25));
            add(new CustomerDef(count, "Victor", "vbarzana@hab.uci.cu", 24));
            add(new CustomerDef(count, "Rolando", "rzantamaria@hab.uci.cu", 26));
            add(new CustomerDef(count, "Carlos", "ckcespedez@hab.uci.cu", 25));

        }catch(Exception ex){
        
        }
        
    }

    public void add(CustomerDef customer) throws Exception {
        for (CustomerDef u : customers) {
            if (u.getEmail().equals(customer.getEmail())) {
                throw new Exception("customers duplicated");
            }
        }
        customers.add(customer);
        count++;
    }

    public LinkedList<CustomerDef> getCustomers() {
        return customers;
    }

    public List<CustomerDef> getSubList(int start, int limit) {


        if (limit >  customers.size())
        {
            limit -= limit - customers.size();
        }
        
        return customers.subList(start, limit);
         
    }

    public Integer getSize(){
        return customers.size();
    }


    public CustomerDef getCustomer(Integer id) {
        CustomerDef customer = null;
        for (CustomerDef u : customers) {
            if (u.getId().equals(id)) {
                return u;
            }
        }
        return customer;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getCount() {
        return count;
    }

    public boolean findCustomer(Integer id) {
        for (CustomerDef u : customers) {
            if (u.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    public boolean deleteCustomer(Integer id) {
        for (CustomerDef u : customers) {
            if (u.getId().equals(id)) {
                customers.remove(u);
                return true;
            }
        }

        return false;
    }
}
