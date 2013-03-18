window.Account or= {}

class window.Account.Follows extends Backbone.Collection

  model: Account.Follow

  url: '/follows'
