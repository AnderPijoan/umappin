var timelineApp = timelineApp || {};

//this is the view controller for the all comments APP
(function(){
	timelineApp.NewsAppView = Backbone.View.extend({
		el: 'body',

		initialize: function () {
			//triggered on sync
			this.listenTo(timelineApp.NewsCollection, 'sync', this.renderNews);

		},
		//Publication Headers methods
		
		
		renderNews: function(){
			$('#news_body').html('<ul id="publication_list"> </ul>');
			timelineApp.NewsCollection.sort();
			timelineApp.NewsCollection.each(function(publication){
				var date = new Date(publication.get("timeStamp"));
				publication.set({"date": date});
			   	var view = new timelineApp.ReceivedView({ model: publication});
			   	$('#publication_list').prepend(view.render().el);
			});
		}		
	});
	
	newsAppView = (typeof newsAppView === 'undefined') ? new timelineApp.NewsAppView() : newsAppView;
	
})();
	