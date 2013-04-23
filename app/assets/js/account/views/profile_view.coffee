window.Account or= {}

_.templateSettings.variable = 'rc'

class window.Account.ProfileView extends Backbone.View
  readonly: true

  events:
    "click button":  "update"
    "click dropProfilePicture":  "update"
    "dragover dropProfilePicture":  "handleDragOver"
    "drop dropProfilePicture":  "handleFileSelect"

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
    e.dataTransfer.dropEffect = 'copy'

  handleFileSelect: (evt) ->
    evt.stopPropagation()
    evt.preventDefault()
    files = evt.dataTransfer.files
    output = [];
    for f in files do (f) ->
      if !f.type.match('image.*') then continue
      output.push """ <li><strong>', escape(f.name), '</strong> (', f.type || 'n/a', ') - ',
      f.size, ' bytes, last modified: ',
      if f.lastModifiedDate then f.lastModifiedDate.toLocaleDateString() else 'n/a',
      '</li>')                                 files = evt.dataTransfer.files
                   files = evt.dataTransfer.files
    var reader = new FileReader();

    // Closure to capture the file information.
    //here we are just diplaying the image on the screen
    reader.onload = (function(theFile) {
    return function(e) {
    // Render thumbnail.
    var span = document.createElement('span');
    span.innerHTML = ['<img class="thumb" src="', e.target.result,
                      '" title="', escape(theFile.name), '"/>'].join('');
    document.getElementById('list').insertBefore(span, null);
    };
    })(f);

    // Read in the image file as a data URL.
    reader.readAsDataURL(f);

    var reader2 = new FileReader();
    reader2.readAsDataURL(f);

    //here we are uplaoading it!
    reader2.onload = (function(theFile) {
    return function(event) {
    var result = event.target.result;
    var type = theFile.type;
    var id = $('#photoContentIdAjax2').val();
    if(id){
    alert('updating photo with id ' + id);
      url = '@routes.PhotosREST.updatePhoto("%s")'.replace("%s", id);
    }else{
    alert('creating a new photo');
    url = '@routes.PhotosREST.newPhoto()';
    }

    var jsonContent = JSON.parse($('#json_content_metadata_ajax').val());

    //add the base64content
    jsonContent.content = event.target.result;



    $.ajax({
      type: "POST",
      url: url + addOptionalUserQueryString(),
      dataType: "json",
      contentType: "application/json; charset=utf-8",
    //data: JSON.stringify({"content": result, "type": type})
    data: JSON.stringify(jsonContent)
    }).done(function( msg ) {
    document.getElementById("returned_content").innerHTML = JSON.stringify(msg);
    }).fail(function( msg ) {
    document.getElementById("returned_content").innerHTML = "Request was unsuccessful:\n" + JSON.stringify(msg);
    });

    };
    })(f);


    reader.read


    }
    document.getElementById('list').innerHTML = '<ul>' + output.join('') + '</ul>';
