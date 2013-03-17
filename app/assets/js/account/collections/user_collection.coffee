window.Account or= {}

class window.Account.Users extends Backbone.Collection

  model: Account.User

  url: '/users'
