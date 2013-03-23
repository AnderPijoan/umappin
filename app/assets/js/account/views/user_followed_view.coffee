window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.UserFollowedView extends Backbone.View

  followed: null
  tagName: 'div'
  className: 'span2'

  template: _.template $('#user-followed-template').html()

  initialize: ->
    @followed = @options.followed
    @listenTo @followed, 'change', @render

  render: ->
    text = if @followed.get("follow").indexOf(@model.get "id") != -1  then 'Follower' else 'Non Follower'
    @$el.html @template text
    @