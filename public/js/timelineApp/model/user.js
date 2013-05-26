var timelineApp = timelineApp || {};

(function(){

	timelineApp.User = Backbone.Model.extend({
    	initialize: function(attrs, options) {
			this.set(attrs);
			this.attributes.selected = false;
	}});
    
})();
