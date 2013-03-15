
function findCustomer(){

    	
    var id = Ext.fly('textIdSearch').getValue();
    if(isNaN(id))
       log("id must be numeric");
    else
    Customer.load(id,{
        success: function(customer, response) { 
            log(response.response.responseObject.message);
            showCustomerData(customer);
        },
        failure: function(aToken,response) { 	
            log(response.error.responseObject.message);
            showCustomerData(null);
        }
    });	  
} 
        
        
function createCustomer() { 

    var nameText = Ext.fly('textName').getValue();
    var mailText = Ext.fly('textEmail').getValue();
    
    if (nameText == "" || mailText == "" ){
        log("Enter name and email");
        return;
    }
    
    var customer = new Customer({
        name: nameText,
        email: mailText
    });
    
    customer.save({
        success: function(custmSaved, response) {
            log(response.response.responseObject.message);
            getAllCustomers();
        },
        failure:function(aToken,response){
            log(response.error.responseObject.message);
        }
    });
    
        
}

function updateCustomer(){
    
    var listBox = Ext.get('listUser');
    if (listBox.dom.selectedIndex != -1){
        
        var idArray     =   listBox.dom.options[2].value.split(":");
        var nameArray   =   listBox.dom.options[0].value.split(":");
        var emailArray  =   listBox.dom.options[1].value.split(":");
        
        Customer.load(idArray[1].trim(),{
            
            success: function(customer, response) { 

                customer.set('name', nameArray[1 ]);
                customer.set('email',emailArray[1]);
                
                customer.save({
                    success:function(cust,response){
                          log(response.response.responseObject.message);
                    },
                    failure:function(aToken,response){
                          log(response.error.responseObject.message);
                    }
                });
                
                showCustomerData(customer);
                getAllCustomers();
            },
            failure: function(aToken,response) { 	
                log(response.error.responseObject.message);
                showCustomerData(null);
            }
        });	  
        
    
    }else
        log("Enter the Customer id an search, after that you can change the name or email with justo dbClick on it ");
    
}


function getAllCustomers()
{
    
    Ext.jws.send("jws.ext.demo", "getAllUsers",{},{
        success: function(aToken){
               showAllCustomers(aToken.data);
        },
        failure: function(aToken){
            log(aToken.message);
        }
    });
}


function delCustomer()
{
    var listBox = Ext.get('listUsers');
    if (listBox.dom.selectedIndex != -1){
        
        var id = listBox.dom.options[listBox.dom.selectedIndex].value;
        
       Customer.load(id,{
            success: function(customer) { 
                
                customer.destroy({
                    success:function(cutomer, response){
                        
                        log(response.response.responseObject.message);
                        getAllCustomers();
                    }
                });
            },
            failure: function(aToken,response) { 	
                log(response.error.responseObject.message);
            }
        });	 
        
    }else{
        log("you shuld select one item to delete");
    }
        
}

function showCustomerData(customer)
{
     var dh = Ext.core.DomHelper;
     var list = Ext.DomQuery.select("#listUser option");
     if (list.length > 0)
        Ext.each(list,function(item){  
            var el = Ext.get(item);
            el.dom.parentNode.removeChild(el.dom);
        });
     
     if (customer != null){
        dh.append("listUser",{tag:'option',html:'name  : '+customer.get('name')});
        dh.append("listUser",{tag:'option',html:'email : '+customer.get('email')});
        dh.append("listUser",{tag:'option',html:'id    : '+customer.get('id')});
     }
}



function showAllCustomers(customer)
{
       
    var dh = Ext.core.DomHelper;
    var list = Ext.DomQuery.select("#listUsers option");
    
    if (list.length > 0)
        Ext.each(list,function(item){  
            var el = Ext.get(item);
            el.dom.parentNode.removeChild(el.dom);
        });
    
    Ext.each(customer,function(item){  
        dh.append("listUsers",{tag:'option',html:'name: '+item.name+'     id:'+item.id,value:item.id});
    });   
    
}


function log(text)
{
    Ext.fly('console').update(text);
}
      


Ext.onReady(function(){
    
    
    Ext.jws.open();

    Ext.jws.on('open',function(){
        initDemo();
    });
    
    Ext.jws.on('close',function(){
        alert("you are disconnect");
    });

});


function initDemo(){


      /*
     * creating jWebsocket proxy settings
     *
     * ns properties is not optional: thi is the namespace than
     *
     * if api is not specified in the config object,
     * the default is taken as below;
     *
     */

    var proxy_cfg = {
        ns:'jws.ext.demo',
        api:{
            create : 'create',
            read   : 'read',
            update : 'update',
            destroy: 'destroy'
        },
        reader: {
            root: 'data'
        }

    };

    var jWSproxy  = new Ext.jws.data.proxy(proxy_cfg);

    Ext.regModel('Customer', {
        fields: ['id','name', 'email'],
        proxy: jWSproxy
    });


    Ext.fly('btnSearch').on("click",findCustomer);
    Ext.fly('btnSend').on("click",createCustomer);
    Ext.fly('btnDel').on("click",delCustomer);
    Ext.fly('btnShow').on("click",getAllCustomers);
    Ext.fly('btnUpdate').on("click",updateCustomer);

    Ext.fly('listUser').on("dblclick",function(event, el){

       var el    =  Ext.get(el);
       var value = el.getValue();
       if (el.dom.selectedIndex == -1)
           return;

       var arrayValue = value.split(':');

       if (arrayValue[0].trim() == "id"){
           log("you can not change id field ");
           return;
       }

       var a = prompt("change the value", arrayValue[1]);
       a = a ? a : arrayValue[1];
       el.update( arrayValue[0] +" : "+a);

    });
    



}