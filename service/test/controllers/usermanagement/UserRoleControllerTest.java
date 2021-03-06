package controllers.usermanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.BaseApplicationTest;
import controllers.DummyActor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modules.OnRequestHandler;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.response.ResponseParams;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.responsecode.ResponseCode;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

@PrepareForTest(OnRequestHandler.class)
public class UserRoleControllerTest extends BaseApplicationTest {

  private static String role = "someRole";
  private static String userId = "someUserId";
  private static String orgId = "someOrgId";

  @Before
  public void before() throws Exception {
    setup(DummyActor.class);
  }

  @Test
  public void testAssignRolesSuccess() {
    // setup(DummyActor.class);
    Result result =
        performTest("/v1/user/assign/role", "POST", createUserRoleRequest(true, true, true, role));
    assertEquals(getResponseCode(result), ResponseCode.success.getErrorCode().toLowerCase());
    assertTrue(getResponseStatus(result) == 200);
  }

  @Test
  public void testAssignRolesFailueWithoutOrgId() {
    Result result =
        performTest("/v1/user/assign/role", "POST", createUserRoleRequest(true, false, true, role));
    assertEquals(getResponseCode(result), ResponseCode.mandatoryParamsMissing.getErrorCode());
    assertTrue(getResponseStatus(result) == 400);
  }

  @Test
  public void testAssignRolesFailueWithoutUserId() {
    Result result =
        performTest("/v1/user/assign/role", "POST", createUserRoleRequest(false, true, true, role));
    assertEquals(getResponseCode(result), ResponseCode.mandatoryParamsMissing.getErrorCode());
    assertTrue(getResponseStatus(result) == 400);
  }

  @Test
  public void testAssignRolesFailureWithoutRoles() {
    Result result =
        performTest("/v1/user/assign/role", "POST", createUserRoleRequest(true, true, false, null));

    assertEquals(getResponseCode(result), ResponseCode.mandatoryParamsMissing.getErrorCode());
    assertTrue(getResponseStatus(result) == 400);
  }

  @Test
  public void testUpdateAssignedRolesFailureWithEmptyRole() {
    Result result =
        performTest("/v1/user/assign/role", "POST", createUserRoleRequest(true, true, true, null));
    assertEquals(getResponseCode(result), ResponseCode.emptyRolesProvided.getErrorCode());
    assertTrue(getResponseStatus(result) == 400);
  }

  @Test
  public void testGetAllRolesSuccess() {
    Result result = performTest("/v1/role/read", "GET", null);
    assertEquals(getResponseCode(result), ResponseCode.success.getErrorCode().toLowerCase());
    assertTrue(getResponseStatus(result) == 200);
  }

  private Map createUserRoleRequest(
      boolean isUserIdReq, boolean isOrgReq, boolean isRoleReq, String role) {
    Map<String, Object> requestMap = new HashMap<>();

    Map<String, Object> innerMap = new HashMap<>();

    if (isUserIdReq) innerMap.put(JsonKey.USER_ID, userId);
    if (isOrgReq) innerMap.put(JsonKey.ORGANISATION_ID, orgId);
    if (isRoleReq) {
      List<String> roles = new ArrayList<>();
      if (role != null) roles.add(role);
      innerMap.put(JsonKey.ROLES, roles);
    }
    requestMap.put(JsonKey.REQUEST, innerMap);

    return requestMap;
  }

  public Result performTest(String url, String method, Map map) {
    String data = mapToJson(map);
    Http.RequestBuilder req;
    if (StringUtils.isNotBlank(data)) {
      JsonNode json = Json.parse(data);
      req = new Http.RequestBuilder().bodyJson(json).uri(url).method(method);
    } else {
      req = new Http.RequestBuilder().uri(url).method(method);
    }
    //    req.headers(new Http.Headers(headerMap));
    Result result = Helpers.route(application, req);
    return result;
  }

  public String mapToJson(Map map) {
    ObjectMapper mapperObj = new ObjectMapper();
    String jsonResp = "";

    if (map != null) {
      try {
        jsonResp = mapperObj.writeValueAsString(map);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return jsonResp;
  }

  public String getResponseCode(Result result) {
    String responseStr = Helpers.contentAsString(result);
    ObjectMapper mapper = new ObjectMapper();

    try {
      Response response = mapper.readValue(responseStr, Response.class);
      ResponseParams params = response.getParams();
      if (result.status() != 200) {
        return params.getErr();
      } else {
        return params.getStatus();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  public int getResponseStatus(Result result) {
    return result.status();
  }
}
