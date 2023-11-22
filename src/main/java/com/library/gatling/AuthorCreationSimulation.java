package com.library.gatling;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.jmesPath;
import static io.gatling.javaapi.core.CoreDsl.nothingFor;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import java.time.Duration;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpDsl;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public class AuthorCreationSimulation  extends Simulation {

    private static final HttpProtocolBuilder HTTP_PROTOCOL_BUILDER = setupProtocolForSimulation();
    
    private static final ScenarioBuilder CHAIN_SCENARIO_BUILDER = buildChainScenario();
        
    public AuthorCreationSimulation() {
        this.setUp(CHAIN_SCENARIO_BUILDER.injectOpen(
        		nothingFor(5),
        		constantUsersPerSec(50).during(Duration.ofSeconds(60))))
          .protocols(HTTP_PROTOCOL_BUILDER);
    }
    
	private static HttpProtocolBuilder setupProtocolForSimulation() {
	    return HttpDsl.http.baseUrl("http://localhost:8080")
	      .acceptHeader("application/json")
	      .userAgentHeader("Gatling/Performance Test");
	}	

    private static ScenarioBuilder buildChainScenario() {
    	String id = "#{randomUuid()}";
    	String name = "gatling-test-firstname";    	
    	return scenario("Load Test Author")
				.exec(http("create-author-request").post("/api/authors")
					.header("Content-Type", "application/json")
					.body(StringBody("{ \"id\": \"" + id + "\", \"lastName\": \"" +  name + "\" }")) 
					.check(status().is(201), jmesPath("id").find().saveAs("entityId")))
					.pause(10)
                .exec(http("get-author-request").get("/api/authors/${entityId}")
	                .header("Content-Type", "application/json")
	                .check(status().is(200)))
                	.pause(10)                
                .exec(http("delete-author-request").delete("/api/authors/${entityId}")
					.header("Content-Type", "application/json")
					.check(status().is(204)));
    }
    
}
