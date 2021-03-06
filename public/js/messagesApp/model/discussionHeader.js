var messagesApp = messagesApp || {};

(function(){

	messagesApp.DiscussionHeader = Backbone.Model.extend({

		validate: function(attrs){
          if (attrs == null || attrs.id == null || attrs.subject ==null ||
                  attrs.message_number == null || attrs.unread_messages ==null ||
                  attrs.user ==null){

                  return "Error creating a DiscussionHeader";
          }
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
            //add the on invalid event
            this.on("invalid", function(model, error){
                alert(error);
             });
            //validate the attributes and if correct set it to model
			this.set(attrs, {validate: true});
			console.log("DiscussionHeader created");
		}
	});	
}());



