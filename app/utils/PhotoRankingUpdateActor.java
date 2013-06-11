package utils;

import akka.actor.Props;
import akka.actor.UntypedActor;
import com.google.code.morphia.Datastore;
import controllers.MorphiaObject;
import models.*;
import play.Logger;

import java.util.Date;
import java.util.List;

/**
 * this actor computes the ranking for each photo,
 * based on the useful/beautiful counts, the awards of the user (photo owner),
 * and recency of the photo (most recent, most awarded).
 * Likely, it doesn't perform well for many users, and might require spawning children or
 * a better execution plan (in terms of joins)
 */
public class PhotoRankingUpdateActor extends UntypedActor {

    //the time the initial default ranking takes to be reduced by exp(-1).
    //currently set to one week
    //the decay is exponential exp(-age/RANKING_DECAY)
    private static final int RANKING_DECAY = 3600 * 1000 * 24 * 7;

    @Override
    public void onReceive(Object message) throws Exception {

        List<User> users = MorphiaObject.datastore.find(User.class).asList();

        long now = new Date().getTime();

        for(User u : users){

            Logger.info("updating photo ranking for user " + u.id.toString());

            int awardPoints = getTotalPhotoPoints(u);

            //find all photos by this user
            List<Photo> photos = MorphiaObject.datastore.find(Photo.class).field(Photo.OWNER_ID).equal(u.id).asList();

            for(Photo p : photos){
                //if the photo has content, update the ranking
                if(p.getPhotoContents().size() > 0){

                    Logger.info("determining ranking for photo " + p.getId().toString() + " with current ranking " + p.getRanking());

                    //number of weeks elapsed from the creation of the metadata
                    long elapsedWeeks = - (now - p.getId().getTime()) / RANKING_DECAY;
                    int ranking = (int) (Photo.INITIAL_PHOTO_RANKING * Math.exp(elapsedWeeks));
                    ranking += p.getBeautifulCount() + p.getUsefulCount() + awardPoints;

                    //Query<Photo> updateQuery = ds.createQuery(Photo.class).field("_id").equal(p.getId());
                    //UpdateOperations<Photo> ops = ds.createUpdateOperations(Photo.class).set(Photo.RANKING, ranking);

                    if(ranking != p.getRanking()){
                        p.setRanking(ranking);
                        p.save();
                        Logger.info("new ranking updated to " + ranking);
                    }
                }
            }

        }

    }

    private int getTotalPhotoPoints(User u) {
        int awardPoints = 0;

        UserStatistics stats = UserStatistics.findByUserId(u.id.toString());

        if(stats == null) {
            return awardPoints;
        }

        List<UserStatistics.UserAwards> userAwards = stats.userAwards;


        for(UserStatistics.UserAwards a : userAwards){
            Award awardObj = Award.findById(a.award);
            if(awardObj.awardType.equals(StatisticTypes.PHOTOLIKES.toString())){

                awardPoints = awardPoints + awardObj.points;
            }
        }
        return awardPoints;
    }


    @Override
    public void preStart(){
        //for debug
        Logger.info("starting actor of class " + PhotoRankingUpdateActor.class.getName());
        super.preStart();
    }


    @Override
    public void postStop(){
        //for debug
        Logger.info("stopped actor of class " + PhotoRankingUpdateActor.class.getName());
        super.postStop();
    }

    public static Props mkProps() {
        return new Props(PhotoRankingUpdateActor.class);
    }
}
