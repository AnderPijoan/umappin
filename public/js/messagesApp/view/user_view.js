var messagesApp = messagesApp || {};


messagesApp.UserItemView = Backbone.View.extend({
	events: {},
	search_render: function() {
		$(this.el).html(this.search_template(this.model.toJSON()));
		return this;
	},
	selected_render:function(){
		$(this.el).html(this.selected_template(this.model.toJSON()));
		return this;
	},
	
	initialize : function(){
		this.search_template = _.template($("#receiver-search-item-template").html());
		this.selected_template = _.template($("#receiver-selected-item-template").html())
		//this.templateSettings.variable = "receiver";
	}
});