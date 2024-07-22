package dev.example.claimservice;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.random.RandomGenerator;

@SpringBootApplication
public class ClaimserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClaimserviceApplication.class, args);
	}

}

@RestController
class ClaimController {
	private Logger logger = LoggerFactory.getLogger(ClaimController.class);
	private final ObservationRegistry observationRegistry;
	private final Counter claimCounter;
	private final Counter errorCounter;
	private final RestClient.Builder restclientBuilder;

	public ClaimController(ObservationRegistry observationRegistry, MeterRegistry meterRegistry, RestClient.Builder webClientBuilder) {
		this.observationRegistry = observationRegistry;
		this.claimCounter = Counter.builder("claim.counter")
				.description("Counts Claim Submitted")
				.tags("region", "us-east")
				.register(meterRegistry);
		this.errorCounter = Counter.builder("claim.error.counter")
				.description("Counts Errors for Claim Submitted")
				.tags("region", "us-east")
				.register(meterRegistry);
		this.restclientBuilder = webClientBuilder;
	}
	@PostMapping("/claim")
	public Response submitClaim(@RequestBody Claim claim) {
		logger.info("Received claim {}", claim);
		Observation observation = Observation.createNotStarted("submit.claim", this.observationRegistry);
		observation.lowCardinalityKeyValue("rebateType", claim.type());
		observation.highCardinalityKeyValue("claimAmount", claim.amount().toString());
		AtomicReference<String> status = new AtomicReference<>("SUCCESS");
		observation.observe(() -> {
			// Execute business logic here
			try {
				Thread.sleep(30);
				restclientBuilder.baseUrl("http://localhost:8081/payment").build().post().body(claim).retrieve();
				claimCounter.increment();
				if(RandomGenerator.getDefault().nextBoolean()) {
					throw new RuntimeException("some error");
				}
			} catch (Exception _) {
				errorCounter.increment();
				observation.highCardinalityKeyValue("error", "error here");
				status.set("ERROR");
			}
		});
        return new Response(status.get(), UUID.randomUUID().toString());
	}
}

record Response(String status, String correlationId){}
record Claim(String type, Double amount){}
