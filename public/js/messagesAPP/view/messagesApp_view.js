var messagesApp = messagesApp || {};


//this is the view controller for the all messages APP
(function(){
	messagesApp.AppView = Backbone.View.extend({
		tagName:  'ul',
		
		//set the discussion template
		template: _.template($('#discussion-list-template').html()),
		
		render: function(){
			//return the template with the info of the model
			$(this.el).html(this.template(this.model.toJSON()));
			return this;
		},
		el: 'body',
		initialize: function () {
			//if the add method of Discussions is called  "this.addOne" whill be triggered
			this.listenTo(messagesApp.Discussions, 'add', this.addOne);
			

			//messagesApp.Discussions.fetch();
		},
		addOne: function (discussion) {
		console.log(this.template(this.model.toJSON()));
			//Create ReceivedView and append it to the list
			$('#discussion_list').html(this.render().el);
			//var view = new messagesApp.ReceivedView({ model: discussion });
			//$('#discussion_list').append(view.render().el);
		}
		
	});
})();

//created here ... it will be at another place,...
var appView = new messagesApp.AppView({ model: messagesApp.Discussions });
