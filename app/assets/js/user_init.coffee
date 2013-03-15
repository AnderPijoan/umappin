_.templateSettings.variable = "rc"

users = new Users
window.userview = new UserView users

users.fetch
  success: ->
    users.add usr
    console.log users.toJSON()
    $(userview.el).html userview.add().el
    console.log userview.$el

usr = new User({id:"dummyId", name:"Patricio", email:"patri@patri.es"})

console.log userview

