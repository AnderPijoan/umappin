$ () ->

  window.Account or= {}

  usr = new Account.User {id:"dummyId", name:"Patricio", email:"patri@patri.es"}
  users = new Account.Users
  userview = new Account.UsersView collection: users
  users.fetch
    success: ->
      users.add usr

  profile = new Account.User JSON.parse sessionStorage.getItem "user"
  profileview = new Account.ProfileView model: profile
  profileview.render()
