var timelineApp = timelineApp || {};

(function(){

	timelineApp.Comment = Backbone.Model.extend({

    	urlRoot: '/comments',

		initialize: function(attrs, options) {
			this.set(attrs);
			console.log("Comment created");
		}
	});
})();