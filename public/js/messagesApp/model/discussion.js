var messagesApp = messagesApp || {};

(function(){

    messagesApp.Discussion = Backbone.Model.extend({

		urlRoot:'/discussions',

		validate: function(attrs){
			if (attrs === null || attrs.id === null || attrs.subject===null ||
                  attrs.users === null || attrs.timeStamp ===null ||
                  attrs.messages ===null || attrs.lastWrote===null){
                  return "Error creating a Discussion";
          }
        },

        initialize: function(attrs, options) {
            this.on("invalid", function(model, error){
                console.log(error);
            });
            this.set(attrs, {validate: true});
		}
	});
})();
