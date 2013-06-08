var timelineApp = timelineApp || {};
_.templateSettings.variable = "rc";
(function(){
	//this is the view controller for the all Publications (the list of publications)

	timelineApp.ReceivedView = Backbone.View.extend({
		tagName:  'li',

		//set the publication template
		template: _.template($('#publication-head-template').html()),
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
            	timeAgo=(diff / (1000*60*60*24)).toFixed(0) + " days ago";
            } else if (parseInt((diff/(1000*60*60*24).toFixed(0)),10) == 1){
            	timeAgo="Yesterday";
            } else if (parseInt((diff/(1000*60*60).toFixed(0)),10) > 1) {
            	timeAgo=dateDiff.getHours() + " hours ago";
            } else if (parseInt((diff/(1000*60*60).toFixed(0)),10) == 1) {
            	timeAgo="1 hour ago";
            } else if (parseInt((diff/(1000*60).toFixed(0)),10) > 1) {
            	timeAgo=dateDiff.getMinutes() + " minutes ago";
            } else if (parseInt((diff/(1000*60).toFixed(0)),10) == 1) {
            	timeAgo="1 minute ago";
            } else {
            	timeAgo=dateDiff.getSeconds() + " seconds ago";
            }
            this.model.set("timeAgo", timeAgo);
        },
		render: function(){
			//return the template with the info of the model and changed to
			//comments_body to have this one as the comments APP body
			//$('#comments_body').html(this.template(this.model.toJSON()));
			//return the template with the info of the model
			$(this.el).html(this.template(this.model.toJSON()));
			console.log("render received Publications");
			return this;
		}
	});
}());