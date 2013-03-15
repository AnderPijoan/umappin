class window.User extends Backbone.Model
  defaults:
    id: ''
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
