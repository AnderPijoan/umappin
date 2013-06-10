var statisticsApp = statisticsApp || {};

//this is the view controller for the all comments APP
(function(){
	statisticsApp.StatisticsView = Backbone.View.extend({
		el: 'div',
		
		template:$("#statistic-template").html(),
		
		initialize: function(attrs) {
            _.bind(this, "render");
            this.model.on("sync", this);
        },
		
		initAwards:function () {
			statistic = new Statistic({id:userId});
			statistic.fetch(
				{
					success: function(){
						view = new StatisticView({ model: statistic });
						$('#comments_body').html(view.addTimeAgoAndRender().el);
					},
					error: function(){
						console.log("Error getting comments from server");
					}
				}
			);
		},

		render:function () {
			this.model.set({level: 2, points:456, coins: 1200, userAwards: {name: "cool", description: "very cool", timeStamp: 3}});
			console.log(JSON.stringify(this.model));
			
			$(this.el).html(this.template(this.model.toJSON()));
			return this;
		}
	});
	
})();