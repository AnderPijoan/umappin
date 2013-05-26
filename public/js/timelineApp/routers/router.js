/*global Backbone*/
var timelineApp = timelineApp || {};

timelineApp.Router = Backbone.SubRoute.extend({
	routes: {
		'newPublication': 'newPublication',
		'comment/:id' : 'comments',
   		'received' : 'publicationHeaders',
   		'/'	: 	'publicationHeaders'
	},
	initialize: function() {
	    this.routesHit = 0;
	    console.log('init comments router');

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
	comments: function(idComment){
		this.loadTemplateIfNeed(function(){
				publication = new timelineApp.Publication({id:idComment});
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
	newPublication: function(id){
		this.loadTemplateIfNeed(function(){
			console.log('newPublication');
			requirejs (['/assets/js/timelineApp/view/newPublication_view.js',
				'/assets/js/timelineApp/view/userSearch_view.js'], function(){
				timelineApp.newPublicationView.showPublicationForm();
			});
        });
	},
	loadTemplateIfNeed:function(callback){
		//it reloads the template only if not set
		if($('#comments_body').length ===0){
			console.log("Load Comments template");
			setTemplate ("/assets/templates/wall.html", callback);
		}else{
			callback();
		}
	}
});



