window.Account or= {}

Account.loadUsersData = () ->
  usr = new Account.User {id:"dummyId", name:"Patricio", email:"patri@patri.es"}
  @users = new Account.Users
  @usersview = new Account.UsersView collection: @users
  that = @
  @users.fetch
    success: ->
      that.users.add usr

Account.loadProfileData = () ->
  @profile = new Account.User JSON.parse sessionStorage.getItem "user"
  @profileview = new Account.ProfileView model: @profile
  @profileview.render()

$ () ->
  Account.loadProfileData()





