var timelineApp = timelineApp || {};

//this is the view controller for the all comments APP
(function(){
	timelineApp.NewsAppView = Backbone.View.extend({
		el: 'div#wallDiv',

		initialize: function () {
			//triggered on sync
			this.listenTo(timelineApp.NewsCollection, 'sync', this.renderNews);
			this.renderNews();
		},
		//Publication Headers methods
		
		
		renderNews: function(){
			$('#news_body').html('<ul id="publication_list"> </ul>');
			timelineApp.NewsCollection.sort();
			timelineApp.NewsCollection.each(function(publication){
			   	var view = new timelineApp.ReceivedNewsView({ model: publication});
			   	$('#publication_list').prepend(view.render().el);
			});
		}		
	});
})();