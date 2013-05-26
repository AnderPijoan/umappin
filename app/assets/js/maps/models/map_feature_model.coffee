window.Maps or= {}

class window.Maps.Feature extends Backbone.Model
  urlRoot: '/mapfeatures'
  defaults:
    id: null
    ownerId: null
    name: '<Unknown>'
    type: null
    geometry: null