# Kafka Sink

The `KafkaSink<TKey, TValue>` provides the ability to write [`Event<TKey, TValue>`](../event-sources/index.md#event)
instances back to a Kafka topic.  This allows writing events back to Kafka as the final step of a pipeline.

Note that this sink is provided by the [`event-source-kafka` module](../event-sources/kafka.md) not the
`projectors-core` module so requires a dependency on the `event-source-kafka` module to be available.

## Behaviours

- Terminal
- Transforming: No
- Batching: No

## Parameters

The primary parameters are the Kafka bootstrap servers, topic and serializers for the key and value types.

Optionally you may also configure the linger milliseconds which controls how much latency the Kafka producer trades off
for improved batching of sent events.

## Example Usage

In this example we configure a sink with our desired Kafka destination and serialisers:

```java
try (KafkaSink<Integer, String> sink 
        = KafkaSink.<Integer, String>create()
                   .bootstrapServer("localhost:9092")
                   .topic("your-topic")
                   .keySerializer(IntegerSerializer.class)
                   .valueSerializer(StringSerializer.class)
                   .lingerMs(5)
                   .build()) {
    for (Event<Integer, String> input : someDataSource()) {
        sink.send(input);
    }
}
```

Here we are using the Kafka built-in `IntegerSerializer` and `StringSerializer` classes but, you can use your own custom
Kafka `Serializer` implementations if you want to support your own types.

A `lingerMs()` of 5 milliseconds means that the underlying `KafkaProducer` will wait for up to 5 milliseconds for
additional events to be received before sending them onwards to Kafka.  This trades off a small amount of latency in
favour of reducing the number of requests that are sent to the Kafka brokers.
