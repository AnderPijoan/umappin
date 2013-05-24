window.Maps or= {}

class window.Maps.Route extends Backbone.Model
  urlRoot: '/routes'
  defaults:
    id: null
    name: '<Unknown>'
    difficulty: null
    geometry: null
    properties: {}