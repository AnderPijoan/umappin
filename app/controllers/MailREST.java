package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.Mail;
import com.feth.play.module.mail.Mailer.Mail.Body;
import org.codehaus.jackson.JsonNode;
import play.Configuration;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

public class MailREST extends Controller {

    //@Restrict(@Group(Application.USER_ROLE))
    public static Result sendInviteMail() {
        JsonNode json = request().body().asJson();
        final String email = json.findPath("email").asText();
        final String subject = json.findPath("subject").asText();
        final String bodytext = json.findPath("text").asText();
        final String bodyhtml = json.findPath("html").asText();
        final Mail verifyMail = new Mail(
            subject,
            new Body(bodytext, bodyhtml),
            new String[] { email }
        );
        Configuration mailConfig = Play.application().configuration()
                                        .getConfig("play-authenticate")
                                        .getConfig("password")
                                        .getConfig("mail");
        Mailer mailer = Mailer.getCustomMailer(mailConfig);
        mailer.sendMail(verifyMail);
        return ok();
    }

}
