var umappin = umappin || {};
requirejs(['/assets/js/messagesAPP/routers/router.js']);
umappin.Routers ={};
umappin.Router = Backbone.Router.extend({

    routes: {
        // general routes for cross-app functionality
        "messages/*subroute"                 : "invokeMessages",
    },
    invokeMessages: function(subroute) {
          console.log("to messages subrouter...");
          if (!umappin.Routers.messagesRouter) {
              umappin.Routers.messagesRouter = new messagesApp.Router("messages/");
          }
    },
    
  });

  // Actually initialize
  new umappin.Router();
