window.Account or= {}

class window.Account.UserFollows extends Account.FollowCollection
  model: Account.Follows
  url: '/userfollows'
