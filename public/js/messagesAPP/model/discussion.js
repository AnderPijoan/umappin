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
            messages:null, //array of messages (String)
            to_friends:false,
            receivers:null //array of users, with an id, name and user_pic
        } 
	});
})();