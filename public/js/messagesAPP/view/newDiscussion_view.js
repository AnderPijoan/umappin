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
				messagesApp.messagesRouter.back();
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
			var toFriends = this.$el.find('#form_all_check').is(':checked');
			var receivers = [];

			if (!toFriends){
				receivers = this.$el.find('#form_receivers').val().split(',');
			}

			var some={
				"discussion":{
					"subject": "'"+subject+"'",
					"message":{
						"message":"'"+message+"'",
					},
					"to_friends": toFriends,
					"receiver_users": receivers,


				}
			};
			console.log("Create Message "+subject);
			//checx if the discussion have been send
			if(true){
				//clean the form
				this.$el.find('#form_subject').val('');
				this.$el.find('#form_message').val('');
				this.$el.find('#form_receivers').val('');
				this.$el.find('#form_all_check').attr('checked', false);
				this.$el.find('#form_receivers').attr('disabled',false);

				//hide the modal dialog
				this.$el.modal('hide');
			}
			
		}
	});
})();


