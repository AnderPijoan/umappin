window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.UserRowView extends Backbone.View

  refUser: null
  followsView: null

  tagName: 'li'

  className: 'user-entry'

  template: _.template $('#user-row-template').html()

  initialize: -> 
    @listenTo @model, 'change', @render
    @refUser = @options.refUser
    follows = Account.follows.getByUserId @refUser.get 'id'
    if follows == undefined
      follows = new Account.Follow
      follows.set userId: @refUser.get 'id'
      follows.set follow: []
      Account.follows.add follows
      follows.save() # Here  to decide whether to use local/session storage as cache
    @followsView = new Account.UserFollowsView model: follows, followed: @model.get 'id'

  render: ->   
    @$el.html @template @model.attributes
    @$el.find('div.row').append @followsView.render().el
    @

