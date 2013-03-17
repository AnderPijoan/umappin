/*global Backbone*/
var messagesApp = messagesApp || {};

(function () {

	var Workspace = Backbone.Router.extend({
		routes: {
			'newDiscussion': 'newDiscussion',
			'message/:id' : 'messages',
       		'received' : 'discussionHeaders',
       		''	: 	'discussionHeaders'
		},
		initialize: function() {
		    this.routesHit = 0;
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
		}

	});

	messagesApp.messagesRouter = new Workspace();


	messagesApp.messagesRouter.on("route:newDiscussion", function(id){
		var newDiscussionView = new messagesApp.NewDiscussionView();
		newDiscussionView.showDiscussionForm();
	});    
    messagesApp.messagesRouter.on("route:messages", function(id){
           view = new messagesApp.MessagesView({ model: disc1 });
           console.log("Backbone routing to message");
           $('#messages_body').html(view.render().el);
    });
    messagesApp.messagesRouter.on("route:discussionHeaders", function() {
        console.log("routing received");
        messagesApp.DiscussionHeaders.loadDiscussions();
    });


	Backbone.history.start();

}());
