/*global Backbone*/
var statisticsApp = statisticsApp || {};

statisticsApp.Router = Backbone.SubRoute.extend({
	routes: {
   		'/'	: 		'showStatistics',
	},

	showStatistics: function(){
		console.log("Show Statistics");
	}
});