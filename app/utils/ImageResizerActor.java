package utils;

import akka.actor.Props;
import akka.actor.UntypedActor;
import models.Photo;
import play.Logger;

public class ImageResizerActor extends UntypedActor {

    @Override
    public void onReceive(Object photo) {
        if (photo instanceof Photo && photo != null){
            Logger.info("actor:received photo " + ((Photo) photo).getId().toString());
            getContext().stop(getSelf());
        } else unhandled(photo);
    }


    public static Props mkProps() {
        return new Props(ImageResizerActor.class);
    }

    public ImageResizerActor(){}
}
