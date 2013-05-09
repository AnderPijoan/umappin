window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.UserFollowsView extends Backbone.View

  follows: null
  followed: null
  tagName: 'div'
  className: 'span1'

  events:
    "click button":  "follow"

  template: _.template $('#user-follows-template').html()

  initialize: ->
    @follows = @options.follows
    @followed = @options.followed
    @listenTo @follows, 'change reset', @render

  render: ->
    text = if @follows.get("follow").indexOf(@model.get "id") != -1  then 'Unfollow' else 'Follow'
    @$el.html @template text
    @

  follow: () ->
    followsPos = @follows.get("follow").indexOf @model.get "id"
    followedPos = @followed.get("follow").indexOf @follows.get "userId"

    if followsPos == -1
      @follows.get("follow").push @model.get "id"
      if followedPos == -1
        @followed.get("follow").push @follows.get "userId"
    else
      @follows.get("follow").splice followsPos, 1
      if followedPos != -1
        @followed.get("follow").splice followedPos, 1

    @follows.save success: (resp) -> @follows.trigger 'change'
    @followed.save success: (resp) -> @followed.trigger 'change'
