var timelineApp = timelineApp || {};


//this is the view controller for the all messages APP
(function(){
	timelineApp.NewPublicationView = Backbone.View.extend({
		el: '#newPublication',
		friends_loaded :false,

		
		events: {
			"click #send_new_publication_button":   "create",
			"focus #form_receivers": 				"load_friends",
			//"click #cancelDiscussionButton": "destroy"
		},
		initialize: function () {
			//triggered on sync
			this.listenTo(timelineApp.followeds, 'sync', this.show_friends);

		},

		showPublicationForm: function (){
			console.log('some');
			this.$el.on('hide',function(){
				//Call back function from comments Router
				//impl. in timelineApp/routers/router.js
				//It can be changed if /app/assets/js/router.coffee is changed
				umappin.router.subroutes.commentsRouter.back();
			}).modal('show');

		},

		load_friends: function(){
			if (!this.friends_loaded){
				timelineApp.followeds.fetch();
				this.friends_loaded = true;
			}
		},

		show_friends: function(){
			console.log("show friends");
			console.log(timelineApp.followeds);
			if (timelineApp.followeds.size() == 0){
				this.$el.find('#form_receivers').attr("disabled", "disabled");
			}else{
				timelineApp.searchView = timelineApp.searchView || new timelineApp.UserSearchView(
					{collection: timelineApp.followeds});
				this.$el.find('#form_receivers').removeAttr("disabled");
			}
			

		},

		create: function (publication) {
			//Create ReceivedView and append it to the list
			var subject = this.$el.find('#form_subject').val();
			var comment = this.$el.find('#form_comment').val();
			var receivers = [];

			timelineApp.followeds.getSelected().each(function(e){
				receivers.push(e.get('id'));
				e.set('selected',false);
			});			

			var newPublication={
				"subject": subject,
				"comments":{
					"comment":comment,
				},
				"users": receivers,
			};
			var a = new timelineApp.Publication(newPublication);
			timelineApp.PublicationCollection.add(a);
			var that = this;
			a.save({}, {  
			    success:function(){
			    	//clear form
					that.$el.find('#form_subject').val('');
					that.$el.find('#form_comment').val('');
					that.$el.find('#form_receivers').val('');
					that.$el.find('#form_all_check').attr('checked', false);
					that.$el.find('#form_receivers').attr('disabled',false);

					//hide the modal dialog
					that.$el.modal('hide');
					this.friends_loaded = false;	
					$('#receivers_list').html('');	   
				}
			});

			
			
			
		}
	});
})();

timelineApp.newPublicationView = new timelineApp.NewPublicationView();



