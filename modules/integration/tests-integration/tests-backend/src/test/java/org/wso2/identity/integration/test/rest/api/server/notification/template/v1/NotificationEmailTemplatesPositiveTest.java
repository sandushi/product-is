package org.wso2.identity.integration.test.rest.api.server.notification.template.v1;

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.APP_TEMPLATES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_BODY;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ATTRIBUTE_CONTENT_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_FOOTER;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_LOCALE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_SELF;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.ATTRIBUTE_SUBJECT;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.BASE_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.COLLECTION_QUERY_BY_LOCALE_TEMPLATE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.EMAIL_TEMPLATES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.SAMPLE_APPLICATION_UUID;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.TEMPLATE_TYPES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.LOCALE_EN_US;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.ORG_TEMPLATES_PATH;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.PATH_SEPARATOR;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.PLACE_HOLDER_BODY;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.PLACE_HOLDER_CONTENT_TYPE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.PLACE_HOLDER_FOOTER;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1.Constants.PLACE_HOLDER_LOCALE;
import static org.wso2.identity.integration.test.rest.api.server.notification.template.v1
        .Constants.PLACE_HOLDER_SUBJECT;

/**
 * Integration tests for Notification Template API.
 * Test class for Email Templates REST API positive paths.
 */
public class NotificationEmailTemplatesPositiveTest extends NotificationTemplatesTestBase {

    private static final String TEMPLATE_TYPE_SYSTEM = "AccountConfirmation";

    private static final String TEST_DATA_BODY_1 = "Test Email Template Body 1";
    private static final String TEST_DATA_BODY_2 = "Test Email Template Body 2";
    private static final String TEST_DATA_FOOTER_1 = "Test Email Template Footer 1";
    private static final String TEST_DATA_FOOTER_2 = "Test Email Template Footer 2";
    private static final String TEST_DATA_SUBJECT_1 = "Test Email Template Subject 1";
    private static final String TEST_DATA_SUBJECT_2 = "Test Email Template Subject 2";

