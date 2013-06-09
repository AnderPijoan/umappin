var timelineApp = timelineApp || {};

(function(){
	var NewsCollection =Backbone.Collection.extend({
		url:'/news',
		
		comparator: function(collection){
		    return(collection.get('timeStamp'));
		}
	});
	
	// Create our global collection of **Publications**.
	timelineApp.NewsCollection = new NewsCollection();
}());

