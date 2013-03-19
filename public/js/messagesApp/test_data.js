
_.templateSettings.variable = "rc";

//Messages
/*
var discHead1 = new messagesApp.DiscussionHeader({
    "id":123456,
    "subject": "This subject",
    "message_number":5, //number of total messages
    "unread_messages":2, //number of unread messages, 0 if none
    "user":{
        "id":123, //creator user id
        "name":"Jon Doe",
        "user_pic":"http://" //blank for now

    }
});


var discHead2 = new messagesApp.DiscussionHeader({
	
    "id":64321,
    "subject": "This second subject",
    "message_number":3, //number of total messages
    "unread_messages":0,
    "user":{
        "id":124, //creator user id
        "name":"Pepe Gotera",
        "user_pic":"http://" //blank for now
         
    }
        
});

messagesApp.DiscussionHeaders.add(discHead1);
messagesApp.DiscussionHeaders.add(discHead2);*/

var disc1 = new messagesApp.Discussion({
            "id": 123456,
            "subject": "Greeting",
            "unread_messages":0,
            "messages":["Hello", "How are you?", "Well, thank you! What about you?", "I'm ok"], //array of messages (String)
            "to_friends":true,
            "receivers":["Pepe Gotera"]
});

var disc2 = new messagesApp.Discussion({
            "id": 64321,
            "subject": "Master branch",
            "unread_messages":1,
            "messages":["Do you know what happened with the master branch?"], //array of messages (String)
            "to_friends":true,
            "receivers":["Jon Doe"]
});
