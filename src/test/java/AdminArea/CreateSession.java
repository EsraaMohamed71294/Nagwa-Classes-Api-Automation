package AdminArea;

import EducatorProfile.Educator_TestData;
import com.github.javafaker.Faker;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import TestConfig.TestBase;

import java.io.File;
import java.sql.SQLException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

public class CreateSession {
    TestBase test = new TestBase();
    Educator_TestData data = new Educator_TestData();
    CreateEducator educator = new CreateEducator();
    GetClass Class = new GetClass();
    Faker fakeDate =new Faker();
    String sessionTitle = fakeDate.name().title();
    Long EducatorId;
    Long Class_ID;
    Long subject;
    Long sessionId;
    String valid_body;
    Response Create_Session;
    Response Create_Session_with_notExisting_class;
    Response Create_Session_with_notExisting_subject;
    Response Create_Session_with_invalid_data;
    Response Create_Session_without_subject;
    Response Create_Session_InvalidToken;

    @Given("Performing the Api of Create session With valid data")
    public Long Create_Session() throws SQLException {
        Class.user_send_valid_classId();
        Class.Get_Class();
        Class.getClassDetails ();
        Class_ID = Class.classID;
        EducatorId = Class.EducatorID;
        subject = Class.Subjects;

        System.out.println("class " + Class_ID + " Edu " + EducatorId + " sub "+ subject);

        valid_body ="{\"session_title\":\""+ sessionTitle +"\",\"session_start_date\":\"2024-10-01T18:00:00Z\"," +
                "\"session_end_date\":\"2025-02-01T20:00:00Z\",\"session_duration_in_minutes\":120,\"educator_id\":"+ EducatorId +"," +
                "\"meta_session_id\":123456789012,\"session_order\":1,\"is_test_session\":false,\"classes_subjects\":[{\"class_id\":"+ Class_ID +"," +
                "\"subject_id\":"+ subject +",\"class_subject_session_price\":10}]}";
                System.out.println(valid_body);


        Create_Session = test.sendRequest("POST", "/admin/sessions", valid_body, data.Admin_Token);

        return sessionId = Create_Session.then().extract().path("session_id");
    }

    @Then("I verify the appearance of status code 200 and create session successfully")
    public void Validate_Response_of_create_session_successfully() {
        Create_Session.prettyPrint();
        Create_Session.then()
                .statusCode(HttpStatus.SC_CREATED)
                .assertThat()
                .body(JsonSchemaValidator.matchesJsonSchema(new File("src/test/resources/Schemas/AdminAreaSchemas/CreateSession.json")))
                .body("message", hasToString("Session created successfully."),"session_id",equalTo(sessionId));
    }

    @Given("Performing the Api of create session With class id not exist")
    public void Create_Session_with_class_notFound() throws SQLException {
        System.out.println("title is " + sessionTitle);
        Class.user_send_valid_classId();
        Class.Get_Class();
        Class.getClassDetails ();
        Class_ID = Class.classID;
        EducatorId = Class.EducatorID;
        subject = Class.Subjects;
        String InvalidClass_into_body = "{\"session_title\":\""+ sessionTitle +"\",\"session_start_date\":\"2024-10-01T18:00:00Z\"," +
                "\"session_end_date\":\"2025-02-01T20:00:00Z\",\"session_duration_in_minutes\":120,\"educator_id\":"+ EducatorId +"," +
                "\"meta_session_id\":123456789012,\"session_order\":1,\"is_test_session\":false,\"classes_subjects\":[{\"class_id\":123456789123," +
                "\"subject_id\":"+ subject +",\"class_subject_session_price\":100}]}";

        Create_Session_with_notExisting_class = test.sendRequest("POST", "/admin/sessions", InvalidClass_into_body, data.Admin_Token);
    }

    @Then("I verify the appearance of status code 404 and class id not found")
    public void Validate_Response_of_get_session_with_NOTfoundClass() {
        Response notFound_class = Create_Session_with_notExisting_class;
        test.Validate_Error_Messages(notFound_class,HttpStatus.SC_NOT_FOUND,"class not found or not eligible for display.",4046);
    }

    @Given("Performing the Api of create session With Invalid subject")
    public void Create_Session_with_subject_notFound() throws SQLException {
        Class.user_send_valid_classId();
        Class.Get_Class();
        Class.getClassDetails ();
        Class_ID = Class.classID;
        EducatorId = Class.EducatorID;
        subject = Class.Subjects;
        String InvalidClass_into_body ="{\"session_title\":\""+ sessionTitle +"\",\"session_start_date\":\"2024-10-01T18:00:00Z\"," +
                "\"session_end_date\":\"2025-02-01T20:00:00Z\",\"session_duration_in_minutes\":120,\"educator_id\":"+ EducatorId +"," +
                "\"meta_session_id\":123456789012,\"session_order\":1,\"is_test_session\":false,\"classes_subjects\":[{\"class_id\":"+ Class_ID +"," +
                "\"subject_id\":??????,\"class_subject_session_price\":100}]}" ;

        Create_Session_with_notExisting_subject = test.sendRequest("POST", "/admin/sessions", InvalidClass_into_body, data.Admin_Token);
    }

