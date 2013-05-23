window.Maps or= {}

class window.Maps.Feature extends Backbone.Model
  urlRoot: '/routes'
  defaults:
    id: null
    name: '<Unknown>'
    difficulty: null
    geometry: null
    properties: {}