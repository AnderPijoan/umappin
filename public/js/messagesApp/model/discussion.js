var messagesApp = messagesApp || {};

(function(){

    messagesApp.Discussion = Backbone.Model.extend({

        validate: function(attrs){
            if (attrs.receivers==null || attrs.messages==null) {
                return "Error creating a Discussion";
            }
        },

        initialize: function(attrs, options) {
            /*this.on("invalid", function(model, error){
                alert(error);
            });*/
            //this.set(attrs, {validate: true});
            console.log("New Discussion created");
        }
    });
})();
