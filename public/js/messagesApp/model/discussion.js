var messagesApp = messagesApp || {};

(function(){

    messagesApp.Discussion = Backbone.Model.extend({

    

        initialize: function(attrs, options) {
            /*this.on("invalid", function(model, error){
                alert(error);
            });*/
            //this.set(attrs, {validate: true});
            console.log("New Discussion created");
        }
    });
})();
