package io.github.brfreitas.elastic.rx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assume.assumeTrue;

public class ElasticRxClientExampleIntegrationTest {

    public static final String ELASTICSEARCH_TEST_URL = "ELASTICSEARCH_TEST_URL";

    public static final String SHAKESPEARE_INDEX_NAME = "shakespeare";
    public static final String SHAKESPEARE_INDEX_DEFINITION = "{\n" +
            "  \"mappings\": {\n" +
            "    \"_doc\": {\n" +
            "      \"properties\": {\n" +
            "        \"speaker\": {\n" +
            "          \"type\": \"keyword\"\n" +
            "        },\n" +
            "        \"play_name\": {\n" +
            "          \"type\": \"keyword\"\n" +
            "        },\n" +
            "        \"line_id\": {\n" +
            "          \"type\": \"integer\"\n" +
            "        },\n" +
            "        \"line_number\": {\n" +
            "          \"type\": \"keyword\"\n" +
            "        },\n" +
            "        \"speech_number\": {\n" +
            "          \"type\": \"integer\"\n" +
            "        },\n" +
            "        \"text_entry\": {\n" +
            "          \"type\": \"text\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
    public static final String SHAKESPEARE_TYPE_LINE = "_doc";

    private ElasticRxClient client;

    @BeforeEach
    void setUp() throws MalformedURLException {
        String elasticUrl = System.getenv(ELASTICSEARCH_TEST_URL);
        assumeTrue(ELASTICSEARCH_TEST_URL + " is set", elasticUrl != null);
        client = ElasticRxClient.fromUrl(elasticUrl);
        client.request(Method.DELETE, "*").blockingGet();
    }

    @Test
    @DisplayName("Create index, put a document and retrieve it")
    void example() {
        ElasticResponse indexCreated = client.request(
                Method.PUT,
                SHAKESPEARE_INDEX_NAME,
                SHAKESPEARE_INDEX_DEFINITION
        ).blockingGet();

        assertThat(indexCreated.code(), is(200));
        assertThat(indexCreated.body(), is(Optional.of("{\"acknowledged\":true,\"shards_acknowledged\":true,\"index\":\"shakespeare\"}")));

        String aDoc = "{" +
                "\"line_id\":55783," +
                "\"play_name\":\"macbeth\"," +
                "\"speech_number\":1," +
                "\"line_number\":\"1.1.1\"," +
                "\"speaker\":\"First Witch\"," +
                "\"text_entry\":\"When shall we three meet again\"" +
                "}";
        ElasticResponse docIndexed = client.request(Method.POST,
                SHAKESPEARE_INDEX_NAME + "/" + SHAKESPEARE_TYPE_LINE + "?refresh=wait_for",
                aDoc).blockingGet();

        assertThat(docIndexed.code(), is(201));
        assertThat(docIndexed.body().get(), containsString("\"result\":\"created\""));


        String query = "{\"query\": {\"match_all\": {}}}";
        ElasticResponse searchResult = client
                .request(Method.GET, SHAKESPEARE_INDEX_NAME + "/_search", query)
                .blockingGet();

        assertThat(searchResult.code(), is(200));
        assertThat(searchResult.body().get(), containsString("\"hits\":{\"total\":1"));
        assertThat(searchResult.body().get(), containsString("\"line_id\":55783"));
    }

}
