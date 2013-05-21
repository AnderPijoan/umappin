var statisticsApp = statisticsApp || {};

(function(){
	var AwardCollection =Backbone.Collection.extend({
		model:Award,
	    url:'/awards'
	});

	statisticsApp.AwardCollection = new AwardCollection();
}());