package info.kupczynski.elastic.rx;

import io.reactivex.SingleEmitter;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;

import java.io.IOException;
import java.util.Optional;

class RxResponseListener implements ResponseListener {

    private final SingleEmitter<ElasticResponse> emitter;

    public RxResponseListener(SingleEmitter<ElasticResponse> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void onSuccess(Response response) {
        ImmutableElasticResponse.Builder builder = ImmutableElasticResponse.builder()
                .code(response.getStatusLine().getStatusCode())
                .reason(response.getStatusLine().getReasonPhrase());

        try {
            String body = EntityUtils.toString(response.getEntity());
            builder.body(Optional.ofNullable(body));

            emitter.onSuccess(builder.build());
        } catch (IOException e) {
            emitter.onError(e);
        }
    }

    @Override
    public void onFailure(Exception exception) {
        emitter.onError(exception);
    }
}
