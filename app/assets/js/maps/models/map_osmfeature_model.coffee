window.Maps or= {}

class window.Maps.OsmFeature extends Backbone.Model
  defaults:
    id: null
    version: null
    user: null
    uid: null
    timeStamp: null
    properties: {}