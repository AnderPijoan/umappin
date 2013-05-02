var messagesApp = messagesApp || {};
_.templateSettings.variable = "rc";
(function(){
	//this is the view controller for the messages of a discussion

	messagesApp.MessagesView = Backbone.View.extend({

        template: _.template($('#messages-template').html()),

        events: {
            "click #reply": "reply"
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
            message.save(null,
                    {
                        success: function(){
                            console.log("successfully saved message");
                            that.model.fetch({
                                succes:function(){
                                    that.render();
                                    console.log("model with new message received");
                                },
                                error:function(){
                                    console.log("error fetching discussion");
                                }
                            });
                        },
                        error: function(){
                            console.log("Error saving message");
                        }
                    }
                );
        }
	});
}());