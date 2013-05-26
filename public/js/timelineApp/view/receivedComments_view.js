var timelineApp = timelineApp || {};
_.templateSettings.variable = "rc";
(function(){
	//this is the view controller for the all Publications (the list of publications)

	timelineApp.ReceivedView = Backbone.View.extend({
		tagName:  'li',
		
		//set the publication template
		template: _.template($('#publication-head-template').html()),
		
		render: function(){
			//return the template with the info of the model and changed to 
			//comments_body to have this one as the comments APP body
			//$('#comments_body').html(this.template(this.model.toJSON()));
			//return the template with the info of the model
			$(this.el).html(this.template(this.model.toJSON()));
			console.log("render received Publications");
			return this;
		}
	});
}());

//var timelineApp.receivedPublications = new timelineApp.ReceivedView();