/*global Backbone*/
var messagesApp = messagesApp || {};

(function () {

	var Workspace = Backbone.Router.extend({
		routes: {
			'newDiscussion': 'newDiscussion'
		},


		newDiscussion: function (param) {
			var newDiscussionView = new messagesApp.NewDiscussionView();

			newDiscussionView.showDiscussionForm();
		}
	});

	messagesApp.messagesRouter = new Workspace();
	Backbone.history.start();

}());
