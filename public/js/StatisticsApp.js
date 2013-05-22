(function ($) {

	var Statistic = Backbone.Model.extend({

	});
	
	var Statistics = Backbone.Collection.extend({
	    model:Statistic,
	    url:'/users/5159755b494bae44d2dfbc05/statistics'
	});

	var StatisticView = Backbone.View.extend({	
		tagName:"div",
		className:"statisticContainer",

		template:$("#statistic-Template").html(),

		initialize:function () {
	        this.collection = new Statistics();
	        this.collection.fetch();
	        this.render();

	        this.collection.on("reset", this.render, this);
	    },
		
		render:function () {
			var tmpl = _.template(this.template);

			this.$el.html(tmpl(this.model.toJSON()));
			alert("hola");
			return this;
		}
	});

	var statistic = new Statistic({
		level:1,
		points:50
	});
	
    statisticView = new StatisticView({
    	model:statistic
    });

    $("#statistics").html(statisticView.render().el);
    
})(jQuery);