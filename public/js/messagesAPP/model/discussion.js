var messagesApp = messagesApp || {};

(function(){
	var Discussion = Backbone.Model.extend({
		initialize: function(attrs, options) {
			console.log("New Discussion created");
			this.on("invalid", function(model, error){
			    alert(error);
			});
			this.set(attrs, {validate: true});
		},
        defaults: {
            id: '',
            subject: '',
            unread_messages:0,
            messages:null, //array of messages (String)
            to_friends:false,
            receivers:null //array of users, with an id, name and user_pic
        }, validate: function(attrs){
            if (attrs.receivers==null || attrs.messages==null || attrs.id==null) {
                return "Error creating a Discussion";
            }
        }
	});
})();