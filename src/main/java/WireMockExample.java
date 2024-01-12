// Install the Java helper library from twilio.com/docs/java/install

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.http.*;
import com.twilio.rest.api.v2010.account.Message;
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

        Message message = Message.creator(
                        new PhoneNumber(dotenv.get("TWILIO_TO_NUMBER")),
                        new PhoneNumber(dotenv.get("TWILIO_FROM_NUMBER")),
                        "Hello from WireMock Cloud Java example.")
                .create();

        System.out.println(message.getSid());
    }
}

class CustomHttpClient extends NetworkHttpClient {

    private final String baseUrl;

    public CustomHttpClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Response makeRequest(Request request) {
        HttpMethod method = request.getMethod();
        String url = request.constructURL().toString();
        url = url.replace("https://api.twilio.com", baseUrl);
        RequestBuilder builder = RequestBuilder.create(method.toString())
                .setUri(url)
                .setVersion(HttpVersion.HTTP_1_1)
                .setCharset(StandardCharsets.UTF_8);

        if (request.requiresAuthentication()) {
            builder.addHeader(HttpHeaders.AUTHORIZATION, request.getAuthString());
        }

        for (Map.Entry<String, List<String>> entry : request.getHeaderParams().entrySet()) {
            for (String value : entry.getValue()) {
                builder.addHeader(entry.getKey(), value);
            }
        }

        if (method == HttpMethod.POST) {
            builder.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

            for (Map.Entry<String, List<String>> entry : request.getPostParams().entrySet()) {
                for (String value : entry.getValue()) {
                    builder.addParameter(entry.getKey(), value);
                }
            }
        }
        builder.addHeader(HttpHeaders.USER_AGENT, "custom-wiremock-http-client");

        HttpResponse response = null;

        try {
            response = client.execute(builder.build());
            HttpEntity entity = response.getEntity();
            return new Response(
                    // Consume the entire HTTP response before returning the stream
                    entity == null ? null : new BufferedHttpEntity(entity).getContent(),
                    response.getStatusLine().getStatusCode(),
                    response.getAllHeaders()
            );
        } catch (IOException e) {
            throw new ApiException(e.getMessage(), e);
        } finally {

            // Ensure this response is properly closed
            HttpClientUtils.closeQuietly(response);

        }
    }
}
