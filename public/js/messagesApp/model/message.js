var messagesApp = messagesApp || {};

(function(){

    messagesApp.Message = Backbone.Model.extend({

    	urlRoot: '/messages',

		initialize: function(attrs, options) {
			this.set(attrs);
			console.log("message created");
		}
	});
})();