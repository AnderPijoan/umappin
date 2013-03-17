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


		newDiscussion: function (param) {
			var newDiscussionView = new messagesApp.NewDiscussionView();

			newDiscussionView.showDiscussionForm();
		}
	});

	messagesApp.messagesRouter = new Workspace();


    //var router = new Router;
    
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
