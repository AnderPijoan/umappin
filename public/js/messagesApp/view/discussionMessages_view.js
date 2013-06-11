var messagesApp = messagesApp || {};
_.templateSettings.variable = "rc";
(function(){
	//this is the view controller for the messages of a discussion

	messagesApp.MessagesView = Backbone.View.extend({

        template: _.template($('#messages-template').html()),

        events: {
            "click #reply": "reply",
            "click #backMessage": "backMessages"
        },

        initialize: function(attrs, options) {
            _.bind(this, "render");
            this.model.on("sync",this.addTimeAgoAndRender, this);
        },
        addTimeAgoAndRender: function() { //Function to add to each message the text timeAgo. Finally it
                          //calls render
            //TODO
            //Get this out and make general function on /js/lib/utils.js
            //to work with in in any app. For example in Posts. ;)

            var messages = this.model.get("messages");
            for (var x in messages) {
                var diff = new Date().getTime()-messages[x].timeStamp;
                var dateDiff = new Date(diff);
                if (parseInt((diff/(1000*60*60*24).toFixed(0)),10) > 6) {
                    var dateMessage = new Date(messages[x].timeStamp);
                    messages[x].timeAgo=(dateMessage.getMonth()+1) + "/" + dateMessage.getDate() + "/" + dateMessage.getFullYear();
                } else if ((diff/(1000*60*60*24).toFixed(0)) > 1) {
                    messages[x].timeAgo=(diff / (1000*60*60*24)).toFixed(0) + " days ago";
                } else if (parseInt((diff/(1000*60*60*24).toFixed(0)),10) == 1){
                    messages[x].timeAgo="Yesterday";
                } else if (parseInt((diff/(1000*60*60).toFixed(0)),10) > 1) {
                    messages[x].timeAgo=dateDiff.getHours() + " hours ago";
                } else if (parseInt((diff/(1000*60*60).toFixed(0)),10) == 1) {
                    messages[x].timeAgo="1 hour ago";
                } else if (parseInt((diff/(1000*60).toFixed(0)),10) > 1) {
                    messages[x].timeAgo=dateDiff.getMinutes() + " minutes ago";
                } else if (parseInt((diff/(1000*60).toFixed(0)),10) == 1) {
                    messages[x].timeAgo="1 minute ago";
                } else {
                    messages[x].timeAgo=dateDiff.getSeconds() + " seconds ago";
                }
            }
            this.model.set("messages",messages);
            return this.render();
        },
        render: function() {
            //changed to html() because replace, replaced the selected whole tag
            $(this.el).html(this.template(this.model.toJSON()));
            return this;
        },
        reply: function(ev) {
            var that = this;
            message = new messagesApp.Message({discussion_id: this.model.id,
                                                message: this.$el.find('#sending_message').val()});
            this.model.unset("messages"); //unset messages because with attribute timeAgo it can't fetch
            message.save(null,
                    {
                        success: function(){
                            that.model.fetch();
                            //clean message
                            $('#sending_message').val("");
                            console.log(message);

                        },
                        error: function(){
                            console.log("Error saving message");
                        }
                    }
                );
        },
        backMessages: function() {
            location.href = "/#messages/";
        }
	});
}());