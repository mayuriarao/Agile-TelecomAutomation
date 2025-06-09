import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.testng.Assert.*;

public class ContactListTest {

    private final String baseUrl = "https://thinking-tester-contact-list.herokuapp.com";
    private String token;
    private String contactId;

    private ExtentReports extent;
    private ExtentTest test;

    @BeforeSuite
    public void setupReport() {
        ExtentSparkReporter reporter = new ExtentSparkReporter("test-output/ContactListReport.html");
        extent = new ExtentReports();
        extent.attachReporter(reporter);
    }

    @Test(priority = 1)
    public void addUser() {
        test = extent.createTest("1. Add New User");

        String body = String.format("""
        {
          "%s": "Mayuri",
          "%s": "Rao",
          "%s": "mayurirao@fake.com",
          "%s": "mayurirao"
        }
        """, APIConstants.KEY_FIRST_NAME, APIConstants.KEY_LAST_NAME, APIConstants.KEY_EMAIL, APIConstants.KEY_PASSWORD);

        Response res = RestAssured.given().header(APIConstants.HEADER_CONTENT_TYPE, APIConstants.VALUE_JSON)
                .body(body)
                .post(baseUrl + "/users");

        if (res.getStatusCode() == SC_CREATED) {
            test.pass("User added successfully");
            token = res.jsonPath().getString("token");
            assertNotNull(token);
        } else {
            test.fail("Expected status code 201 but got: " + res.getStatusCode());
            assertEquals(res.getStatusCode(), 201);
        }
    }

    @Test(priority = 2)
    public void getUserProfile() {
        test = extent.createTest("2. Get User Profile");

        Response res = RestAssured.given()
                .header(APIConstants.HEADER_AUTH, APIConstants.BEARER_PREFIX +" " + token)
                .get(baseUrl + "/users/me");

        if (res.getStatusCode() == HttpStatus.SC_OK) {
            assertEquals(res.jsonPath().getString(APIConstants.KEY_EMAIL), "mayurirao@fake.com");
            test.pass("Profile fetched successfully");
        } else {
            test.fail("Expected status code 200 but got: " + res.getStatusCode());
            assertEquals(res.getStatusCode(), HttpStatus.SC_OK);
        }
    }

    @Test(priority = 3)
    public void updateUser() {
        test = extent.createTest("3. Update User");

        String body = String.format("""
        {
          "%s": "Mayuri-Updated",
          "%s": "Rao",
          "%s": "mayurirao@fake.com",
          "%s": "mayurirao"
        }
        """, APIConstants.KEY_FIRST_NAME, APIConstants.KEY_LAST_NAME, APIConstants.KEY_EMAIL, APIConstants.KEY_PASSWORD);

        Response res = RestAssured.given()
                .header(APIConstants.HEADER_AUTH, APIConstants.BEARER_PREFIX +" "+ token)
                .header(APIConstants.HEADER_CONTENT_TYPE, APIConstants.VALUE_JSON)
                .body(body)
                .patch(baseUrl + "/users/me");

        if (res.getStatusCode() == HttpStatus.SC_OK) {
            test.pass("User updated successfully");
        } else {
            test.fail("Expected status code 200 but got: " + res.getStatusCode());
            assertEquals(res.getStatusCode(), 200);
        }
    }

    @Test(priority = 4)
    public void loginUser() {
        test = extent.createTest("4. Login User");

        String body = String.format("""
        {
          "%s": "mayurirao@fake.com",
          "%s": "mayurirao"
        }
        """, APIConstants.KEY_EMAIL, APIConstants.KEY_PASSWORD);

        Response res = RestAssured.given().header(APIConstants.HEADER_CONTENT_TYPE, APIConstants.VALUE_JSON)
                .body(body)
                .post(baseUrl + "/users/login");

        if (res.getStatusCode() == HttpStatus.SC_OK) {
            token = res.jsonPath().getString("token");
            assertNotNull(token);
            test.pass("Login successful, token acquired");
        } else {
            test.fail("Expected status code 200 but got: " + res.getStatusCode());
            assertEquals(res.getStatusCode(), HttpStatus.SC_OK);
        }
    }

   /* @Test(priority = 5)
    public void loginWithNewUser() {
        test = extent.createTest("5. Login with Updated User");

        String body = """
        {
          "%s": "mayurirao@fake.com",
          "%s": "mayurirao"
        }
        """;

        Response res = RestAssured.given()
                .header(APIConstants.HEADER_CONTENT_TYPE, APIConstants.VALUE_JSON)
                .body(body)
                .post(baseUrl + "/users/login");

        token = res.jsonPath().getString("token");
        assertNotNull(token);
        test.pass("Login successful with updated credentials");
    }*/

