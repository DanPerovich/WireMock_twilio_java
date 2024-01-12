// Install the Java helper library from twilio.com/docs/java/install

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.http.*;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.BufferedHttpEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class WireMockExample {

    public static void main(String[] args) {
        WireMockServer wm = null;

        try {
            wm = new WireMockServer(wireMockConfig().dynamicPort().notifier(new ConsoleNotifier(true)));
            wm.start();

            Twilio.init("my-acct-123", "twilpass");

            // Create a custom request with an overwritten URL
            TwilioRestClient client = new TwilioRestClient.Builder("user", "pass")
                    .httpClient(new CustomHttpClient("http://localhost:" + wm.port()))
                    .build();

            wm.stubFor(post(urlPathTemplate("/{apiVersion}/Accounts/{accountSid}/Messages.json"))
                    .willReturn(okJson("{\n" +
                            "  \"account_sid\": \"my-acct-123\",\n" +
                            "  \"api_version\": \"2010-04-01\",\n" +
                            "  \"body\": \"Hi there\",\n" +
                            "  \"date_created\": \"Thu, 24 Aug 2023 05:01:45 +0000\",\n" +
                            "  \"date_sent\": \"Thu, 24 Aug 2023 05:01:45 +0000\",\n" +
                            "  \"date_updated\": \"Thu, 24 Aug 2023 05:01:45 +0000\",\n" +
                            "  \"direction\": \"outbound-api\",\n" +
                            "  \"error_code\": null,\n" +
                            "  \"error_message\": null,\n" +
                            "  \"from\": \"1234567890\",\n" +
                            "  \"num_media\": \"0\",\n" +
                            "  \"num_segments\": \"1\",\n" +
                            "  \"price\": null,\n" +
                            "  \"price_unit\": null,\n" +
                            "  \"messaging_service_sid\": \"MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\",\n" +
                            "  \"sid\": \"SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\",\n" +
                            "  \"status\": \"queued\",\n" +
                            "  \"subresource_uris\": {\n" +
                            "    \"media\": \"/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Media.json\"\n" +
                            "  },\n" +
                            "  \"to\": \"0123456789\",\n" +
                            "  \"uri\": \"/2010-04-01/Accounts/my-acct-123/Messages/Messages.json\"\n" +
                            "}")));
            Message message = Message.creator(
                            new PhoneNumber("1234567890"),
                            new PhoneNumber("0123456789"),
                            "Where's Wallace?")
                    .create(client);

            System.out.println(message.getSid());
        } finally {
            if (wm != null) {
                wm.stop();
            }
        }
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
