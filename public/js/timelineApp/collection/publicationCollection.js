var timelineApp = timelineApp || {};

(function(){
	window.PublicationCollection =Backbone.Collection.extend({
		url:'/publications',
		
		comparator: function(collection){
		    return(collection.get('timeStamp'));
		}
	});
    timelineApp.PublicationCollection = new PublicationCollection();
}());