    @Test(priority = 5)
    public void addContact() {
        test = extent.createTest("5. Add Contact");

        String body = String.format( """
        {
          "%s": "John",
          "%s": "Doe",
          "%s": "1970-01-01",
          "%s": "jdoe@fake.com",
          "%s": "8005555555",
          "%s": "1 Main St.",
          "%s": "Apartment A",
          "%s": "Anytown",
          "%s": "KS",
          "%s": "12345",
          "%s": "USA"
        }
        """, APIConstants.KEY_FIRST_NAME, APIConstants.KEY_LAST_NAME, APIConstants.KEY_BIRTHDATE, APIConstants.KEY_PHONE, APIConstants.KEY_STREET1
        , APIConstants.KEY_STREET2, APIConstants.KEY_CITY, APIConstants.KEY_STATE, APIConstants.KEY_POSTAL_CODE, APIConstants.KEY_COUNTRY);

        Response res = RestAssured.given()
                .header(APIConstants.HEADER_AUTH, APIConstants.BEARER_PREFIX +" " + token)
                .header(APIConstants.HEADER_CONTENT_TYPE, APIConstants.VALUE_JSON)
                .body(body)
                .post(baseUrl + "/contacts");

        if (res.getStatusCode() == SC_CREATED) {
            contactId = res.jsonPath().getString("_id");
            assertNotNull(contactId);
            test.pass("Contact Added with ID :"+contactId);
        } else {
            test.fail("Expected status code 201 but got: " + res.getStatusCode());
            assertEquals(res.getStatusCode(), SC_CREATED);
        }

    }

    @Test(priority = 6)
    public void getContactList() {
        test = extent.createTest("6. Get Contact List");

        Response res = RestAssured.given()
                .header(APIConstants.HEADER_AUTH, APIConstants.BEARER_PREFIX +" " + token)
                .get(baseUrl + "/contacts");

        if (res.getStatusCode() == HttpStatus.SC_OK) {
            test.pass("Contact List fetched");
        } else {
            test.fail("Expected status code 200 but got: " + res.getStatusCode());
            assertEquals(res.getStatusCode(), HttpStatus.SC_OK);
        }
    }

    @Test(priority = 7)
    public void getContactById() {
        test = extent.createTest("8. Get Contact by ID");

        Response res = RestAssured.given()
                .header(APIConstants.HEADER_AUTH, APIConstants.BEARER_PREFIX +" " + token)
                .get(baseUrl + "/contacts/" + contactId);

        if (res.getStatusCode() == HttpStatus.SC_OK) {
            test.pass("Contact List fetched");
        } else {
            
        }
        assertEquals(res.getStatusCode(), 200);
        assertEquals(res.jsonPath().getString("email"), "jdoe@fake.com");
        test.pass("Contact fetched successfully");
    }

    @Test(priority = 8)
    public void updateFullContact() {
        test = extent.createTest("9. Update Full Contact");

        String body = """
        {
          "firstName": "Amy",
          "lastName": "Miller",
          "birthdate": "1992-02-02",
          "email": "amiller@fake.com",
          "phone": "8005554242",
          "street1": "13 School St.",
          "street2": "Apt. 5",
          "city": "Washington",
          "stateProvince": "QC",
          "postalCode": "A1A1A1",
          "country": "Canada"
        }
        """;

        Response res = RestAssured.given()
                .header(APIConstants.HEADER_AUTH, APIConstants.BEARER_PREFIX +" " + token)
                .header(APIConstants.HEADER_CONTENT_TYPE, APIConstants.VALUE_JSON)
                .body(body)
                .put(baseUrl + "/contacts/" + contactId);

        assertEquals(res.getStatusCode(), 200);
        assertEquals(res.jsonPath().getString("email"), "amiller@fake.com");
        test.pass("Contact updated fully");
    }

    @Test(priority = 9)
    public void updatePartialContact() {
        test = extent.createTest("10. Update Partial Contact");

        String body = """
        {
          "firstName": "Anna"
        }
        """;

        Response res = RestAssured.given()
                .header(APIConstants.HEADER_AUTH, APIConstants.BEARER_PREFIX +" " + token)
                .header(APIConstants.HEADER_CONTENT_TYPE, APIConstants.VALUE_JSON)
                .body(body)
                .patch(baseUrl + "/contacts/" + contactId);

        assertEquals(res.getStatusCode(), 200);
        assertEquals(res.jsonPath().getString("firstName"), "Anna");
        test.pass("Contact updated partially (firstName only)");
    }

    @Test(priority = 10)
    public void logoutUser() {
        test = extent.createTest("11. Logout User");

        Response res = RestAssured.given()
                .header(APIConstants.HEADER_AUTH, APIConstants.BEARER_PREFIX +" " + token)
                .post(baseUrl + "/users/logout");

        assertEquals(res.getStatusCode(), 200);
        test.pass("User logged out");
    }

    @AfterSuite
    public void tearDownReport() {
        extent.flush();
    }
}
