package io.github.brfreitas.elastic.rx;

import io.reactivex.Single;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

public class ElasticRxClient implements Closeable {

    public static final String PATH_ROOT = "/";
    private final RestClient restClient;

    public ElasticRxClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public static ElasticRxClient fromUrl(URL url) {
        RestClientBuilder builder = RestClient.builder(
                new HttpHost(url.getHost(), url.getPort(), url.getProtocol())
        );
        if (url.getUserInfo() != null) {
            builder.setHttpClientConfigCallback(
                    (HttpAsyncClientBuilder httpClientBuilder) -> {
                        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                        credentialsProvider.setCredentials(AuthScope.ANY,
                                new UsernamePasswordCredentials(url.getUserInfo()));
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
            );
        }
        return new ElasticRxClient(builder.build());
    }

    public static ElasticRxClient fromUrl(String url) {
        try {
            return fromUrl(new URL(url));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Single<ElasticResponse> request(Method method, String path) {
        return Single.<ElasticResponse>create(emitter -> {
            ResponseListener listener = new RxResponseListener(emitter);
            restClient.performRequestAsync(method.toString(),
                    ensureAbsolute(path),
                    Collections.emptyMap(),
                    listener);
        });
    }

    public Single<ElasticResponse> request(Method method, String path, String body) {
        Single<String> asyncBody = Single.defer(() -> {
            return Single.just(body);
        });
        return request(method, path, asyncBody);
    }

    public Single<ElasticResponse> request(Method method, String path, Single<String> asyncBody) {
        return asyncBody.flatMap(body -> {

            StringEntity entity = new StringEntity(body);
            entity.setContentType(ContentType.APPLICATION_JSON.toString());

            return Single.<ElasticResponse>create(emitter -> {
                ResponseListener listener = new RxResponseListener(emitter);
                restClient.performRequestAsync(method.toString(),
                        ensureAbsolute(path),
                        Collections.emptyMap(),
                        entity,
                        listener);
            });
        });
    }

    private String ensureAbsolute(String path) {
        if (!path.startsWith(PATH_ROOT)) {
            return PATH_ROOT + path;
        }
        return path;
    }

    @Override
    public void close() throws IOException {
        restClient.close();
    }
}
