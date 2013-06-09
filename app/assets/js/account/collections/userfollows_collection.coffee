window.Account or= {}

class window.Account.UserFollows extends Account.FollowCollection
  model: Account.UsrFollows
  url: '/userfollows'
