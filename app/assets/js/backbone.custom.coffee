defaultSync = Backbone.sync

Backbone.sync = (method, model, options) ->
  if typeof(window.localStorage) != 'undefined'
    optionsSuccess = options.success
    url = if $.isFunction model.url then model.url() else model.url
    aURL = url.split '/'

    options.success = (model, resp, options) ->      
      if method is 'read' and not resp.id
        window.localStorage.setItem url, JSON.stringify resp 
      else
        coll = (JSON.parse window.localStorage.getItem "/#{aURL[1]}") or []
        mdl = _.find coll, (mdl) -> mdl.id is resp.id
        switch method
          when "read", "create", "update"
            if mdl then coll[coll.indexOf mdl] = resp else coll.push resp           
          when "delete"
            coll = _.without coll, mdl
        window.localStorage.setItem "/#{aURL[1]}", JSON.stringify coll
      optionsSuccess model, resp, options unless not optionsSuccess

    console.log window.localStorage

    data = switch method
      when "read"
        coll = JSON.parse window.localStorage.getItem "/#{aURL[1]}"
        if not model.get "id" then coll else _.find coll, (mdl) -> mdl.id is model.get "id"
      when "create", "update", "delete" then model.attributes
      
    if data and optionsSuccess
      setTimeout (() -> optionsSuccess model, data, options), 1

  defaultSync(method, model, options)
