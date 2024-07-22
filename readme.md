# SpringBoot Observability

A set up to demonstrate micometer for observability in SpringBoot java applications as well as the usage of `grafana/otel-lgtm` image for local testing.

More details [here](https://medium.com/@chalise-arun/jvm-applications-observability-with-open-telemetry-and-micrometer-6df8502c8ac1)

To run the demo:
- Start claimservice: `cd claimservice ./gradlew bootRun`
- Start pamentservice: `cd paymentservice ./gradlew bootRun`

Submit a claim several times to generate metrics and traces:
```
POST localhost:8080/claim
Content-Type: application/json

{
  "type": "FLOOD_DAMAGE",
  "amount": 300.50,
  "email": "testuser@email.com"

}
```

Metrics and traces can now be viewed via grafana at localhost:3000

