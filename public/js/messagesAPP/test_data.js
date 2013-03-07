
_.templateSettings.variable = "rc";

//Messages
var disc1 = new messagesApp.Discussion({
    "id":"123456",
    "subject": "This subject",
    "message_number":"5", //number of total messages
    "unread_messages":"2", //number of unread messages, 0 if none
    "user":{
        "id":"123", //creator user id
        "name":"Jon Doe",
        "user_pic":"http://" //blank for now

    }
});

var disc2 = new messagesApp.Discussion({
	
    "id":"64321",
    "subject": "This second subject",
    "message_number":"3", //number of total messages
    "unread_messages":"0",
    "user":{
        "id":"124", //creator user id
        "name":"Pepe Gotera",
        "user_pic":"http://" //blank for now
         
    }
        
})

messagesApp.Discussions.add(disc1);

messagesApp.Discussions.add(disc2);