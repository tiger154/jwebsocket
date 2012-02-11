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
			add(new CustomerDef(count, "Alexander", "arojash@uci.cu", 24));
			add(new CustomerDef(count, "Alexander", "a.schulze@jwebsocket.org", 40));
			add(new CustomerDef(count, "Anuradha", "galianuradha@gmail.com", 25));
			add(new CustomerDef(count, "Armando", "alsimon@uci.cu", 26));
			add(new CustomerDef(count, "Carlos", "carlosfeyt@hab.uci.cu", 25));
			add(new CustomerDef(count, "Carlos", "ckcespedes@uci.cu", 25));
			add(new CustomerDef(count, "Daimi", "dmederos@hab.uci.cu", 24));
			add(new CustomerDef(count, "Dariel", "dnoa@uci.cu", 25));
			add(new CustomerDef(count, "Eduardo", "ebourzach@uci.cu", 26));

			add(new CustomerDef(count, "Johannes", "johannes.schoenborn@gmail.com", 25));
			add(new CustomerDef(count, "Johannes", "j.smutny@gmail.com", 25));
			add(new CustomerDef(count, "Lester", "lzaila@hab.uci.cu", 25));
			add(new CustomerDef(count, "Lisdey", "lperez@hab.uci.cu", 23));
			add(new CustomerDef(count, "Merly", "mlopez@hab.uci.cu", 24));
			add(new CustomerDef(count, "Marcos ", "magonzalez@hab.uci.cu", 25));
			add(new CustomerDef(count, "Marta ", "mrodriguez@hab.uci.cu", 25));
			add(new CustomerDef(count, "Mayra", "memaranon@hab.uci.cu", 25));
			add(new CustomerDef(count, "Orlando", "omiranda@uci.cu", 25));
			add(new CustomerDef(count, "Osvaldo", "oaguilar@uci.cu", 25));

			add(new CustomerDef(count, "Prashant", "prashantkhanal@gmail.com", 25));
			add(new CustomerDef(count, "Puran", "mailtopuran@gmail.com", 25));
			add(new CustomerDef(count, "Quentin", "quentin.ambard@gmail.com", 25));
			add(new CustomerDef(count, "Rebecca", "r.schulze@jwebsocket.org", 23));
			add(new CustomerDef(count, "Rolando", "rbetancourt@hab.uci.cu", 24));
			add(new CustomerDef(count, "Rolando", "rsantamaria@hab.uci.cu", 26));
			add(new CustomerDef(count, "Roylandi", "rgpujol@hab.uci.cu", 25));
			add(new CustomerDef(count, "Unni", "unnivm@gmail.com", 25));
			add(new CustomerDef(count, "Victor", "vbarzana@uci.cu", 25));
			add(new CustomerDef(count, "Yamila", "yvigil@hab.uci.cu", 27));

			add(new CustomerDef(count, "Yasmany", "ynbosh@hab.uci.cu", 24));			
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
