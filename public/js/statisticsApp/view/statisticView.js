var statisticsApp = statisticsApp || {};
_.templateSettings.variable = "rc";

//this is the view controller for the Statistics
(function(){
	statisticsApp.StatisticView = Backbone.View.extend({
		initialize: function() {
			console.log(this.model.get('statistics'));
			$('#awards-badge').hide();
            var currentUser = JSON.parse(sessionStorage.getItem('user')),
				statistics = this.model.get('statistics');
			statistics.INVITEFRIENDS = localStorage.getItem('invite-friend-award'+currentUser.id);
			if(!statistics.INVITEFRIENDS){
				statistics.INVITEFRIENDS = 0;
			}
		},
		setTemplate: function() {
			this.template = _.template($('#statistics-template').html());
		},

		render:function () {
			$(this.el).html(this.template(this.model.toJSON()));
            return this;
		}
	});
})();