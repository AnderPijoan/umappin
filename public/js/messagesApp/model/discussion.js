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
		/*,
		save: function(attributes, options) {
			console.log("save");
			if (attributes.privateMessage === true ) {
				options = _.defaults((options || {}), {url: '/discussions/' + this.attributes.id});
				return Backbone.Model.prototype.save.call(this, this.attributes.newMessage.message, options);
			} else {
				return Backbone.Model.prototype.save.call(this, attributes, options);
			}
		}*/
	});
})();
