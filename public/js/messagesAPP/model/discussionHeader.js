var messagesApp = messagesApp || {};

(function(){

	messagesApp.DiscussionHeader = Backbone.Model.extend({

		defaults:{
			id: '',
			subject: '',
			message_number:0,
			unread_messages:0,
			user:null
		},
		initialize: function(attrs, opts){
			/*attrs are
			id
			subject
			message_number
			unread_messages
			user{
				id
				name
				user_pic
			}
			*/

			//test if there is a valid discussion
			if (attrs != null &&attrs.id != null && attrs.subject !=null &&
				attrs.message_number != null && attrs.unread_messages !=null &&
				attrs.user !=null){
				this.set(attrs);
			}else{
				console.error("New Discussion, something is wrong")
				console.error(attrs);
			}
		}

	});

	
}());



