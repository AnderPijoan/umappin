package controllers;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.Routes;
//import views.html.userMessage.form;
import views.html.foo;

import models.Message;
/**
 * User: a.digangi
 * Date: 24/02/13
 * Time: 13.50
 */
public class UserMessage extends Controller {
    public static Result post(){

        models.Message message = Form.form(models.Message.class).bindFromRequest().get();
        message.save();
        //String url = Router.reverse("controllers.RestMessages.all").url;
        //redirect(url);
        //return redirect(routes.controllers.RestMessages);
        return ok(views.html.userMessageForm.render(message));
        //return ok(views.html.foo.render());
    }

    public static Result form(){
        models.Message message = Form.form(models.Message.class).bindFromRequest().get();
        //Content html = views.html.UserMessage.form;
        return ok(views.html.userMessageForm.render(message));

        //return redirect(routes.controllers.RestMessages);
    }

}
