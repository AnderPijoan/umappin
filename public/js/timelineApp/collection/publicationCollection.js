var timelineApp = timelineApp || {};

(function(){
	var PublicationCollection =Backbone.Collection.extend({
		url:'/publications',
	});

	// Create our global collection of **Publications**.
	timelineApp.PublicationCollection = new PublicationCollection();
}());

