var timelineApp = timelineApp || {};

(function(){
	var PublicationList =Backbone.Collection.extend({
		model:timelineApp.Publication
	});
	// Create our global collection of **Publications**.
	timelineApp.Publications = new PublicationList();
}());

