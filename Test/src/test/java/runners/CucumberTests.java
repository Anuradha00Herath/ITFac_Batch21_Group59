package runners;

import org.junit.jupiter.api.Test;

import io.cucumber.core.cli.Main;

/**
 * This test runner allows Cucumber to discover and run feature files
 * using Cucumber's command-line interface integrated with JUnit
 */
public class CucumberTests {
    
    @Test
    void runCucumberTests() {
        System.out.println("========== Running Cucumber Tests ==========");
        
        // Run Cucumber using the configuration from cucumber.properties
        String[] cucumberArgs = {
            "--plugin", "pretty",
            "--plugin", "html:target/cucumber-report.html",
            "--plugin", "json:target/cucumber.json",
            "--glue", "classpath:stepdefinitions",
            "--glue", "classpath:hooks",
            "classpath:features"
        };
        
        try {
            byte exitStatus = Main.run(cucumberArgs, Thread.currentThread().getContextClassLoader());
            if (exitStatus != 0) {
                throw new RuntimeException("Cucumber tests failed with exit status: " + exitStatus);
            }
        } catch (Exception e) {
            System.err.println("Error running Cucumber tests: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Cucumber execution failed", e);
        }
    }
}
