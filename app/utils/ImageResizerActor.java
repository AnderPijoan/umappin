package utils;

import akka.actor.Props;
import akka.actor.UntypedActor;
import models.Photo;
import org.bson.types.ObjectId;
import play.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

import org.imgscalr.Scalr;

/**
 * receives photo ids as messages. The type of that param must be String
 */
public class ImageResizerActor extends UntypedActor {

    /**
     * Gets from the DB the photo and saves resized copies into the DB. It just gets a string
     * as an input to be easily sent along the netwrok, in case this actor gets remoted in another iteration.
     * @param photoId A string representing a valid objectId that refers to a photo
     */
    @Override
    public void onReceive(Object photoId) throws IOException {
        if (photoId instanceof String && photoId != null){
            Logger.info(ImageResizerActor.class.getName() + ": received photo " + photoId);

            //any exception here on should just be thrown, because means that the photoId is incorrect
            ObjectId id = new ObjectId((String)photoId);
            Photo photo = Photo.findById(id);

            List<Photo.Content> contents = photo.getPhotoContents();
            //element "0" in the list of contents is the original photo
            byte[] originalBytes = contents.get(0).getFileBytes();
            InputStream in = new ByteArrayInputStream(originalBytes);
            BufferedImage originalImage = ImageIO.read(in);

            for(Integer size : Photo.Content.SIZES){
                byte[] resizedBytes = resize(originalImage, size);
                //contents.add()
                //contents.get(1).getFileBytes()
                Logger.info(ImageResizerActor.class.getName() + ": created resized copy of " + size + "px");
            }

            //now update just the content pointers!

        } else unhandled(photoId);

        getContext().stop(getSelf());

    }

    private byte[] resize(BufferedImage originalImage, Integer size) {
        return new byte[0];  //To change body of created methods use File | Settings | File Templates.
    }

    public static Props mkProps() {
        return new Props(ImageResizerActor.class);
    }

    public ImageResizerActor(){}

    @Override
    public void preStart(){
        //for debug
        Logger.info("starting actor of class " + ImageResizerActor.class.getName());
        super.preStart();
    }


    @Override
    public void postStop(){
        //for debug
        Logger.info("stopped actor of class " + ImageResizerActor.class.getName());
        super.postStop();
    }


    @Override
    public void postRestart(Throwable t){
        //for debug
        Logger.info("postRestart() actor of class " + ImageResizerActor.class.getName());
        super.postRestart(t);
    }


    public static byte[] resize(File icon) {
        try {
            BufferedImage originalImage = ImageIO.read(icon);


            int height = originalImage.getHeight();
            int width = originalImage.getWidth();

            System.out.println("h x w = " + height + " x " + width);
            //originalImage= Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, 128, 153);
            //To save with original ratio uncomment next line and comment the above.
            originalImage = Scalr.resize(originalImage, Scalr.Method.QUALITY, 128, 128);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(originalImage, "jpg", baos);
            originalImage.flush();
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            return imageInByte;
        } catch (Exception e) {
            return null;
        }


    }

    /*
        public static void main(String[] arg) throws Exception{
    	String inputFile = "C:\\Users\\a.digangi\\Desktop\\ricetta paella DSCF8427.JPG";
    	String outputFile = "C:\\Users\\a.digangi\\Desktop\\ricetta paella DSCF8427.resized.JPG";
    	File f = new File(inputFile);
    	FileOutputStream fos = new FileOutputStream(outputFile);

    	byte[] imageBytes = resize(f);

    	fos.write(imageBytes);
    	fos.close();
    	System.out.println("resized?");
    }

    public static final List<Integer> SIZES = Arrays.asList(50, 150, 500, 1000);

    public static void resizeImage(String photo){

    	//get the photo max side
    	//int maxSide = ...

    	//look up the first size that is smaller than that
    	//while(maxSide < SIZE(i)){continue}
    	//now we have our first target size
    	//indexSize = i;
    	//sizeIndex = ...

    	//for k = indexSize, k > 0, k--
    	//resize with that target
    	//resized = resize(image, SIZE(k))
    	//photo.content(k) = resized

    	//photo.save();



    }
}

    */

}
