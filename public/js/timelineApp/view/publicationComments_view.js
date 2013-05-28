var timelineApp = timelineApp || {};

_.templateSettings.variable = "rc";

(function(){
	//this is the view controller for the comments of a publication

	timelineApp.CommentsView = Backbone.View.extend({

        template: _.template($('#comments-template').html()),

        events: {
            "click #reply": "reply"
        },

        initialize: function(attrs, options) {
            _.bind(this, "render");
            this.model.on("sync",this.addTimeAgoAndRender, this);
        },
        addTimeAgoAndRender: function() { //Function to add to each comment the text timeAgo. Finally it
                          //calls render
            //TODO
            //Get this out and make general function on /js/lib/utils.js
            //to work with in any app. For example in Posts. ;)

            var comments = this.model.get("messages");
            for (var x in comments) {
                var diff = new Date().getTime()-comments[x].timeStamp;
                var dateDiff = new Date(diff);
                if (parseInt((diff/(1000*60*60*24).toFixed(0)),10) > 6) {
                    var dateComment = new Date(comments[x].timeStamp);
                    comments[x].timeAgo=(dateComment.getMonth()+1) + "/" + dateComment.getDate() + "/" + dateComment.getFullYear();
                } else if ((diff/(1000*60*60*24).toFixed(0)) > 1) {
                	comments[x].timeAgo=(diff / (1000*60*60*24)).toFixed(0) + " days ago";
                } else if (parseInt((diff/(1000*60*60*24).toFixed(0)),10) == 1){
                	comments[x].timeAgo="Yesterday";
                } else if (parseInt((diff/(1000*60*60).toFixed(0)),10) > 1) {
                	comments[x].timeAgo=dateDiff.getHours() + " hours ago";
                } else if (parseInt((diff/(1000*60*60).toFixed(0)),10) == 1) {
                	comments[x].timeAgo="1 hour ago";
                } else if (parseInt((diff/(1000*60).toFixed(0)),10) > 1) {
                	comments[x].timeAgo=dateDiff.getMinutes() + " minutes ago";
                } else if (parseInt((diff/(1000*60).toFixed(0)),10) == 1) {
                	comments[x].timeAgo="1 minute ago";
                } else {
                	comments[x].timeAgo=dateDiff.getSeconds() + " seconds ago";
                }
            }
            this.model.set("messages",comments);
            return this.render();
        },
        render: function() {
            //changed to html() because replace, replaced the selected whole tag
            $(this.el).html(this.template(this.model.toJSON()));
            return this;
        },
        reply: function(ev) {
            var that = this;
            comment = new timelineApp.Comment({publication_id: this.model.id,
            	message: this.$el.find('#sending_comment').val()});
            console.log(JSON.stringify(comment));
            this.model.unset("messages"); //unset comments because with attribute timeAgo it can't fetch
            comment.save(null,
                    {
                        success: function(){
                            that.model.fetch();
                            //clean message
                            $('#sending_comment').val("");
                            console.log(comment);

                        },
                        error: function(){
                            console.log("Error saving the comment");
                        }
                    }
                );
        }
	});
}());