/*global Backbone*/
var messagesApp = messagesApp || {};

messagesApp.Router = Backbone.SubRoute.extend({
	routes: {
		'newDiscussion': 'newDiscussion',
		'message/:id' : 'messages',
   		'received' : 'discussionHeaders',
   		'/'	: 	'discussionHeaders'
	},
	initialize: function() {
	    this.routesHit = 0;
	    console.log('init messages router');

	    //keep count of number of routes handled by your application
	    Backbone.history.on('route', function() { this.routesHit++; }, this);
	},
	//make backbone able to go previous page from JS
	//we need it from bootstrap modal dialogs
	back: function() {
	    if(this.routesHit > 1) {
	      //more than one route hit -> user did not land to current page directly
	      window.history.back();
	    } else {
	      //otherwise go to the home page. Use replaceState if available so
	      //the navigation doesn't create an extra history entry
	      this.navigate('/', {trigger:true, replace:true});
	    }
	},
	messages: function(id){
                setTemplate ("/messages", function() {
                       view = new messagesApp.MessagesView({ model: disc1 });
                       console.log("Backbone routing to message");
                      // setTemplate('messages');
                       $('#messages_body').html(view.render().el);   
                });   
	},
	discussionHeaders: function(){
	       setTemplate ("/messages", function() {
		console.log("routing received");
    		messagesApp.DiscussionHeaders.loadDiscussions();
	       });
	},
	newDiscussion: function(id){
                    setTemplate ("/messages", function() {
		var newDiscussionView = new messagesApp.NewDiscussionView();
		newDiscussionView.showDiscussionForm();
                   });
	}

});



