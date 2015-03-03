Ext.define('Ext.jws.form.action.Submit', {
    override: 'Ext.form.action.Submit',
    require: ['Ext.jws.Client'],
    
    kind:'submit',
    
    //jwebsocket properties
    ns: 'org.jwebsocket.plugins.scripting',
    type: 'callMethod',
    method: '',
    //folder name
    app:'',
    //published object
    objectId:'',
    args:'',
    constructor: function(config) {

        this.callParent(arguments);
        
        if(this.kind === 'jws')
        if(config.ns === '' && config.type === '' && config.method === '' && config.app === '' && config.objectId === '')
            Ext.Error.raise('the jwebsocket submit are not configured');
        
    },
    run: function() {
      
      if(this.kind === 'jws')
      {
          var token = {
              ns: this.ns,
              method: this.method,
              type: this.type,
              app: this.app,
              objectId: this.objectId,
              args: this.args
          };
          
          var callbacks = {};
          
          if(Ext.isFunction(this.success))
             callbacks.success = this.success;
         
          if(Ext.isFunction(this.failure))
            callbacks.failure = this.failure;
           
          Ext.jwsClient.callScriptMethod(token,callbacks,this);
          
      } else {
           this.callParent(arguments);
      }


    }

});

