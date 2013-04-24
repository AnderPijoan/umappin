var messagesApp = messagesApp || {};
messagesApp.DISCUSSION_LIST_BODY ='<ul id="discussion_list"></ul>';

//this is the view controller for the all messages APP
(function(){
	messagesApp.AppView = Backbone.View.extend({
		el: 'body',
		initialize: function () {
			//triggered on sync
			this.listenTo(messagesApp.DiscussionCollection, 'sync', this.renderDiscussions);

		},
		//Discussion Headers methods
		
		
		renderDiscussions: function(){
			$('#messages_body').html('<ul id="discussion_list"> </ul>');
			messagesApp.DiscussionCollection.each(function(discussion){
			   	var view = new messagesApp.ReceivedView({ model: discussion});
			   	$('#discussion_list').append(view.render().el);
			});	
		}

		//End Discussions Headers methods
	});
})();

//created here ... it will be at another place,...
var appView = new messagesApp.AppView();
