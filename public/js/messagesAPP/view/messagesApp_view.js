var messagesApp = messagesApp || {};


//this is the view controller for the all messages APP
(function(){
	messagesApp.AppView = Backbone.View.extend({
		el: 'body',
		initialize: function () {
			
			//if the add method of Discussions is called  "this.addOne" whill be triggered
			this.listenTo(messagesApp.Discussions, 'add', this.addOne);
			

			//messagesApp.Discussions.fetch();
		},
		addOne: function (discussion) {
			//Create ReceivedView and append it to the list
			var view = new messagesApp.ReceivedView({ model: discussion });
			$('#discussion_list').append(view.render().el);
		},
	});
})();

//created here ... it will be at another place,...
var appView = new messagesApp.AppView();
