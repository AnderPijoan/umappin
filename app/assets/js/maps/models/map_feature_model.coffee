window.Maps or= {}

class window.Maps.Feature extends Backbone.Model
  urlRoot: '/features'
  defaults:
    id: null
    ownerId: null
    name: '<Unknown>'
    type: null
    geometry: null