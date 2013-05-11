package controllers;

import play.mvc.Controller;
import play.mvc.Result;


public class Photos extends Controller {



    public static Result manager(){
        return ok(views.html.photo_manager.render());

    }

}
