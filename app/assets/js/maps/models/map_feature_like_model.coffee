window.Maps or= {}

class window.Maps.FeatureLike extends Backbone.Model
  urlRoot: '/featurelikes'
  defaults:
    id: null
    featureId: null
    userId: null
    comment: null