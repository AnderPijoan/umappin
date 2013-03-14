var messagesApp = messagesApp || {};

(function(){
	//this is the view controller for the messages of a discussion

	messagesApp.MessagesView = Backbone.View.extend({

	    template: _.template($('#messages-template').html()),

	    render: function() {
	         $(this.el).html(this.template(disc1.toJSON()));
            return this;
	    }
	});
}());