var timelineApp = timelineApp || {};

//this is the view controller for the all comments APP
(function(){
	timelineApp.UserAppView = Backbone.View.extend({
		el: 'div#wallDiv',

		events: {
            "click #send_new_publication_button":   "create"//,
            //"click #user-follow": "userFollow",
            //"click #user-unfollow": "userUnfollow"
        },

		initialize: function () {
			//triggered on sync
			this.listenTo(timelineApp.UserPublicationCollection, 'sync reset change', this.renderPublications);
		},
        /*
        userFollow: function(){
          var userFollow = sessionStorage.getItem('wall-follow-user'),
          		currentUser = sessionStorage.getItem('user');
	      var userFollowProfile = $.ajax({
                url: '/api/addFollows/'+userFollow,
                type: 'PUT',
                contentType: 'application/json',
                data: JSON.stringify(currentUser)
        	});
	      userFollowProfile.done(function( data ) {
			$('#user-wall-follow').html('<button id="user-unfollow" class="btn btn-primary">Unfollow</button>');
	      });
	      userFollowProfile.error(function( data ) {
	      	console.log(data);
	      });
        },
        userUnfollow: function(){
          var userFollow = sessionStorage.getItem('wall-follow-user'),
          	currentUser = sessionStorage.getItem('user');
	      var userFollowProfile = $.ajax({
                url: '/api/unfollow/'+userFollow,
                type: 'PUT',
                contentType: 'application/json',
                data: JSON.stringify(currentUser)
        	});
	      userFollowProfile.done(function( data ) {
			$('#user-wall-follow').html('<button id="user-follow" class="btn btn-primary">Follow</button>');
	      });
	      userFollowProfile.error(function( data ) {
	      	console.log(data);
	      });
        },
        */
		//Publication Headers methods

		renderPublications: function(){
			$('#comments_body').html('<ul id="publication_list"> </ul>');
			timelineApp.UserPublicationCollection.sort();
			timelineApp.UserPublicationCollection.each(function(publication){
				var date = new Date(publication.get("timeStamp"));
				publication.set({"date": date});
			   	var view = new timelineApp.ReceivedView({ model: publication});
			   	$('#publication_list').prepend(view.render().el);
			});
		},

		create: function (event) {
			//Create ReceivedView and append it to the list
			var subject = this.$el.find('#form_subject').val();
			var comment = this.$el.find('#form_comment').val();

			var a = new timelineApp.UserPublication({subject: this.$el.find('#form_subject').val(),
            	message: this.$el.find('#form_comment').val()});
            a.urlRoot = timelineApp.UserPublicationCollection.url
			timelineApp.UserPublicationCollection.unshift(a);
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

