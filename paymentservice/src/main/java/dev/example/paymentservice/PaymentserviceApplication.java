package dev.example.paymentservice;

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

import java.util.UUID;
import java.util.random.RandomGenerator;

@SpringBootApplication
public class PaymentserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentserviceApplication.class, args);
	}

}
@RestController
class PaymentController {
	private Logger logger = LoggerFactory.getLogger(PaymentController.class);
	private final ObservationRegistry observationRegistry;
	private final Counter myCounter;
	private final Counter errorCounter;

	public PaymentController(ObservationRegistry observationRegistry, MeterRegistry meterRegistry) {
		this.observationRegistry = observationRegistry;
		this.myCounter = Counter.builder("payment.counter")
				.description("Payment for Claim Submitted")
				.tags("region", "us-east")
				.register(meterRegistry);
		this.errorCounter = Counter.builder("payment.error.counter")
				.description("Counts Errors for Payment Submitted")
				.tags("region", "us-east")
				.register(meterRegistry);
	}
	@PostMapping("/payment")
	public Response submitClaim(@RequestBody Claim claim) {
		logger.info("Received claim for payment {}", claim);
		Observation observation = Observation.createNotStarted("submit.payment", this.observationRegistry);
		observation.lowCardinalityKeyValue("rebateType", claim.type());
		observation.highCardinalityKeyValue("claimAmount", claim.amount().toString());
		observation.observe(() -> {
			// Execute business logic here
			try {
				Thread.sleep(30);
				myCounter.increment(10);
				if(RandomGenerator.getDefault().nextBoolean()) {
					throw new RuntimeException("some error");
				}
			} catch (Exception _) {
				errorCounter.increment();
				observation.highCardinalityKeyValue("error", "error here");
			}
		});
		return new Response("SUCCESS", UUID.randomUUID().toString());
	}
}

record Response(String status, String correlationId){}
record Claim(String type, Double amount){}
