var timelineApp = timelineApp || {};
timelineApp.PUBLICATION_LIST_BODY ='<ul id="publication_list"></ul>';

//this is the view controller for the all comments APP
(function(){
	timelineApp.AppView = Backbone.View.extend({
		el: 'body',
		initialize: function () {
			//triggered on sync
			this.listenTo(timelineApp.PublicationCollection, 'sync', this.renderPublications);

		},
		//Publication Headers methods
		
		
		renderPublications: function(){
			$('#comments_body').html('<ul id="publication_list"> </ul>');
			timelineApp.PublicationCollection.each(function(publication){
			   	var view = new timelineApp.ReceivedView({ model: publication});
			   	$('#publication_list').append(view.render().el);
			});	
		}

		//End Publications Headers methods
	});
})();

//created here ... it will be at another place,...
var appView = new timelineApp.AppView();
