function setTemplate(url) {
    $('div#actionResult').css('display','none');
    $('div#content').empty();
    var templateGet = $.get( url );
    templateGet.done(function( data ) {
        $.get(
            data,
            function(template) {
                $('div#content').empty().html(template);
                sessionStorage.setItem("lastTemplate", url);
            }
        )
    });
    templateGet.error(function( data ) {
        $('div#actionResult').css('display','block').empty().html(
            "<div class='alert alert-error'>" + data.responseText + "</div>");
    });
}

function updateSessionViews(username) {
    $('.loggedout').css('display', username != "" ? 'none' : 'block');
    $('.loggedin').css('display', username != "" ? 'block' : 'none').find('#username').text(username);
}

function doLogout() {
    setTemplate('/logout');
    sessionStorage.removeItem("user");
    updateSessionViews("");
}

function home() {
    sessionStorage.setItem("lastTemplate", "/home");
    window.location.href = "/";
}

$(function() {

    var json = sessionStorage.getItem("user");
    if (json != null && json != undefined && json != "") {
        var usr = JSON.parse(json);
        updateSessionViews(usr.name);
    } else {
        var sessionRequest = $.get("/sessionuser");
        sessionRequest.done(
            function(data) {
                updateSessionViews(data.name);
                sessionStorage.setItem("user", JSON.stringify(data));
            }
        );
        updateSessionViews("");
    }
    var lastTemplate = sessionStorage.getItem("lastTemplate");
    if (lastTemplate == null || lastTemplate == "")
        lastTemplate = "/home";
    setTemplate(lastTemplate);

});
