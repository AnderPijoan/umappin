window.Maps or= {}

class window.Maps.RouteLike extends Backbone.Model
  urlRoot: '/routelikes'
  defaults:
    id: null
    routeId: null
    userId: null
    comment: null