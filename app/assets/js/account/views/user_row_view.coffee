window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.UserRowView extends Backbone.View

  follows: null
  followed: null
  usrFollowed: null
  followsView: null
  followedView: null
  tagName: 'li'
  className: 'user-entry'
  template: _.template $('#user-row-template').html()

  initialize: -> 
    @listenTo @model, 'change reset', @render
    @follows = @options.follows
    @followed = @options.followed
    @usrFollowed = @options.usrFollowed

    @followsView = new Account.UserFollowsView model: @model, follows: @follows, followed: @usrFollowed
    @followedView = new Account.UserFollowedView model: @model ,followed: @followed

  render: ->   
    @$el.html @template @model.attributes
    @$el.find('div.row').append @followsView.render().el
    @$el.find('div.row').append @followedView.render().el
    @

