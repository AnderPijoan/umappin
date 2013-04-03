defaultSync = Backbone.sync
Backbone.sync = (method, model, options) ->
  if typeof(window.localStorage) != 'undefined'
    url = if model.id then model.url() else model.url
    optionsSuccess = options.success
    options.success = (model, resp, options) ->
      window.localStorage.setItem url, JSON.stringify resp
      optionsSuccess model, resp, options unless not optionsSuccess
    console.log window.localStorage
    if method == "read"
      item = window.localStorage.getItem url
      if item
        optionsSuccess model, (JSON.parse item), options
        return

  defaultSync(method, model, options)
