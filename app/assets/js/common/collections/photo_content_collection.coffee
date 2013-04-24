class window.Photos.Contents extends Backbone.Collection
  model: Photo.Content
  url: '/photos/*/content'

  orderBy: (order) ->
    @comparator = (model) -> model.get order