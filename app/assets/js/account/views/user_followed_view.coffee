window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.UserFollowedView extends Backbone.View

  followed: null
  tagName: 'div'
  className: 'span2'

  template: _.template $('#user-followed-template').html()

  initialize: ->
    @listenTo @model, 'change', @render
    @followed = @options.followed

  render: ->
    text = if @model.get("follow").indexOf(@followed) != -1  then 'Follower' else 'Non Follower'
    @$el.html @template text
    @