var messagesApp = messagesApp || {};
(function(){
    Router = Backbone.Router.extend({
       routes: {
       "message/:id" : "messages",
       "received" : "discussionHeaders"
       }
    });

    var router = new Router;
    router.on("route:messages", function(id){
           view = new messagesApp.MessagesView({ model: disc1 });
           console.log("Backbone routing to message");
           $('#discussion_list').replaceWith(view.render().el);
    });
    router.on("route:discussionHeaders", function() {
        console.log("routing received");
    });

    Backbone.history.start();

})();
