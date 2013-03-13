var messagesApp = messagesApp || {};
(function(){
    Router = Backbone.Router.extend({
       routes: {
       "messages/:id" : "messages"
       },

       messages: function(id) {

       }
    });

})();