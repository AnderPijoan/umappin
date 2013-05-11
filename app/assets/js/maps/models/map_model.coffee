window.Maps or= {}

class window.Maps.Map extends Backbone.Model
  urlRoot: '/maps'
  defaults:
    id: null
    ownerId: null
    features: []