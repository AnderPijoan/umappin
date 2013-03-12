var messagesApp = messagesApp || {};

(function(){
	var Discussion = Backbone.Model.extend({
		initialize: function() {
			console.log("New Discussion created");
		},
        defaults: {
            id: '',
            subject: '',
            unread_messages:0,
            messages:[], //array of messages (String)
            to_friends:false,
            receivers:[] //array of users, with an id, name and user_pic
        }, validate: function(attrs){
            if (attrs.receivers==null || attrs.messages==null || attrs.id==null) {
                return "Error creating a Discussion";
            }
        }
	});
})();