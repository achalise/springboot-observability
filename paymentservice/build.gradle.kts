plugins {
	java
	id("org.springframework.boot") version "3.3.1"
	id("io.spring.dependency-management") version "1.1.5"
}

group = "dev.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(22)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")

	implementation("com.grafana:grafana-opentelemetry-starter:1.4.0")
	implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

dependencyManagement {
	imports {
		mavenBom("io.opentelemetry:opentelemetry-bom:1.39.0")
		mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha:2.5.0-alpha")
	}
}