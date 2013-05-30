/*global Backbone*/
var timelineApp = timelineApp || {};

timelineApp.Router = Backbone.SubRoute.extend({
	routes: {
		'comment/:id' : 	'comments',
   		'/'	: 				'publicationHeaders'
	},

	comments: function(idComment){
		this.loadTemplateIfNeed(function(){
				publication = new timelineApp.Publication({id:idComment});
				console.log(idComment);
				publication.fetch(
					{
						success: function(){
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
	publicationHeaders: function(){
		this.loadTemplateIfNeed(function(){
			console.log("routing received");
			timelineApp.PublicationCollection.fetch();
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
	}
});



