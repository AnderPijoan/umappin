var messagesApp = messagesApp || {};

(function(){
	var DiscussionHeaderList =Backbone.Collection.extend({
		model:messagesApp.DiscussionHeader,
	


	});

	// Create our global collection of **Discussions**.
	messagesApp.DiscussionHeaders = new DiscussionHeaderList();
}());

