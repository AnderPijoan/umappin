var messagesApp = messagesApp || {};


//this is the view controller for the all messages APP
(function(){
	messagesApp.NewDiscussionView = Backbone.View.extend({
		el: '#newDiscussion',
		friends_loaded :false,

		
		events: {
			"click #send_new_discussion_button":   "create",
			"focus #form_receivers": 			"load_friends",
			//"click #cancelDiscussionButton": "destroy"
		},
		initialize: function () {
			//triggered on sync
			this.listenTo(messagesApp.followeds, 'sync', this.show_friends);

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

		load_friends: function(){
			if (!this.friends_loaded){
				messagesApp.followeds.fetch();
				this.friends_loaded = true;
			}
		},

		show_friends: function(){
			console.log("show friends");
			console.log(messagesApp.followeds);
			if (messagesApp.followeds.size() == 0){
				this.$el.find('#form_receivers').attr("disabled", "disabled");
			}else{
				messagesApp.searchView = messagesApp.searchView || new messagesApp.UserSearchView(
					{collection: messagesApp.followeds});
				this.$el.find('#form_receivers').removeAttr("disabled");
			}
			

		},

		create: function (discussion) {
			//Create ReceivedView and append it to the list
			var subject = this.$el.find('#form_subject').val();
			var message = this.$el.find('#form_message').val();
			var receivers = [];

			messagesApp.followeds.getSelected().each(function(e){
				receivers.push(e.get('id'));
				e.set('selected',false);
			});			

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
					this.friends_loaded = false;	
					$('#receivers_list').html('');	   
				}
			});

			
			
			
		}
	});
})();

messagesApp.newDiscussionView = new messagesApp.NewDiscussionView();