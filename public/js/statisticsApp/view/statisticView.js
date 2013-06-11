var statisticsApp = statisticsApp || {};
_.templateSettings.variable = "rc";

//this is the view controller for the Statistics
(function(){
	statisticsApp.StatisticView = Backbone.View.extend({
		
		setTemplate: function() {
			this.template = _.template($('#statistics-template').html());
		},
		
		render:function () {
			$(this.el).html(this.template(this.model.toJSON()));
            return this;
		}
	});
})();