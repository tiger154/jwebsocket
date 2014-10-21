/*
 * File: app/view/MyViewport.js
 *
 * This file was generated by Sencha Architect version 3.1.0.
 * http://www.sencha.com/products/architect/
 *
 * This file requires use of the Ext JS 5.0.x library, under independent license.
 * License of Sencha Architect does not include license for Ext JS 5.0.x. For more
 * details see http://www.sencha.com/license or contact license@sencha.com.
 *
 * This file will be auto-generated each and everytime you save your project.
 *
 * Do NOT hand edit this file.
 */

Ext.define('MyApp.view.MyViewport', {
    extend: 'Ext.container.Viewport',
    alias: 'widget.myviewport',

    requires: [
        'MyApp.view.MyViewportViewModel',
        'Ext.panel.Panel',
        'Ext.toolbar.Toolbar',
        'Ext.button.Button',
        'Ext.toolbar.Fill',
        'Ext.form.field.TextArea'
    ],

    viewModel: {
        type: 'myviewport'
    },
    layout: 'border',

    items: [
        {
            xtype: 'panel',
            region: 'north',
            height: 150,
            title: 'Model'
        },
        {
            xtype: 'panel',
            region: 'center',
            layout: 'border',
            bodyPadding: 10,
            title: 'Wizard',
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    items: [
                        {
                            xtype: 'button',
                            text: 'Previous'
                        },
                        {
                            xtype: 'tbfill'
                        },
                        {
                            xtype: 'button',
                            text: 'Next'
                        }
                    ]
                }
            ],
            items: [
                {
                    xtype: 'panel',
                    region: 'west',
                    width: 150,
                    layout: 'accordion',
                    title: 'Elements',
                    items: [
                        {
                            xtype: 'panel',
                            title: 'Activities',
                            items: [
                                {
                                    xtype: 'panel',
                                    title: 'Connectors'
                                },
                                {
                                    xtype: 'panel',
                                    title: 'Events'
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'panel',
                    region: 'center',
                    layout: 'form',
                    title: 'Information',
                    items: [
                        {
                            xtype: 'textfield',
                            fieldLabel: 'Name'
                        },
                        {
                            xtype: 'textareafield',
                            fieldLabel: 'Description'
                        },
                        {
                            xtype: 'textfield',
                            fieldLabel: 'Other main properties'
                        }
                    ]
                }
            ]
        }
    ]

});