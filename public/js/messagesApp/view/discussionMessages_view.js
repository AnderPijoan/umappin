var messagesApp = messagesApp || {};
_.templateSettings.variable = "rc";
(function(){
	//this is the view controller for the messages of a discussion

	messagesApp.MessagesView = Backbone.View.extend({

        template: _.template($('#messages-template').html()),

        events: {
            "click #reply": "reply"
        },

        initialize: function(attrs, options) {
            _.bindAll(this, "render");
            this.model.bind('messageAgo', this.render);
        },

        render: function() {
            //changed to html() because replace, replaced the selected whole tag
            console.log(this.model);
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
                            that.model.fetch();
                        },
                        error: function(){
                            console.log("Error saving message");
                        }
                    }
                );
        }
	});
}());