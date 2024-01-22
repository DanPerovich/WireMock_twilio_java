// Install the Java helper library from twilio.com/docs/java/install

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;

import io.github.cdimascio.dotenv.Dotenv;

public class Example {

    public static void main(String[] args) {
        //Dotenv dotenv = null;
        Dotenv dotenv = Dotenv.load();
        //dotenv.get("MY_ENV_VAR1")
        //dotenv = Dotenv.configure().load();

        //Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Twilio.init(dotenv.get("TWILIO_ACCOUNT_SID"), dotenv.get("TWILIO_AUTH_TOKEN"));

        MessageCreator message = Message.creator(
                        new PhoneNumber(dotenv.get("TWILIO_TO_NUMBER")),
                        new PhoneNumber(dotenv.get("TWILIO_FROM_NUMBER")),
                        "Where's Wallace?");
        message.create();

        //System.out.println(message.getSid());
    }
}