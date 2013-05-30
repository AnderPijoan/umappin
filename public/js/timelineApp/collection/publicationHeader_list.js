var timelineApp = timelineApp || {};

(function(){
	var PublicationHeaderList =Backbone.Collection.extend({

		model:timelineApp.PublicationHeader,
		loadPublications:function(){
			//here we load all Publications from the server side,... now from json File
			this.reset();
			console.log("reset Received Publications");
			$.getJSON("/assets/js/timelineApp/test/publication_header.json", function(data) {
			    $.each(data, function(key, val) {
			    	timelineApp.PublicationHeaders.add(val);
				});
			});
		}
	});

	// Create our global collection of **Publications**.
	timelineApp.PublicationHeaders = new PublicationHeaderList();
}());