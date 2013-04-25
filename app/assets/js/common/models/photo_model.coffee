class window.Photo extends Backbone.Model

  urlRoot: "/photos"
  defaults:
    id: null
    owner_id: null
    photoContents: null
    title: null
    post_content_location: null
    get_content_location: null
    latitude: null
    longitude: null
    date_created: null

  @dragOverHandler: (e) ->
    e.stopPropagation()
    e.preventDefault()
    e.originalEvent.dataTransfer.dropEffect = 'copy'

  dropHandler: (e) =>
    e.stopPropagation()
    e.preventDefault()
    files = e.originalEvent.dataTransfer.files
    output = ''

    for f in files
      do (f) =>
        if not f.type.match 'image.*' then return
        evt = e
        reader = new FileReader()
        reader2 = new FileReader()
        reader.onload = (e) => reader2.readAsDataURL f
        reader.readAsDataURL f

        reader2.onload = (e) =>
          result = e.target.result
          jsonContent =
            title: f.name
            date_created: new Date().getTime()
            owner_id: Account.profile.id
            content: result
          $.ajax(
            type: "POST"
            url: "/photos"
            dataType: "json"
            contentType: "application/json; charset=utf-8"
            data: JSON.stringify jsonContent
            success: (resp) =>
              @id = resp.id
              @.fetch success: () => @.trigger 'change'
          )

        reader.read
    