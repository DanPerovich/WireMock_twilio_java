// Install the Java helper library from twilio.com/docs/java/install

import com.twilio.Twilio;
import com.twilio.http.HttpMethod;
import com.twilio.http.Request;
import com.twilio.http.Response;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import io.github.cdimascio.dotenv.Dotenv;

public class WireMockExample {

    public static void main(String[] args) {
        // Load .evn file
        Dotenv dotenv = Dotenv.load();

        // Initialize Twilio with your credentials
        Twilio.init(dotenv.get("TWILIO_ACCOUNT_SID"), dotenv.get("TWILIO_AUTH_TOKEN"));

        // Create a custom request with an overwritten URL
        TwilioRestClient customClient = new TwilioRestClient.Builder()
                .httpClient(new CustomHttpClient())
                .build();

        Message message = Message.creator(
                        new PhoneNumber(dotenv.get("TWILIO_TO_NUMBER")),
                        new PhoneNumber(dotenv.get("TWILIO_FROM_NUMBER")),
                        "Where's Wallace?")
                        .setClient(customClient)
                        .create();

        System.out.println(message.getSid());
    }
}

class CustomHttpClient extends com.twilio.http.TwilioRestClient.DefaultClient {

    @Override
    public Response request(Request request) {
        // You can customize the URL here before making the request
        String customUrl = "https://twilio.wiremockapi.cloud" + request.getUrl();
        request.setUrl(customUrl);

        // Call the Twilio API with the customized request
        return super.request(request);
    }
}
