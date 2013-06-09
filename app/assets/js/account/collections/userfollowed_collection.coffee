window.Account or= {}

class window.Account.UserFollowed extends Account.FollowCollection
  model: Account.Followed
  url: '/userfollowed'
