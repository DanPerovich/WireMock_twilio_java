// Install the Java helper library from twilio.com/docs/java/install

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

import io.github.cdimascio.dotenv.Dotenv;

public class Example {

    public static void main(String[] args) {
        //Dotenv dotenv = null;
        Dotenv dotenv = Dotenv.load();
        //dotenv.get("MY_ENV_VAR1")
        //dotenv = Dotenv.configure().load();

        //Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Twilio.init(dotenv.get("TWILIO_ACCOUNT_SID"), dotenv.get("TWILIO_AUTH_TOKEN"));
        Message message = Message.creator(
                        new com.twilio.type.PhoneNumber(dotenv.get("TWILIO_TO_NUMBER")),
                        new com.twilio.type.PhoneNumber(dotenv.get("TWILIO_FROM_NUMBER")),
                        "Where's Wallace?")
                .create();

        System.out.println(message.getSid());
    }
}