    @Then("I verify the appearance of status code 404 and subject is Invalid")
    public void Validate_Response_of_get_session_with_NOTfoundSubject() {
        Response notFound_subject = Create_Session_with_notExisting_subject;
        test.Validate_Error_Messages(notFound_subject,HttpStatus.SC_BAD_REQUEST,"Invalid request. Please check the path parameters and request context for accuracy.",4002);
    }

    @Given("Performing the Api of create session With Invalid data")
    public void Create_Session_with_invalid_data() throws SQLException {

        String Invalid_body ="{\"session_title\":\"\",\"session_start_date\":\"\"," +
                "\"session_end_date\":\"\",\"session_duration_in_minutes\":120,\"educator_id\":,\"meta_session_id\":123456789012," +
                "\"session_order\":1,\"is_test_session\":true,\"classes_subjects\":[{\"class_id\":,\"subject_id\":123456789012,\"class_subject_session_price\":1}]}" ;

        Create_Session_with_invalid_data = test.sendRequest("POST", "/admin/sessions", Invalid_body, data.Admin_Token);
    }

    @Then("I verify the appearance of status code 400 and invalid data")
    public void Validate_Response_of_get_session_with_InvalidData() {
        Response Invalid_data = Create_Session_with_invalid_data;
        test.Validate_Error_Messages(Invalid_data,HttpStatus.SC_BAD_REQUEST,"Invalid request. Please check the path parameters and request context for accuracy.",4002);
    }

    @Given("Performing the Api of create session Without sending subject")
    public void Create_Session_without_sending_subject() throws SQLException {
        Class.user_send_valid_classId();
        Class.Get_Class();
        Class.getClassDetails ();
        Class_ID = Class.classID;
        EducatorId = Class.EducatorID;
        String body_without_subject ="{\"session_title\":\""+ sessionTitle +"\",\"session_start_date\":\"2024-10-01T18:00:00Z\"," +
                "\"session_end_date\":\"2025-02-01T20:00:00Z\",\"session_duration_in_minutes\":120,\"educator_id\":"+ EducatorId +"," +
                "\"meta_session_id\":123456789012,\"session_order\":1,\"is_test_session\":false,\"classes_subjects\":[{\"class_id\":"+ Class_ID +"," +
                "\"subject_id\":123456789098,\"class_subject_session_price\":100}]}" ;

        Create_Session_without_subject = test.sendRequest("POST", "/admin/sessions", body_without_subject, data.Admin_Token);
    }

    @Then("I verify the appearance of status code 404 and there is no subject")
    public void Validate_Response_of_get_session_without_subject() {
        Response Invalid_data = Create_Session_without_subject;
        test.Validate_Error_Messages(Invalid_data,HttpStatus.SC_NOT_FOUND,"No subjects found.",4045);
    }

    @Given("Performing the Api of Create session With invalid token")
    public void Create_Session_invalid_token() throws SQLException {
        Class.user_send_valid_classId();
        Class.Get_Class();
        Class.getClassDetails ();
        Class_ID = Class.classID;
        EducatorId = Class.EducatorID;
        subject = Class.Subjects;
        String body_request ="{\"session_title\":\""+ sessionTitle +"\",\"session_start_date\":\"2024-10-01T18:00:00Z\"," +
                "\"session_end_date\":\"2025-02-01T20:00:00Z\",\"session_duration_in_minutes\":120,\"educator_id\":"+ EducatorId +"," +
                "\"meta_session_id\":123456789012,\"session_order\":1,\"is_test_session\":false,\"classes_subjects\":[{\"class_id\":"+ Class_ID +"," +
                "\"subject_id\":"+ subject +",\"class_subject_session_price\":100}]}";

        Create_Session_InvalidToken = test.sendRequest("POST", "/admin/sessions", body_request, data.refresh_token_for_notActiveEducator);
    }

    @Then("I verify the appearance of status code 403 and invalid token of the admin")
    public void Validate_Response_of_create_session_invalidToken() {
        Response Invalid_token = Create_Session_InvalidToken;
        test.Validate_Error_Messages(Invalid_token,HttpStatus.SC_FORBIDDEN,"Unauthorized",4031);
    }


}
