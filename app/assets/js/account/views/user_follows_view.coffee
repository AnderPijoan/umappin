window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.UserFollowsView extends Backbone.View

  followed: null
  tagName: 'div'
  className: 'span1'

  events:
    "click button":  "follow"

  template: _.template $('#user-follows-template').html()

  initialize: ->
    @listenTo @model, 'change', @render
    @followed = @options.followed
    
  render: ->
    text = if @model.get("follow").indexOf(@followed) != -1  then 'Unfollow' else 'Follow'
    @$el.html @template text
    @

  follow: () ->
    followedObj = Account.followed.getByUserId @followed
    followsPos = @model.get("follow").indexOf @followed
    followedPos = followedObj.get("follow").indexOf @model.get "id"

    if followsPos == -1
      @model.get("follow").push @followed
      if followedPos == -1
        followedObj.get("follow").push @model.get "id"
    else
      @model.get("follow").splice followsPos, 1
      if followedPos != -1
        followedObj.get("follow").splice followedPos, 1

    @model.save() # Here  to decide whether to use local/session storage as cache
    followedObj.save() # Here  to decide whether to use local/session storage as cache
    @model.trigger 'change'
    followedObj.trigger 'change'
