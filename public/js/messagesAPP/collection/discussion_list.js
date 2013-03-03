var messagesApp = messagesApp || {};

(function(){
	var DiscussionList =Backbone.Collection.extend({
		model:messagesApp.Discussion,
	


	});

	// Create our global collection of **Discussions**.
	messagesApp.Discussions = new DiscussionList();
}());

