$(function(){

	var Statistic = Backbone.Model.extend({
		urlRoot: '/users'
	});

	var StatisticView = Backbone.View.extend({
		el: $('#statistics'),
		
		className: "#statisticContainer",

		template:$("#statistic-Template").html(),

		render:function () {
			this.model.set({"level":3, "points":65});
			alert(JSON.stringify(this.model));
			var tmpl = _.template(this.template);
			this.$el.html(tmpl(this.model.toJSON()));
			return this;
		}
	});
	
	statistic = new Statistic({});
	
    statisticView = new StatisticView({model: statistic});
    statisticView.render();
});