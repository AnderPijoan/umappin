window.Account or= {}

Account.loadUsersData = () ->
  usr = new Account.User {id:"dummyId", name:"Patricio", email:"patri@patri.es"}
  Account.users = new Account.Users
  Account.usersview = new Account.UsersView collection: Account.users
  Account.users.fetch()

  Account.follows = new Account.Follows
  Account.follows.fetch()

Account.loadProfileData = () ->
  Account.profile = new Account.User JSON.parse sessionStorage.getItem "user"
  Account.profileview = new Account.ProfileView model: Account.profile
  Account.profileview.render()

$ () ->
  Account.loadProfileData()





