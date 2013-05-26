var timelineApp = timelineApp || {};

timelineApp.UserSearchView = Backbone.View.extend({
	el: '#receivers_block',
	events: {
		"keyup #form_receivers" : "search",
		"click .receiver_item"	: "add_receiver",
		"click .remove_selected_item"	: "remove_receiver", 
	},
	render: function(data) {
		//$(this.el).html(this.template);
		return this;
	},
	render_search_list : function(followed){
		$("#search_result_list").html("");
 
		followed.each(function(f){
			var view = new timelineApp.UserItemView({
				model: f,
			});
			$("#search_result_list").append(view.search_render().el);
		});
		return this;
	},
	render_selected_list : function(){
		$("#receivers_list").html("");
		var selected = this.collection.getSelected();
 
		selected.each(function(f){
			var view = new timelineApp.UserItemView({
				model: f,
			});
			$("#receivers_list").append(view.selected_render().el);
		});
		return this;
	},
	initialize : function(){
		//this.template = _.template($("#user_search_result").html());
		this.collection.bind("reset", this.render, this);
		console.log("search view init");

	},
	search: function(e){
		var letters = $("#form_receivers").val();
		this.render_search_list(this.collection.search(letters));
	},

	add_receiver: function(e){
		var id = $(e.currentTarget).data("id");
        var item = this.collection.get(id);
        item.set({'selected' : true});
        var view = new timelineApp.UserItemView({
				model: item,
		});
        this.render_selected_list();

		this.search();
	},
	remove_receiver: function(e){
		var id = $(e.currentTarget).data("id");
		console.log(id);
        var item = this.collection.get(id);
        item.set({'selected' : false});
        this.render_selected_list();

        this.search();
	}
});
 
// we would instantiate this view with our collection
// print our template
//$("#user_search_result").prepend(this.listContainerView.render().el);

