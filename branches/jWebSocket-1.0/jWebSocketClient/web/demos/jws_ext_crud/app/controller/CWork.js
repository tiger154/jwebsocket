Ext.define('CRUD.controller.CWork', {
    extend: 'Ext.app.Controller',
    views: ['VWork'],
    ///models:['MWork'],
    stores: ['SWork'],
    init: function() {
        this.callParent(arguments);

        Ext.jwsClient.on('welcome', function() {
            Ext.ComponentQuery.query('vwork displayfield')[0].setValue('Success');
            Ext.ComponentQuery.query('vwork displayfield')[0].setFieldStyle('color:green');


        });

        Ext.jwsClient.on('close', function() {

            var myWindow = Ext.ComponentQuery.query('window')[1];

            myWindow.removeAll(true);

            myWindow.setTitle('User Loggin');

            myWindow.animate({
                to: {
                    x: 200,
                    y: 200,
                    width: 300,
                    height: 170
                }
            });

            myWindow.add({
                xtype: 'vwork'
            });


        });

        Ext.jwsClient.open();

        this.control({
            'vwork button:first': {
                'click': this.onClick
            },
        });

    },
    onClick: function(b, e, opt) {

        // console.log(Ext.ComponentQuery.query('vwork displayfield')[0].getValue());
        if (Ext.jwsClient.getConnection().fStatus == 1) {

            var form = b.up('form').getForm();

            var user = Ext.ComponentQuery.query('vwork textfield')[0].getValue();
            var password = Ext.ComponentQuery.query('vwork textfield')[1].getValue();

            form.submit({
                kind: 'jws',
                objectId: 'Crud',
                method: 'authenticate',
                app: 'jws_extjs_crud',
                args: [user, password],
                success: function(resp) {

                    var result = Ext.decode(resp.result);
                    if (result.success)
                    {

                        var myWindow = Ext.ComponentQuery.query('window')[1];
                        
                        myWindow.setTitle('jWebSocket Sencha Demo using Scripting Plugin and JavaScript Server-Side');
                        
                        Ext.ComponentQuery.query('vwork')[0].destroy();
                        myWindow.animate({
                            to: {
                                x: 30,
                                y: 130,
                                width: 660,
                                height: 350
                            }
                        })

                        
                        myWindow.add(new Ext.create('CRUD.view.VGrid'));
                       
                    }
                    else {
                        Ext.Msg.show({
                            title: 'Error',
                            msg: 'User Denied!! Contact with Admin',
                            buttons: Ext.Msg.OK,
                            fn: function() {

                                Ext.ComponentQuery.query('vwork')[0].getForm().reset();
                                Ext.ComponentQuery.query('vwork textfield')[0].focus();

                                Ext.ComponentQuery.query('vwork displayfield')[0].setValue('Success');

                            },
                            icon: Ext.MessageBox.ERROR
                        });
                    }
                }
            })

        } else {
            Ext.Msg.show({
                title: 'Error',
                msg: 'The jwebsocket server isn´t working',
                buttons: Ext.Msg.OK,
                fn: function() {
                    Ext.ComponentQuery.query('vwork')[0].getForm().reset();
                    Ext.ComponentQuery.query('vwork textfield')[0].focus();
                },
                icon: Ext.MessageBox.ERROR
            });
        }

        //Ext.Msg.alert('Error','The jwebsocket server isn´t working');
    },
    onSuccess: function(resp) {
        console.log(resp.result);
    }


});