    @Factory (dataProvider = "restAPIUserConfigProvider")
    public NotificationEmailTemplatesPositiveTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);
    }


    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.TENANT_ADMIN}
        };
    }

    @DataProvider(name = "emailTemplateDataProvider")
    public static Object[][] emailTemplateDataProvider() {

        String testTemplateTypeId = base64String(TEMPLATE_TYPE_SYSTEM);
        String orgTemplateRequestPath = EMAIL_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testTemplateTypeId + ORG_TEMPLATES_PATH;
        String appTemplateRequestPath = EMAIL_TEMPLATES_PATH + TEMPLATE_TYPES_PATH + PATH_SEPARATOR
                + testTemplateTypeId + APP_TEMPLATES_PATH + PATH_SEPARATOR + SAMPLE_APPLICATION_UUID;
        return new Object[][]{
                {orgTemplateRequestPath},
                {appTemplateRequestPath}
        };
    }

    @Test(groups = "wso2.is", dataProvider = "emailTemplateDataProvider")
    public void testAddTemplate(String requestPath) throws Exception {

        // Add the template.
        String requestBodyTemplate = readResource("request-body-add-email-template.template");
        String requestBody = requestBodyTemplate
                .replace(PLACE_HOLDER_CONTENT_TYPE, ContentType.TEXT_HTML.getMimeType())
                .replace(PLACE_HOLDER_SUBJECT, TEST_DATA_SUBJECT_1)
                .replace(PLACE_HOLDER_BODY, TEST_DATA_BODY_1)
                .replace(PLACE_HOLDER_FOOTER, TEST_DATA_FOOTER_1)
                .replace(PLACE_HOLDER_LOCALE, LOCALE_EN_US);
        String resourcePath = requestPath + PATH_SEPARATOR + LOCALE_EN_US;
        String selfPath = getTenantedRelativePath(BASE_PATH + resourcePath,
                context.getContextTenant().getDomain());
        Response response = getResponseOfPost(requestPath, requestBody);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body(ATTRIBUTE_LOCALE, equalTo(LOCALE_EN_US))
                .body(ATTRIBUTE_SELF, equalTo(selfPath));
        // Verify that the template is added.
        Response validationResponse = getResponseOfGet(resourcePath);
        validationResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(
            groups = "wso2.is",
            dataProvider = "emailTemplateDataProvider",
            dependsOnMethods = {"testAddTemplate"})
    public void testGetTemplatesOfTemplateType(String requestPath) throws Exception {

        String collectionQueryByLocaleENUS = String.format(COLLECTION_QUERY_BY_LOCALE_TEMPLATE, LOCALE_EN_US);
        String resourcePathUS = requestPath + PATH_SEPARATOR + LOCALE_EN_US;
        String selfPathUS = getTenantedRelativePath(BASE_PATH + resourcePathUS,
                context.getContextTenant().getDomain());
        Response response = getResponseOfGet(requestPath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("$", isA(List.class))
                .body("$", hasSize(1))
                .body(collectionQueryByLocaleENUS + ATTRIBUTE_SELF, equalTo(selfPathUS));
    }

    @Test(
            groups = "wso2.is",
            dataProvider = "emailTemplateDataProvider",
            dependsOnMethods = {"testAddTemplate"})
    public void testGetTemplateOfTemplateType(String requestPath)  {

        String resourcePath = requestPath + PATH_SEPARATOR + LOCALE_EN_US;
        Response response = getResponseOfGet(resourcePath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ATTRIBUTE_CONTENT_TYPE, equalTo(ContentType.TEXT_HTML.getMimeType()))
                .body(ATTRIBUTE_SUBJECT, equalTo(TEST_DATA_SUBJECT_1))
                .body(ATTRIBUTE_BODY, equalTo(TEST_DATA_BODY_1))
                .body(ATTRIBUTE_FOOTER, equalTo(TEST_DATA_FOOTER_1));
    }

    @Test(
            groups = "wso2.is",
            dataProvider = "emailTemplateDataProvider",
            dependsOnMethods = {"testAddTemplate"})
    public void testUpdateTemplate(String requestPath) throws Exception {

        String requestBodyTemplate = readResource("request-body-update-email-template.template");
        String requestBody = requestBodyTemplate
                .replace(PLACE_HOLDER_CONTENT_TYPE, ContentType.TEXT_HTML.getMimeType())
                .replace(PLACE_HOLDER_SUBJECT, TEST_DATA_SUBJECT_2)
                .replace(PLACE_HOLDER_BODY, TEST_DATA_BODY_2)
                .replace(PLACE_HOLDER_FOOTER, TEST_DATA_FOOTER_2);
        String resourcePath = requestPath + PATH_SEPARATOR + LOCALE_EN_US;
        Response response = getResponseOfPut(resourcePath, requestBody);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        // Verify that the template is updated.
        Response validationResponse = getResponseOfGet(resourcePath);
        validationResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(ATTRIBUTE_CONTENT_TYPE, equalTo(ContentType.TEXT_HTML.getMimeType()))
                .body(ATTRIBUTE_SUBJECT, equalTo(TEST_DATA_SUBJECT_2))
                .body(ATTRIBUTE_BODY, equalTo(TEST_DATA_BODY_2))
                .body(ATTRIBUTE_FOOTER, equalTo(TEST_DATA_FOOTER_2));
    }

    @Test(
            groups = "wso2.is",
            dataProvider = "emailTemplateDataProvider",
            dependsOnMethods = {"testAddTemplate", "testUpdateTemplate", "testGetTemplatesOfTemplateType"})
    public void testDeleteTemplate(String requestPath) {

        String resourcePath = requestPath + PATH_SEPARATOR + LOCALE_EN_US;
        Response response = getResponseOfDelete(resourcePath);
        response.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        // Verify that the template is deleted.
        Response validationResponse = getResponseOfGet(resourcePath);
        validationResponse.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }
}
