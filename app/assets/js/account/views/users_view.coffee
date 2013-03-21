window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.UsersView extends Backbone.View

  refUser = null
  userRowViews = null

  el: 'ul#userlist'

  initialize: ->
    @listenTo @collection, 'reset ', @render
    @refUser = @options.refUser

  render: ->
    @userRowViews = []
    @$el.html ''
    _this = @
    @collection.each (row) ->
      if row.get('id') != _this.refUser.get('id')
        view = new Account.UserRowView model: row, refUser: _this.refUser
        _this.userRowViews.push view
        _this.$el.append view.render().el
    @

