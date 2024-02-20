package studentClasses;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.Matchers.*;

public class MarkLearningRecordAsDeleted {
    TestBase test = new TestBase();
    TestData data = new TestData();
    String student_id = data.student_Id;
    String class_id ;
    String session_id;

    String refresh_Token =data.refresh_token;
    Database_Connection Connect = new Database_Connection();
    String archived_class;
    Integer newStudentLearningRecordId;
    Integer  studentLearningRecordId;

    Integer deleted_student_learning_Record;
    Map<String, Object> PathParams = test.pathParams;
    Response Mark_Student_Learning_Record_As_Deleted;

    public  MarkLearningRecordAsDeleted()throws SQLException{
        get_data();
        get_data_for_another_Sceanrios();
    }
    public Integer get_data() {
        ResultSet resultSet = Connect.connect_to_database("select * from sessions_educational_resources ser join public.classes_subjects_sessions css \n" +
                "on ser.session_id = css.session_id  join classes_subjects cs on css.class_subject_id = cs.class_subject_id \n" +
                "join classes_students cs2 on cs2.class_id = cs.class_id join classes c on c.class_id = cs2.class_id join students_access_rights sar\n" +
                "on sar.student_id  = cs2.student_id and sar.student_access_right_class_id = cs2.class_id join educational_resources er \n" +
                "on er.educational_resource_id  = ser.educational_resource_id  join educational_resources_types ert on ert.educational_resource_type_id \n" +
                "= er.educational_resource_type_id join public.student_learning_records slr on slr.session_educational_resource_id = ser.session_educational_resource_id  \n" +
                "where cs2.student_id ="+student_id+" " +"and slr.student_learning_record_is_deleted = false");
        try {
            while (resultSet.next()) {
                newStudentLearningRecordId = resultSet.getInt("student_learning_record_id");
                class_id= resultSet.getString("class_id");
                session_id= resultSet.getString("session_id");
                if (newStudentLearningRecordId != null) {
                    studentLearningRecordId = newStudentLearningRecordId;
                    return studentLearningRecordId;
                }else{
                    return  newStudentLearningRecordId;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public void get_data_for_another_Sceanrios()throws SQLException{
        ResultSet get_archived_class = Connect.connect_to_database("\n" +
                "select * from public.classes c where c.class_archive_date < now()");

        while(get_archived_class.next()){
            archived_class= get_archived_class.getString("class_id");
        }

        ResultSet get_deleted_student_learning_Record = Connect.connect_to_database(" select * from sessions_educational_resources ser join public.classes_subjects_sessions css \n" +
                "on ser.session_id = css.session_id  join classes_subjects cs on css.class_subject_id = cs.class_subject_id \n" +
                "join classes_students cs2 on cs2.class_id = cs.class_id join classes c on c.class_id = cs2.class_id join students_access_rights sar\n" +
                "on sar.student_id  = cs2.student_id and sar.student_access_right_class_id = cs2.class_id join educational_resources er \n" +
                "on er.educational_resource_id  = ser.educational_resource_id  join educational_resources_types ert on ert.educational_resource_type_id \n" +
                "= er.educational_resource_type_id join public.student_learning_records slr on slr.session_educational_resource_id = ser.session_educational_resource_id  \n" +
                "where cs2.student_id ="+student_id+" "+"and slr.student_learning_record_is_deleted = true\n" +
                "");
        while (get_deleted_student_learning_Record.next()){
            deleted_student_learning_Record= get_deleted_student_learning_Record.getInt("student_learning_record_id");
            class_id= get_deleted_student_learning_Record.getString("class_id");
            session_id= get_deleted_student_learning_Record.getString("session_id");
        }
    }
    @When("Performing The API Of Mark Learning Record As Deleted API")
    public void perform_mark_learning_record_as_deleted() {
        System.out.println(archived_class + " " + deleted_student_learning_Record  + " " + class_id + session_id + get_data().toString());
        Mark_Student_Learning_Record_As_Deleted = test.sendRequest("DELETE", "/students/{student_id}/classes/{class_id}/sessions/{session_id}/records/{record_id}", null,refresh_Token);
    }
    @Given("User Send Valid Data To MarkLearningRecordAsDeleted API")
    public void delete_student_learning_record() {
        PathParams.put("student_id", student_id);
        PathParams.put("class_id", class_id);
        PathParams.put("session_id", session_id);
        PathParams.put("record_id", studentLearningRecordId);
    }
    @Then("Response Status Code Is 200 And Body Contains Learning Record Is Deleted Successfully")
    public void validate_mark_learning_Record_response() {
        Mark_Student_Learning_Record_As_Deleted.prettyPrint();
        Mark_Student_Learning_Record_As_Deleted.then()
                .statusCode(HttpStatus.SC_OK)
                .body(JsonSchemaValidator.matchesJsonSchema(new File("src/test/resources/Schemas/StudentClassesSchemas/MarkLearningRecordAsDeleted.json")))
                .body("message", containsString("Learning record successfully marked as deleted"))
                .body("record_id", equalTo((studentLearningRecordId)));
    }
    @Given("User Send Invalid Student Id To Mark Learning Record Request")
    public void mark_learning_record_unAuthorizedStudent() {
        PathParams.put("student_id", "134565433123");
        PathParams.put("class_id", class_id);
        PathParams.put("session_id", session_id);
        PathParams.put("record_id", studentLearningRecordId);
    }
    @Then("Status Code Of Mark Request Is 403 And Body Have Error Message")
    public void validate_mark_learning_Record_unauthorized() {
        test.Validate_Error_Messages(Mark_Student_Learning_Record_As_Deleted, HttpStatus.SC_FORBIDDEN, "Unauthorized", 4031);
    }
    @Given("User Send Archived ClassId To Mark Learning Record As Deleted API")
    public void mark_learning_Record_invalid_class() {
        PathParams.put("student_id", student_id);
        PathParams.put("class_id", archived_class);
        PathParams.put("session_id", session_id);
        PathParams.put("record_id", archived_class);
    }
    @Then("Response Code is 404 And Body Have Class Not Found Message")
    public void validate_mark_learning_record_invalid_classId() {
        test.Validate_Error_Messages(Mark_Student_Learning_Record_As_Deleted, HttpStatus.SC_NOT_FOUND, "Class not found or not eligible for display", 4046);
    }
    @Given("Send Deleted Learning Record")
    public void mark_learning_Record_marked_as_deleted() {
        PathParams.put("student_id", student_id);
        PathParams.put("class_id", class_id);
        PathParams.put("session_id", session_id);
        PathParams.put("record_id", deleted_student_learning_Record);
    }
    @Then("Response Code Is 404 And Body Have Learning Record Is Deleted Message")
    public void validate_deleted_learning_record() {
        test.Validate_Error_Messages(Mark_Student_Learning_Record_As_Deleted, HttpStatus.SC_NOT_FOUND, "Learning record not found or has been deleted.", 40411);
    }
}
