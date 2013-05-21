var statisticsApp = statisticsApp || {};

(function(){
	statisticsApp.AwardListView = Backbone.View.extend({
		el:$("#statistics"),
		
		render:function () {
            var that = this;
            _.each(this.collection.models, function (award) {
                that.render(award);
            });
        }

	});
})();

var awardListView = new statisticsApp.AwardListView();