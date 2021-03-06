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
	messages: function(idMessage){
        //$("#messages-badge").show();
        var unread = JSON.parse(sessionStorage.getItem('unread-discusion'))
        if(unread){
        	if(unread.length == 0){
	        	$("#messages-badge").hide();
        		$("#message-unread").attr("href", "./#messages/");
        	}else{
		        $("#messages-badge").text(unread.length);
		        $("#message-unread").attr("href", "./#messages/message/"+unread.pop().id)
		        sessionStorage.setItem('unread-discusion',JSON.stringify(unread));
        	}
        }
		this.loadTemplateIfNeed(function(){
				discussion = new messagesApp.Discussion({id:idMessage});
				discussion.fetch(
					{
						success: function(){
							view = new messagesApp.MessagesView({ model: discussion });
							$('#messages_body').html(view.addTimeAgoAndRender().el);
						},
						error: function(){
							console.log("Error getting messages from server");
						}
					}
				);
		});
	},
	discussionHeaders: function(){
		this.loadTemplateIfNeed(function(){
			console.log("routing received");
			messagesApp.DiscussionCollection.fetch();
		});
	},
	newDiscussion: function(id){
		this.loadTemplateIfNeed(function(){
			console.log('newDiscussion');
			requirejs (['/assets/js/messagesApp/view/newDiscussion_view.js',
				'/assets/js/messagesApp/view/userSearch_view.js'], function(){
					messagesApp.newDiscussionView.showDiscussionForm();
			});
        });
	},
	loadTemplateIfNeed:function(callback){
		//it reloads the template only if not set
		if($('#messages_body').length ===0){
			console.log("Load Messages template");
			setTemplate ("/assets/templates/messages.html", callback);
		}else{
			callback();
		}
	}
});



