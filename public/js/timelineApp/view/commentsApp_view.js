var timelineApp = timelineApp || {};

//this is the view controller for the all comments APP
(function(){
	timelineApp.AppView = Backbone.View.extend({
		el: 'body',
		
		events: {
            "click #send_new_publication_button":   "create"
        },
		
		initialize: function () {
			//triggered on sync
			this.listenTo(timelineApp.PublicationCollection, 'sync', this.renderPublications);

		},
		//Publication Headers methods
		
		
		renderPublications: function(){
			$('#comments_body').html('<ul id="publication_list"> </ul>');
			timelineApp.PublicationCollection.each(function(publication){
			   	var view = new timelineApp.ReceivedView({ model: publication});
			   	$('#publication_list').prepend(view.render().el);
			});
		},
		
		create: function (event) {
			//Create ReceivedView and append it to the list
			var subject = this.$el.find('#form_subject').val();
			var comment = this.$el.find('#form_comment').val();
			
			var a = new timelineApp.Publication({subject: this.$el.find('#form_subject').val(),
            	message: this.$el.find('#form_comment').val()});
			
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

var appView = new timelineApp.AppView();
alert("entro");
