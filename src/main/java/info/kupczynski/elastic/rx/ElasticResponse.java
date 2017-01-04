package info.kupczynski.elastic.rx;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface ElasticResponse {
    Integer code();
    String reason();
    Optional<String> body();
}
