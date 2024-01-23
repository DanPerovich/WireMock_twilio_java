// Install the Java helper library from twilio.com/docs/java/install

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.http.*;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.HttpVersion;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.client.utils.HttpClientUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class WireMockExample {

    public static void main(String[] args) {
        // Load .evn file
        Dotenv dotenv = Dotenv.load();

        // Initialize Twilio with your credentials
        Twilio.init(dotenv.get("TWILIO_ACCOUNT_SID"), dotenv.get("TWILIO_AUTH_TOKEN"));

        // Create a custom request with an overwritten URL
        TwilioRestClient customClient = new TwilioRestClient.Builder(
                dotenv.get("TWILIO_ACCOUNT_SID"),
                dotenv.get("TWILIO_AUTH_TOKEN"))
                .httpClient(new CustomHttpClient("https://twilio.wiremockapi.cloud"))
                .build();

        MessageCreator message = Message.creator(
                new PhoneNumber(dotenv.get("TWILIO_TO_NUMBER")),
                new PhoneNumber(dotenv.get("TWILIO_FROM_NUMBER")),
                "Where's Wallace?");
        message.create();

        //System.out.println(message.getSid());
    }
}

class CustomHttpClient extends NetworkHttpClient {

    private final String baseUrl;

    public CustomHttpClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Response makeRequest(Request request) {
        String url = request
                .constructURL()
                .toString()
                .replace("https://api.twilio.com", baseUrl);

        Request newRequest = new Request(
                request.getMethod(),
                url
        );

        for (Map.Entry<String, List<String>> entry: request.getQueryParams().entrySet()) {
            for (String value: entry.getValue()) {
                newRequest.addQueryParam(entry.getKey(), value);
            }
        }
        for (Map.Entry<String, List<String>> entry: request.getPostParams().entrySet()) {
            for (String value: entry.getValue()) {
                newRequest.addPostParam(entry.getKey(), value);
            }
        }

        return super.makeRequest(newRequest);
    }
}
