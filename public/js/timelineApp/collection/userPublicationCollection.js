var timelineApp = timelineApp || {};

(function(){
	window.UserPublicationCollection = Backbone.Collection.extend({
		url:'/userpublications',
		model: timelineApp.UserPublication,
		comparator: function(collection){
		    return(collection.get('timeStamp'));
		}
	});
    timelineApp.UserPublicationCollection = new UserPublicationCollection();
}());

