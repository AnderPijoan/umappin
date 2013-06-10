var timelineApp = timelineApp || {};

(function(){
	var UserPublicationCollection = Backbone.Collection.extend({
		url:'/userpublications',
		
		comparator: function(collection){
		    return(collection.get('timeStamp'));
		}
	});
	
	// Create our global collection of **Publications**.
	timelineApp.UserPublicationCollection = new UserPublicationCollection();
}());

