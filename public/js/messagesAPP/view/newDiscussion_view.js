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
			var toFriends = this.$el.find('#form_all_check').is(':checked');
			var receivers = [];

			if (!toFriends){
				receivers = this.$el.find('#form_receivers').val().split(',');
			}

			var newDiscussion={
				"subject": "'"+subject+"'",
				"messages":{
					"message":"'"+message+"'",
				},
				"to_friends": toFriends,
				"receivers": receivers,
			};


			var a = new messagesApp.Discussion(newDiscussion);
			messagesApp.DiscussionCollection.add(a);

			a.save({}, {  // se genera POST /usuarios  - contenido: {nombre:'Alfonso'}
			    success:function(){
			        // Suponiendo que el servidor ha devuelto el objeto {"id": 1}
			        alert(a.id);  // imprime 1
			    }
			});

			//console.error(some);
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


