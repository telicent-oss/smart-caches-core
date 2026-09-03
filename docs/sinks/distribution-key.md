# Distribution Key Sink

The `DistributionKeySink` is a forwarding sink that sets the Distribution ID as the events Kafka message key, as
required by the *Data Partitioning in Kafka* section of the Core Data Management design.  Events sent to a topic are
partitioned on a hash of their key, so keying by Distribution ID means that all events carrying data for a given
distribution land on the same partition and are therefore processed in-order.

Note that this sink is provided by the [`event-sources-core` module](../event-sources/index.md#sinks) not the
`projectors-core` module so requires a dependency on the `event-sources-core` module to be available.

## Behaviours

- Forwarding
- Transforming: Yes
- Batching: No

## The key contract

A Distribution ID message key is **always the UTF-8 encoding of a string**.  A key written by a producer configured
with `BytesSerializer` is therefore byte for byte identical to one written with `StringSerializer`, which is why
adopting message keys does not require any pipeline to change its configured serializers.

Two key formats are supported, selected by the `DistributionKeyStrategy`:

| Strategy | Key | Notes |
| --- | --- | --- |
| `distribution-id` (default) | `<distributionId>` | Guarantees in-order processing per distribution.  Not compatible with Kafka log compaction, which would eventually reduce each distribution to its single most recent event. |
| `distribution-id-and-uuid` | `<distributionId>/<uuid>` | Every event has a unique key so log compaction, and therefore deletion of a distribution's events by tombstoning, stays viable.  See the caveat below. |

> **NB** With Kafka's default partitioner each `distribution-id-and-uuid` key hashes independently, so that strategy
> on its own does **not** give the in-order guarantee.  Where both properties are needed, configure producers with the
> [`DistributionIdPartitioner`](#distributionidpartitioner).

The strategy is configured by the `DISTRIBUTION_KEY_STRATEGY` environment variable, or the
`--distribution-key-strategy` CLI option where a command mixes in `DistributionKeyOptions`.  Writing keys can be
disabled entirely with `DISTRIBUTION_KEY_ENABLED=false` or `--no-distribution-key`, which is intended for staged
rollout, it does not affect the `Distribution-Id` header.

## Reading the Distribution ID

On the read side **the message key is authoritative and the `Distribution-Id` header is the fallback**.  Services
**SHOULD NOT** call `event.lastHeader(TelicentHeaders.DISTRIBUTION_ID)` directly, they should use:

```java
String distributionId = KafkaDistributionKeys.resolve(event);
```

This resolves in the following order:

1. Decode the message key as strict UTF-8.  A key that is not valid UTF-8, or that is of a type that conveys no
   Distribution ID such as the `UUID` keys used by the lifecycle and action tracker topics, is skipped.
2. Strip a trailing `/<uuid>` if present, so that both key formats above resolve to the same Distribution ID.  Note
   that Distribution IDs are frequently URIs and routinely contain `/` themselves, so only a final segment that
   actually parses as a UUID is treated as a uniqueness suffix.
3. Fall back to the `Distribution-Id` header, which is how events produced by pipelines predating message keys
   continue to resolve.

`KafkaDistributionKeys` lives in the `event-source-kafka` module and understands Kafka's `Bytes` key type.  Code that
cannot depend on that module can use `DistributionIds` from `event-sources-core`, which handles `byte[]` and
`CharSequence` keys.

## Parameters

| Parameter | Required | Default | Purpose |
| --- | --- | --- | --- |
| `keyEncoder` | Yes | - | Converts the generated key string into the pipelines key type.  Use `KafkaDistributionKeys.bytesKeySink()` or `stringKeySink()` to get a builder with this already set. |
| `resolver` | No | `DistributionIds::resolve` | Resolves the Distribution ID from an event.  The pre-wired builders set the `Bytes` aware resolver. |
| `strategy` | No | `DISTRIBUTION_ID` | Key format, see above. |
| `enabled` | No | `true` | When `false` events are forwarded unmodified. |
| `requireDistributionId` | No | `false` | When `true` an event with no Distribution ID throws `IllegalStateException` rather than being forwarded unmodified. |
| `backfillHeader` | No | `true` | Adds the `Distribution-Id` header when it is missing, so an event that gains a key carries the header too. |

## Example Usage

```java
try (DistributionKeySink<Bytes, ExtractedDocument> sink
        = KafkaDistributionKeys.<ExtractedDocument>bytesKeySink()
                               .destination(KafkaSink.<Bytes, ExtractedDocument>create()
                                                     .bootstrapServers(bootstrapServers)
                                                     .topic(outputTopic)
                                                     .keySerializer(BytesSerializer.class)
                                                     .valueSerializer(ExtractedDocumentSerializer.class))
                               .build()) {
    // Events are keyed by their Distribution ID before reaching Kafka
    sink.send(event);
}
```

## DistributionIdPartitioner

`DistributionIdPartitioner` is a Kafka `Partitioner` that hashes only the portion of the key before the uniqueness
suffix, so the `distribution-id-and-uuid` strategy keeps per-distribution partition affinity.  Enable it in a
producers configuration:

```
partitioner.class=io.telicent.smart.cache.sources.kafka.DistributionIdPartitioner
```

Its hashing is deliberately identical to Kafka's built in partitioning, murmur2 of the key bytes modulo the partition
count, so a `distribution-id` keyed event is assigned the same partition whether or not the partitioner is installed.
It can therefore be rolled out to a topics producers without reshuffling existing distributions.  Records with no
key, or whose key conveys no Distribution ID, are assigned a random partition as the default partitioner does.
