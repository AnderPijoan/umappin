var messagesApp = messagesApp || {};

(function(){
	var FollowedCollection =Backbone.Collection.extend({
		model:messagesApp.User,

		url:'/followedInfo',
		search : function(letters){	 
			return _(this.filter(function(data) {
				return (data.get("name").indexOf(letters) !== -1 ||
						data.get("email").indexOf(letters) !== -1)&&
							data.get("selected") == false;
			}));
		},
		getSelected : function() {
			return _(this.filter(function(data) {
				return data.get("selected") == true;   		
			}));
		},
	});


	messagesApp.followeds = new FollowedCollection();
}());

