var messagesApp = messagesApp || {};


//this is the view controller for the all messages APP
(function(){
	messagesApp.NewDiscussionView = Backbone.View.extend({
		el: '#newDiscussion',

		
		events: {
			"click #send_new_discussion_button":   "create",
			"click #form_all_check": 			"toggle_friends",
			//"click #cancelDiscussionButton": "destroy"
		},

		showDiscussionForm: function (){
			console.log('some');
			this.$el.on('hide',function(){
				//Call back function from messages Router
				//impl. in messagesApp/routeps/router.js
				//It can be changed if /app/assets/js/router.coffee is changed
				umappin.router.subroutes.messagesRouter.back();
			}).modal('show');

		},

		toggle_friends:function(){
			if (this.$el.find('#form_all_check').is(":checked")){
				this.$el.find('#form_receivers').attr('disabled',true);
			}else{
				this.$el.find('#form_receivers').attr('disabled',false);
			}
		},

		create: function (discussion) {
			//Create ReceivedView and append it to the list
			var subject = this.$el.find('#form_subject').val();
			var message = this.$el.find('#form_message').val();
			var receivers = [];

			receivers = this.$el.find('#form_receivers').val().split(',');
			

			var newDiscussion={
				"subject": subject,
				"messages":{
					"message":message,
				},
				"users": receivers,
			};

			var a = new messagesApp.Discussion(newDiscussion);
			messagesApp.DiscussionCollection.add(a);
			var that = this;
			a.save({}, {  
			    success:function(){
			    	//clear form
					that.$el.find('#form_subject').val('');
					that.$el.find('#form_message').val('');
					that.$el.find('#form_receivers').val('');
					that.$el.find('#form_all_check').attr('checked', false);
					that.$el.find('#form_receivers').attr('disabled',false);

					//hide the modal dialog
					that.$el.modal('hide');			   
				}
			});

			
			
			
		}
	});
})();

messagesApp.newDiscussionView = new messagesApp.NewDiscussionView();



