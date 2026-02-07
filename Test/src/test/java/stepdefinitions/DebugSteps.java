package stepdefinitions;

import io.cucumber.java.en.Given;

public class DebugSteps {
    @Given("I print debug message")
    public void i_print_debug_message() {
        System.out.println("DEBUG: Step definition working!");
    }
}