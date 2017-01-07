package info.kupczynski.elastic.rx;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public abstract class ElasticResponse {
    public abstract Integer code();
    public abstract String reason();
    public abstract Optional<String> body();

    public Boolean isSuccessful() {
        return code() >= 200 && code() < 300;
    }
}
