var timelineApp = timelineApp || {};

(function(){

	timelineApp.Publication = Backbone.Model.extend({

		urlRoot:'/publications',

		validate: function(attrs){
			if (attrs === null || attrs.id === null || attrs.subject===null ||
                  attrs.users === null || attrs.timeStamp ===null ||
                  attrs.messages ===null || attrs.lastWrote===null){
                  return "Error validating the Publication";
          }
        },

        initialize: function(attrs, options) {
            this.on("invalid", function(model, error){
            	alert("error");
                console.log(error);
            });
            this.set(attrs, {validate: true});
		}
	});
})();
