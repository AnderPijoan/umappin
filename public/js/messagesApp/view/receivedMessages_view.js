var messagesApp = messagesApp || {};
_.templateSettings.variable = "rc";
(function(){
	//this is the view controller for the all discussions (the list of discussions)

	messagesApp.ReceivedView = Backbone.View.extend({
		tagName:  'li',
        className: 'row',

		//set the discussion template
		template: _.template($('#discussion-head-template').html()),
		initialize: function(attrs, options) {
            return this.addTimeAgoAndRender();
        },
        addTimeAgoAndRender: function() { //Function to add to each comment the text timeAgo. Finally it
                          //calls render
            var timeStamp = this.model.get("timeStamp"),
            	diff = new Date().getTime() - timeStamp,
            	dateDiff = new Date(diff),
            	timeAgo,
            	dateComment = new Date(timeStamp);

            if (parseInt((diff/(1000*60*60*24).toFixed(0)),10) > 6) {
                timeAgo=(dateComment.getMonth()+1) + "/" + dateComment.getDate() +
                		"/" + dateComment.getFullYear();
            } else if ((diff/(1000*60*60*24).toFixed(0)) > 1) {
            	timeAgo=(diff / (1000*60*60*24)).toFixed(0) + " days";
            } else if (parseInt((diff/(1000*60*60*24).toFixed(0)),10) == 1){
            	timeAgo="Yesterday";
            } else if (parseInt((diff/(1000*60*60).toFixed(0)),10) > 1) {
            	timeAgo=dateDiff.getHours() + " hours";
            } else if (parseInt((diff/(1000*60*60).toFixed(0)),10) == 1) {
            	timeAgo="1 hour";
            } else if (parseInt((diff/(1000*60).toFixed(0)),10) > 1) {
            	timeAgo=dateDiff.getMinutes() + " mins";
            } else if (parseInt((diff/(1000*60).toFixed(0)),10) == 1) {
            	timeAgo="1 min";
            } else {
            	timeAgo=dateDiff.getSeconds() + " secs";
            }
            this.model.set("timeAgo", timeAgo);
        },
		render: function(){
			//return the template with the info of the model and changed to
			//messages_body to have this one as the messages APP body
			//$('#messages_body').html(this.template(this.model.toJSON()));
			//return the template with the info of the model
			$(this.el).html(this.template(this.model.toJSON()));
			console.log("render received Discussions");
			return this;
		}
	});
}());

//var messagesApp.receivedDiscussions = new messagesApp.ReceivedView();