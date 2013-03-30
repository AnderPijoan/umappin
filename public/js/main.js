function setTemplate(url, callback) {
    $('div#actionResult').empty();
    $.get(
            url,
            function(template) {
                $('div#content').empty().html(template);
                if (callback) callback.call(this);
            }
        );
}

function updateSessionViews(username) {
    $('.loggedout').css('display', username != "" ? 'none' : 'block');
    $('.loggedin').css('display', username != "" ? 'block' : 'none').find('#username').text(username);
}

$(function() {
    // Initialize main routing namespace
    window.umappin = window.umappin || {};
    // Initialize the main router
    requirejs(['/assets/js/router.js'], function() {
        umappin.router || (umappin.router = new umappin.Router());
        Backbone.history.start();
    });
    // Check the user session
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
});
