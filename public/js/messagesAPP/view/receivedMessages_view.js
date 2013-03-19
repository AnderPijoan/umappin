var messagesApp = messagesApp || {};

(function(){
	//this is the view controller for the all discussions (the list of discussions)

	messagesApp.ReceivedView = Backbone.View.extend({
		tagName:  'li',
		
		//set the discussion template
		template: _.template($('#discussion-head-template').html()),
		
		render: function(){
			//return the template with the info of the model and changed to 
			//messages_body to have this one as the messages APP body
			$(this.el).html(this.template(this.model.toJSON()));
			console.log("render received Discussions");
			return this;
		}
	});
}());

//var messagesApp.receivedDiscussions = new messagesApp.ReceivedView();