window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.ProfileView extends Backbone.View
  readonly: true

  events:
    "click button":  "update"
    #"click #dropProfilePicture":  "update"
    "dragover #dropProfilePicture":  "handleDragOver"
    "drop #dropProfilePicture":  "handleFileSelect"

  el: "form#profile-form"

  template: _.template $('#userprofile-template').html()

  initialize: ->
    @readonly = @options.readonly
    @listenTo @model, 'reset change', @render

  render: ->
    data = @model.attributes
    #data.readonly = @readonly
    $(@el).html @template { data, readonly: @readonly }
    $(@el).find('input[type=text], textarea').attr('readonly', @readonly)
    @

  validate: ->
    name = @$el.find('#profile-name').val()
    email = @$el.find('#profile-email').val()
    name != '' and email.match /^\w.+@\w.+\.\w.+$/g
  update: ->
    if !@readonly
      if @validate
        @model.save
          name: @$el.find('#profile-name').val()
          email: @$el.find('#profile-email').val()
          firstName: @$el.find('#profile-firstname').val()
          lastName: @$el.find('#profile-lastname').val()
      else
        $('#actionResult').css('display','block').empty().html(
          "<div class='alert alert-error'>" + data.responseText + "</div>")

  handleDragOver: (e) ->
    e.stopPropagation()
    e.preventDefault()
    e.originalEvent.dataTransfer.dropEffect = 'copy'

  handleFileSelect: (evt) ->
    evt.stopPropagation()
    evt.preventDefault()
    files = evt.originalEvent.dataTransfer.files
    output = []
    
    for f in files 
      do (f) ->    
        if not f.type.match 'image.*' then return

        output = """
                  <li>
                    <strong>#{escape f.name}</strong>
                    #{f.type or 'n/a'} - #{f.size} bytes
                    last modified: #{if f.lastModifiedDate then f.lastModifiedDate.toLocaleDateString() else 'n/a'}
                  </li>
                  """
        reader = new FileReader()
        reader.onload = (e) ->
          span = document.createElement 'span'
          span.innerHTML = "<img class='thumb' src='#{e.target.result}' title='#{escape f.name}'/>"
          document.getElementById('list').insertBefore span, null

        reader.readAsDataURL f

        reader2 = new FileReader()
        reader2.readAsDataURL f

        reader2.onload = (e) ->
          result = e.target.result
          id = null #TODO reference here to the existing id if any(would be user Id  enough for that?)
          if id?
            alert "updating photo with id #{id}"
            url = "/photos/#{id}/content/"
          else
            alert('creating a new photo')
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
            done: (msg) ->
              document.getElementById("returned_content").innerHTML = JSON.stringify msg
            fail: (msg) ->
              document.getElementById("returned_content").innerHTML = "Request was unsuccessful:\n#{JSON.stringify msg}"
          )

        reader.read
        
      evt.target.innerHTML = "<ul>#{output}</ul>"
