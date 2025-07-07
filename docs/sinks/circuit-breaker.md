# Circuit Breaker Sink

A `CircuitBreakerSink` allows for the injection of a circuit breaker into a pipeline, when the circuit breaker is in the
`CLOSED` state (it's default) then it behaves as a no-op sink merely forwarding items received onto the destination
sink.  

However, when placed into the `OPEN` state via the `setState()` method, it stops forwarding items and instead places
them into a bounded queue.  If the bounded queues maximum size is reached then it will block when the next item is sent
to it.  When the circuit breaker returns to the `CLOSED` state it forwards on any previously queued items to the
destination sink prior to accepting any further items.

Circuit Breakers are typically used in pipelines where applications need to respond to external signals/state changes
e.g. lack of availability of a required external service, ongoing data maintenance operations etc.  By opening the
circuit breaker an application can effectively pause its pipeline until such time as it is safe to resume it.  Bounding
the queue size ensures that the pipeline does not continuously run while the circuit breaker is open while still
allowing some processing to continue if the pause is short-lived.

## Behaviours

- Forwarding
- Transforming: No
- Batching: No

## Parameters

This sink takes a destination `Sink<T>`, an initial state for the circuit breaker - `OPEN` or `CLOSED` - and a maximum
queue size for when the circuit breaker is `OPEN`.

## Example Usage

In this example we create a `CircuitBreakerSink` which starts in the `OPEN` state, i.e. it does not forward on items
initially, and a max queue size of `1000`.  On a background thread we send items to our pipeline, and on our foreground
thread we wait for some external resource to be ready prior to setting the circuit breaking to `CLOSED` to allow it to
forward items to our actual destination.

```java
try (CircuitBreakerSink<Integer> sink 
        = CircuitBreakerSink.<Integer>create()
                   .destination(createDestination())
                   .opened()
                   .queueSize(1_000)
                   .build()) {
    // Send items to the sink on a background thread
    // Do this on a background thread otherwise sink.send() will block once the max queue size is reached
    executor.submit(() -> {
        for (Integer input : someDataSource()) {
            sink.send(input);
        }
    }

    // On foreground thread wait for some external resource to be ready
    while (!externalResource.isReady()) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {

        }
    }

    // Once that's ready set the circuit breaker to CLOSED so it starts forwarding items
    // Any queued items are drained when this happens
    sink.setState(CircuitBreaker.State.CLOSED);
}
```
