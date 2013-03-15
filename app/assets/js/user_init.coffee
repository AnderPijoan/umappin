usr = new User({id:"dummyId", name:"Patricio", email:"patri@patri.es"})
users = new Users
userview = new UserView users
#users.bind 'add', () -> userview.render()
users.fetch
  success: ->
    users.add usr

