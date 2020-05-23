import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;

public class ContractTest {


    // provider name, host, and port on which the server mock (which is created from the contract) will be started.
    @Rule
    public PactProviderRule mockProvider
            = new PactProviderRule("test_provider", "localhost", 8080, this);

    // create contract for get and post method
    @Pact(consumer = "test_consumer")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("test GET")
                    .uponReceiving("GET REQUEST")
                    .path("/pact")
                    .method("GET")
                .willRespondWith()
                    .status(200)
                    .headers(headers)
                    .body("{\"condition\": true, \"name\": \"tom\"}")
                .given("test POST")
                    .uponReceiving("POST REQUEST")
                    .method("POST")
                    .headers(headers)
                    .body("{\"name\": \"Michael\"}")
                    .path("/pact")
                .willRespondWith()
                    .status(201)
                .toPact();

    }

    @Test
    @PactVerification("test_consumer")
    public void givenGet_whenSendRequest_shouldReturn200WithProperHeaderAndBody() {

        // when
        ResponseEntity<String> response = new RestTemplate()
                .getForEntity(mockProvider.getUrl() + "/pact", String.class);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().get("Content-Type").contains("application/json")).isTrue();
        assertThat(response.getBody()).contains("condition", "true", "name", "tom");
    }
}
