var messagesApp = messagesApp || {};

(function(){
	//this is the view controller for the all discussions (the list of discussions)

	messagesApp.ReceivedView = Backbone.View.extend({
		tagName:  'li',
		
		//set the discussion template
		template: _.template($('#discussion-head-template').html()),
		
		render: function(){
			console.log("rendering view " + $(this.el));
			console.log(this.template(this.model.toJSON()));
			//return the template with the info of the model
			$(this.el).html(this.template(this.model.toJSON()));
			return this;
		}
	});

}());

//var messagesApp.receivedDiscussions = new messagesApp.ReceivedView();