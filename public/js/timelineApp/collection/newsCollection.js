var timelineApp = timelineApp || {};

(function(){
	window.NewsCollection =Backbone.Collection.extend({
		url:'/news',
		
		comparator: function(collection){
		    return(collection.get('timeStamp'));
		}
	});
    timelineApp.NewsCollection = new NewsCollection();
}());
