class window.Photos extends Backbone.Collection
  model: Photo
  url: '/photos'

  orderBy: (order) ->
    @comparator = (model) -> model.get order