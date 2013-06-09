package utils;

import akka.actor.Props;
import akka.actor.UntypedActor;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import controllers.MorphiaObject;
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
     * Gets from the DB the photo and saves multiple resized copies into the DB. It just gets a string
     * as an input to be easily sent along the network, in case this actor gets remoted in another iteration.
     *
     * The list of sizes is {@code Photo.Content.RESIZE_SIZES} (in pixels),and a given size is discarded
     * if is larger than the max side of the photo.
     * The photo is rescaled in order to have the max side matching the given size, and the other side is kept
     * proportional.
     *
     * The actor assumes that the photo contains only the original contant (i.e. photoContent[0]), and the other potential
     * contents were already removed. So, it adds additional contents (photoContent[1], photoContent[2]...)
     * as far as the size has to be reduced, i.e. the size is
     *
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

            for (Photo.Content c: contents){
                Logger.info(c.getId().toString());
            }

            //element "0" in the list of contents is the original photo
            Photo.Content originalContent = contents.get(0);

            byte[] originalBytes = originalContent.getFileBytes();
            InputStream in = new ByteArrayInputStream(originalBytes);
            BufferedImage originalImage = ImageIO.read(in);

            int height = originalImage.getHeight();
            int width = originalImage.getWidth();
            int maxSide = Math.max(height, width);


            //we just determined the size of the original content for free,
            // so if it wasn't already up to date in the original image, we
            // get the chance to align it to the real values here
            if(originalContent.getxSize() != width || originalContent.getySize() != height){
                //so save this info in the content
                originalContent.setxSize(width);
                originalContent.setySize(height);
                originalContent.save();
            }

            //here we are supposing that the photo contains only the photoContent[0]
            for(Integer size : Photo.Content.RESIZE_SIZES){
                //do not resize the image if it is already smaller than the target size
                if(size > maxSide){
                    continue;
                }

                BufferedImage resized = Scalr.resize(originalImage, Scalr.Method.QUALITY, size, size);

                Photo.Content c = new Photo.Content();
                c.setxSize(resized.getWidth());
                c.setySize(resized.getHeight());
                c.setMimeType("image/" + Photo.Content.IMAGE_RESIZE_TYPE);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resized, Photo.Content.IMAGE_RESIZE_TYPE, baos);

                c.setFileBytes(baos.toByteArray());

                baos.flush();
                baos.close();

                c.save();

                contents.add(c);

                Logger.info(ImageResizerActor.class.getName()
                        + ": created resized copy for photo "
                        + photo.getId().toString()
                        + " of " + size + "px"
                );
            }

            //retrieve from the DB the last, most update state of the photo, to help prevent concurrent updates
            photo = Photo.findById(photo.getId());
            //...and let it point to the new contents
            photo.setPhotoContents(contents);
            photo.save();

            //the following doesn't work because the photoContents are lazy references
//            //update just the content of the Photo object, to avoid conflicts with other concurrent updates on other fields
//            Datastore ds = MorphiaObject.datastore;
//
//            Query<Photo> updateQuery = ds.createQuery(Photo.class)
//                    .field("_id").equal(photo.getId());
//
//            UpdateOperations<Photo> ops = ds.createUpdateOperations(Photo.class).set(Photo.PHOTO_CONTENTS, contents);
//            //note that 'update' gives a compile time error (overloaded
//            // 'update' methods are not correctly resolved by the compiler)
//            // so we use the 'updateFirst' version, that is fine, given that we
//            // expect one result only
//
//            ds.updateFirst(updateQuery, ops);

            Logger.info(ImageResizerActor.class.getName() + ": persisted all resized copies of photo " + photo.getId().toString());

            originalImage.flush();

        } else {
            unhandled(photoId);
        }

        getContext().stop(getSelf());

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

    public static final List<Integer> RESIZE_SIZES = Arrays.asList(50, 150, 500, 1000);

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
