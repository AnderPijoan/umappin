    $(document).ready(function($) {
        // Workaround for bug in mouse item selection
        $.fn.typeahead.Constructor.prototype.blur = function() {
            var that = this;
            setTimeout(function () { that.hide() }, 250);
        };

        var usersListSearch = $.get('/users/shortinfo'),
            users;
        usersListSearch.done(function(data){
            users = data;
        });

        var that = this;

        $('#user_search').typeahead({
            source: function(query, process) {
                var results = _.map(users, function(user) {
                   return user.id +'';
                });
                process(results);
            },

            matcher: function(item){
                var searchName = $('#user_search').val(),
                    currentUser = JSON.parse(sessionStorage.getItem('user'));
                    names = _.filter(users, function(user) {
                        return user.id == item;
                    });

                if(currentUser.id == item){
                    return false;
                }

                if(names[0].name.toLowerCase().indexOf(searchName.toLowerCase()) >= 0 && currentUser.id !== item){
                    return true;
                }
            },

            highlighter: function(id) {
                var user = _.find(users, function(p) {
                   return p.id == id;
                });

                if(!user.profilePicture){
                    user.profilePicture = './assets/img/140x140.gif';
                }
                return '<div class="search-item-typeahead">'+
                            '<img src="'+user.profilePicture+'">'+
                            '<span>'+user.name+'</span>'+
                        '</div>';
            },

            updater: function(id) {
                var user = _.find(users, function(p) {
                    return p.id == id;
                });
                that.setSelectedProduct(user);
                return user.name;
            }
        });

        $('#product').hide();
        this.setSelectedProduct = function(user) {
            console.log("SELECCIONADO");
            location.href = '/#wall/user/'+user.id;
        }
    })