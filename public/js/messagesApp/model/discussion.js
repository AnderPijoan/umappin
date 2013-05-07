var messagesApp = messagesApp || {};

(function(){

    messagesApp.Discussion = Backbone.Model.extend({

		urlRoot:'/discussions',

		validate: function(attrs){
			if (attrs === null || attrs.id === null || attrs.subject===null ||
                  attrs.users === null || attrs.timeStamp ===null ||
                  attrs.messages ===null || attrs.lastWrote===null){
                  return "Error creating a Discussion";
          }
        },

        initialize: function(attrs, options) {
            this.on("invalid", function(model, error){
                console.log(error);
            });
            this.set(attrs, {validate: true});
		},
		messageAgo: function() {
			messages = this.get("messages");
			for (var x in messages) {
				diff = new Date().getTime()-messages[x].timeStamp;
				dateDiff=new Date(diff);
				if (parseInt((diff/(1000*60*60*24).toFixed(0)),10) > 6) {
					messages.timeAgo=(dateDiff.getMonth()+1) + "/" + dateDiff.getDate() + "/" + dateDiff.getFullYear();
				} else if (parseInt((diff/(1000*60*60*24).toFixed(0)),10) == 1){
					messages.timeAgo="Yesterday";
				} else if (parseInt((diff/(1000*60*60).toFixed(0)),10) > 1) {
					messages.timeAgo=dateDiff.getHours() + " hours ago";
				} else if (parseInt((diff/(1000*60*60).toFixed(0)),10) == 1) {
					messages.timeAgo="1 hour ago";
				} else if (parseInt((diff/(1000*60).toFixed(0)),10) > 1) {
					messages.timeAgo=dateDiff.getMinutes() + " minutes ago";
				} else if (parseInt((diff/(1000*60).toFixed(0)),10) == 1) {
					messages.timeAgo="1 minute ago";
				} else {
					messages.timeAgo=dateDiff.getSeconds() + "seconds ago";
				}
			}
			this.set("messages",messages);
		}
		/*,
		save: function(attributes, options) {
			console.log("save");
			if (attributes.privateMessage === true ) {
				options = _.defaults((options || {}), {url: '/discussions/' + this.attributes.id});
				return Backbone.Model.prototype.save.call(this, this.attributes.newMessage.message, options);
			} else {
				return Backbone.Model.prototype.save.call(this, attributes, options);
			}
		}*/
	});
})();
