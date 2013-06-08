$(function(){

	_.templateSettings.variable = "rc";
	
	var Statistic = Backbone.Model.extend({
		urlRoot: '/users'
	});

	var StatisticView = Backbone.View.extend({
		el: 'div',
		
		template:$("#statistic-template").html(),
		
		initialize: function(attrs) {
            _.bind(this, "render");
            this.model.on("sync", this);
        },
		
		initAwards:function () {
			statistic = new Statistic({id:userId});
			console.log(userId);
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
	
    statisticView = new StatisticView();
    //alert("entro");
});