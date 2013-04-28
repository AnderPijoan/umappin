class window.Map extends Backbone.Model
  urlRoot: '/maps'
  defaults:
    id: null
    ownerId: null
    features: []