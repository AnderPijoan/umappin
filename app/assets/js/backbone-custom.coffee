defaultSync = Backbone.sync

Backbone.sync = (method, model, options) ->
  if typeof(window.localStorage) != 'undefined'
    optionsSuccess = options.success
    url = if $.isFunction model.url then model.url() else model.url
    aURL = url.split '/'

    options.success = (model, resp, options) ->
      if aURL.length < 3
        switch method
          when "read", "create", "update"
            window.localStorage.setItem url, JSON.stringify resp
          when "delete"
            window.localStorage.removeItem url
      else
        switch method
          when "read", "create", "update"
            window.localStorage.setItem url, JSON.stringify resp
          when "delete"
            window.localStorage.removeItem url
      optionsSuccess model, resp, options unless not optionsSuccess

    console.log window.localStorage

    data = switch method
      when "read"
        coll = JSON.parse window.localStorage.getItem aURL[1]
        if aURL.length < 3 then coll else _.find coll, (mdl) -> mdl.id is aURL[2]
      when "create", "update", "delete" then model.attributes
    if data and optionsSuccess
      setTimeout (() -> optionsSuccess model, data, options), 1

  defaultSync(method, model, options)
