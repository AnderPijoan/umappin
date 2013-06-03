$(function(){

	var Statistic = Backbone.Model.extend({
		urlRoot: '/users'
	});

	var StatisticView = Backbone.View.extend({
		el: $('#statistics'),
		
		className: "#statisticContainer",

		template:$("#statistic-Template").html(),
		
		hello:function () {
			alert("hello");
		},

		render:function () {
			var tmpl = _.template(this.template);
			this.$el.html(tmpl(this.model.toJSON()));
			return this;
		}
	});
	
	statistic = new Statistic({});
	
    statisticView = new StatisticView({model: statistic});
    statisticView.render();
});