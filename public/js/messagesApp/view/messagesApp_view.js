var messagesApp = messagesApp || {};


//this is the view controller for the all messages APP
(function(){
	messagesApp.AppView = Backbone.View.extend({

        events: {
            "click li>a": "addMore"
        },

        el: 'ul#discussion_list',

		//set the discussion template
		template: _.template($('#discussion-list-template').html()),
		
		render: function(){
			//return the template with the info of the model
			this.$el.html(this.template(this.model.toJSON()));
			return this;
		},

		initialize: function () {
			//if the add method of Discussions is called  "this.addOne" whill be triggered
			this.listenTo(messagesApp.Discussions, 'add', this.addOne);
			

			//messagesApp.Discussions.fetch();
		},
		addOne: function (discussion) {
            this.render()
			//Create ReceivedView and append it to the list
			//$('#discussion_list').html(this.render().el);
			//var view = new messagesApp.ReceivedView({ model: discussion });
			//$('#discussion_list').append(view.render().el);
		},
        addMore: function () {
            alert('clicked!');
            var disc3 = new messagesApp.Discussion({

                "id":"64321",
                "subject": "This second subject",
                "message_number":"3", //number of total messages
                "unread_messages":"0",
                "user":{
                    "id":"124", //creator user id
                    "name":"Pepe Gotera",
                    "user_pic":"http://" //blank for now

                }

            });
            messagesApp.Discussions.add(disc3);

        }
		
	});
})();

//created here ... it will be at another place,...
var appView = new messagesApp.AppView({ model: messagesApp.Discussions });
