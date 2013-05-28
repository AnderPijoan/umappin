var timelineApp = timelineApp || {};


//this is the view controller for the all messages APP
(function(){
	timelineApp.NewPublicationView = Backbone.View.extend({
		
		events: {
			"click #send_new_publication_button":   "create",
		},

		create: function (publication) {
			alert("create cliked");
			//Create ReceivedView and append it to the list
			var subject = this.$el.find('#form_subject').val();
			var comment = this.$el.find('#form_comment').val();

			var newPublication={
				"subject": subject,
				"messages":{
					"message":comment,
				},
			};
			
			var a = new timelineApp.Publication(newPublication);
			timelineApp.PublicationCollection.add(a);
			var that = this;
			a.save({}, {  
			    success:function(){
			    	//clear form
					that.$el.find('#form_subject').val('');
					that.$el.find('#form_comment').val('');	   
				}
			});	
			
		}
	});
})();

timelineApp.newPublicationView = new timelineApp.NewPublicationView();
alert("inicializo newPublication");



