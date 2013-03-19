window.Account or= {}

class window.Account.User extends Backbone.Model

  defaults:
    id: null
    email: ''
    name: ''
    firstName: ''
    lastName: ''
    lastLogin: ''
    active: ''
    emailValidated: ''
    roles: []
    linkedAccounts: []
    permissions: []
    providers: []
    identifier: ''
