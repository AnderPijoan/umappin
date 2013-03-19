var messagesApp = messagesApp || {};

(function(){
	var DiscussionHeaderList =Backbone.Collection.extend({
		model:messagesApp.DiscussionHeader,
		loadDiscussions:function(){
			//here we load all discussions from the server side,... now from json File
			this.reset();
			console.log("reset Received Discussions");
			$.getJSON("/assets/js/messagesAPP/test/discussion_header.json", function(data) {
			    $.each(data, function(key, val) {
				    messagesApp.DiscussionHeaders.add(val);
				});
			});
		}
	


	});

	// Create our global collection of **Discussions**.
	messagesApp.DiscussionHeaders = new DiscussionHeaderList();
}());

