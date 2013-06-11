/*global Backbone*/
var timelineApp = timelineApp || {};

timelineApp.Router = Backbone.SubRoute.extend({
	routes: {
		'comment/:id' : 	'comments',
		'news' :			'news',
   		'/'	: 				'publicationHeaders',
   		'user/:id' :		'userWall'
	},

	comments: function(idComment){
		this.loadTemplateIfNeed(function(){
				publication = new timelineApp.Publication({id:idComment});
				console.log(idComment);
				publication.fetch(
					{
						success: function(){
							$('#new_post').hide();
							view = new timelineApp.CommentsView({ model: publication });
							$('#comments_body').html(view.addTimeAgoAndRender().el);
						},
						error: function(){
							console.log("Error getting comments from server");
						}
					}
				);
		});
	},
	news: function(){
		this.loadNewsTemplateIfNeed(function(){
			timelineApp.NewsCollection.fetch();
		});
	},
	publicationHeaders: function(){
		this.loadTemplateIfNeed(function(){
			console.log("routing received");
            // Create our global collection of **Publications**.
			$('#new_post').show();
            timelineApp.UserPublicationCollection.reset();
			timelineApp.PublicationCollection.fetch();
		});
	},
	userWall: function(idUser){
		this.loadTemplateIfNeed(function(){
			console.log("User "+idUser+" Wall");
            // Create our global collection of **Publications**.
            timelineApp.UserPublicationCollection.url = "/userpublications/" + idUser;
            timelineApp.UserPublicationCollection.reset();
			timelineApp.UserPublicationCollection.fetch();
		});
	},
	loadTemplateIfNeed:function(callback){
		//it reloads the template only if not set
		if($('#comments_body').length ===0){
			console.log("Load Publications template");
			setTemplate ("/assets/templates/wall.html", callback);
		}else{
			callback();
		}
	},
	loadNewsTemplateIfNeed:function(callback){
		//it reloads the template only if not set
		if($('#news_body').length ===0){
			console.log("Load News template");
			setTemplate ("/assets/templates/activity.html", callback);
		}else{
			callback();
		}
	},
	loadUserTemplateIfNeed:function(callback){
		//it reloads the template only if not set
		if($('#userNews_body').length ===0){
			console.log("Load User Activity template");
			setTemplate ("/assets/templates/userActivity.html", callback);
		}else{
			callback();
		}
	}
});
