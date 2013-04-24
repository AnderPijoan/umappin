class window.Photo extends Backbone.Model

  urlRoot: "/photos"
  defaults:
    id: null
    ownerId: null
    photoContents: null
    title: ''
    description: ''
    latitude: ''
    longitude: ''
    created: ''

  initialize: ->
    @photoContents = new Photo.Content id: @id

  @dragOverHandler: (e) ->
    e.stopPropagation()
    e.preventDefault()
    e.originalEvent.dataTransfer.dropEffect = 'copy'

  @dropHandler: (e, infoTarget) ->
    e.stopPropagation()
    e.preventDefault()
    files = e.originalEvent.dataTransfer.files
    output = []

    for f in files
      do (f) ->
        if not f.type.match 'image.*' then return

        output.push """
                    <li>
                    <strong>#{escape f.name}</strong>#{f.type or 'n/a'} - #{f.size} bytes
                    last modified: #{if f.lastModifiedDate then f.lastModifiedDate.toLocaleDateString() else 'n/a'}
                    </li>
                    """
        evt = e
        reader = new FileReader()
        reader2 = new FileReader()

        reader.onload = (e) ->
          $(evt.target).html "<img class='thumb' src='#{e.target.result}' title='#{escape f.name}'/>"
          reader2.readAsDataURL f
        reader.readAsDataURL f

        reader2.onload = (e) ->
          result = e.target.result
          id = null #TODO reference here to the existing id if any(would be user Id  enough for that?)
          if id?
            console.log "updating photo with id #{id}"
            url = "/photos/#{id}/content/"
          else
            console.log 'creating a new photo'
            url = "/photos/"
          jsonContent =
            name: "photoname"
            date: new Date()
            ownerId: Account.profile.id
            content: result
          $.ajax(
            type: "POST"
            url: url
            dataType: "json"
            contentType: "application/json; charset=utf-8"
            data: JSON.stringify jsonContent
            done: (resp) ->
              console.log resp
            fail: (resp) ->
              console.log "Request was unsuccessful:\n#{resp}"
          )

        reader.read

      $('#' + infoTarget).html "<ul>#{output}</ul>"