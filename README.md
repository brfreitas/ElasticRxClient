Elastic Rx Client
=================

A little wrapper over the official [Elastic Low-Level Java REST Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/index.html).

It aims to provide asynchronous requests via RxJava.

See the [example](./src/test/java/info/kupczynski/elastic/rx/ElasticRxClientExampleIntegrationTest.java)

```java
ElasticResponse indexCreated = client.request(
        Method.PUT,
        SHAKESPEARE_INDEX_NAME,
        SHAKESPEARE_INDEX_DEFINITION
).blockingGet();

assertThat(indexCreated.code(), is(200));
assertThat(indexCreated.body(), is(Optional.of("{\"acknowledged\":true,\"shards_acknowledged\":true}")));

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
assertThat(docIndexed.body().get(), containsString("\"created\":true"));


String query = "{\"query\": {\"match_all\": {}}}";
ElasticResponse searchResult = client.request(Method.GET, SHAKESPEARE_INDEX_NAME + "/_search", query)
        .blockingGet();

assertThat(searchResult.code(), is(200));
assertThat(searchResult.body().get(), containsString("\"hits\":{\"total\":1"));
assertThat(searchResult.body().get(), containsString("\"line_id\":55783"));
```


## Copyright

   Copyright 2017 Igor Kupczyński
   
   [Licensed under the Apache License](./LICENSE.txt)
   
   ```
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this project except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   ```
