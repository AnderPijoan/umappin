
_.templateSettings.variable = "rc"

users = new Users
userview = new UserView users

users.fetch
  success: ->
    users.add usr
    console.log $(userview.add())

usr = new User({id:"dummyId", name:"Patricio", email:"patri@patri.es"})
