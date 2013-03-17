var messagesApp = messagesApp || {};


//this is the view controller for the all messages APP
(function(){
	messagesApp.NewDiscussionView = Backbone.View.extend({
		el: '#newDiscussion',
		
		events: {
			"click #createDiscussionButton":   "create",
			"click #form_all_check": 			"toggle_friends",
			//"click #cancelDiscussionButton": "destroy"
		},

		showDiscussionForm: function (){
			this.$el.show();

		},

		toggle_friends:function(){
			if ($('#form_all_check').is(":checked")){
				$('#form_receivers').attr('disabled',true);
			}else{
				$('#form_receivers').attr('disabled',false);
			}
		},

		create: function (discussion) {
			//Create ReceivedView and append it to the list
			var subject = this.$el.children('#form_subject').val();
			var message = this.$el.children('#form_message').val();
			var toFriends = this.$el.children('#form_all_check').is(':checked');
			var receivers = [];

			if (!toFriends){
				receivers = this.$el.children('#form_receivers').val().split(',');
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
			console.log(some);
			
		}
	});
})();


