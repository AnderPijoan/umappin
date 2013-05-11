var messagesApp = messagesApp || {};

(function(){
	var DiscussionCollection =Backbone.Collection.extend({
		//model:messagesApp.Discussion,

		url:'/discussions',
	});

	// Create our global collection of **Discussions**.
	messagesApp.DiscussionCollection = new DiscussionCollection();
}());

