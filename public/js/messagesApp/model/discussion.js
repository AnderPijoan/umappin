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
                alert(error);
            });
            this.set(attrs, {validate: true});
            /*this.messages=[
				{
					"id":"51768985860e937b2297039f",
					"user":{
						"id":"516d0aa5a1a8937b49f533d9",
						"name":"Jon",
						"photo":"http://paginaspersonales.deusto.es/dipina/images/photo-txikia2.jpg"
					},
					"message":"''",
					"timeStamp":1366722949000
				}
		];*/
		},
		parse: function(response, options) {
			//this.set("messages",response.messages);
			this.set(response);
		}
	});
})();
