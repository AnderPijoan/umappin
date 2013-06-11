/*global Backbone*/
var statisticsApp = statisticsApp || {};

statisticsApp.Router = Backbone.SubRoute.extend({
	routes: {
   		'/'	: 		'showStatistics',
	},

	showStatistics: function(){
		this.loadTemplateIfNeed(function(){
			console.log("Rendering Awards");
			statistic = new statisticsApp.Statistic({});
			statistic.fetch(
				{
					success: function(){
						var view = new statisticsApp.StatisticView({ model: statistic });
						console.log(JSON.stringify(statistic));
						view.setTemplate();
						$('#statistics_body').html(view.el);
						view.render();
					},
					error: function(){
						console.log("Error getting statistics from server");
					}
				}
			);
		});
	},
	
	loadTemplateIfNeed:function(callback){
		//it reloads the template only if not set
		if($('#statistics_body').length === 0){
			console.log("Load Awards template");
			setTemplate ("/assets/templates/awards.html", callback);
		}else{
			callback();
		}
	}
});