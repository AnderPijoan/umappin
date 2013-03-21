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
    console.log @model.get("follow")
    text = if @model.get("follow").indexOf(@followed) != -1  then 'Unfollow' else 'Follow'
    @$el.html @template text
    @

  follow: () ->
    if @model.get("follow").indexOf(@followed) == -1
      @model.get("follow").push @followed
    else
      @model.get("follow").splice(@model.get("follow").indexOf(@followed), 1)
    # Here  to decide whether to use local/session storage as cache
    @model.save()

    @model.trigger('change')
    #@render()
