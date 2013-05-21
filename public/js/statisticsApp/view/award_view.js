(function($){

	var AwardView = Backbone.View.extend({
		tagName:"div",
		className:"awards_container",
		template:$("#award-template").html(),

		render:function () {
			var tmpl = _.template(this.template);

		    this.$el.html(tmpl(this.model.toJSON()));
		    return this;
		},

	});
})();

var AwardView = new awardsApp.AwardView();