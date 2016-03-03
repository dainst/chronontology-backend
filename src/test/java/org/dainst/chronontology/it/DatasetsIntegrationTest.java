package org.dainst.chronontology.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.dainst.chronontology.Constants;
import org.dainst.chronontology.JsonTestUtils;
import org.dainst.chronontology.TestConstants;
import org.dainst.chronontology.util.JsonUtils;
import org.dainst.chronontology.handler.model.Results;
import org.testng.annotations.Test;

import static org.dainst.chronontology.util.JsonUtils.json;
import static org.dainst.chronontology.it.ResponseUtil.*;
import static org.testng.Assert.assertEquals;

/**
 * @author Daniel M. de Oliveira
 */
public class DatasetsIntegrationTest extends IntegrationTest {

    private JsonNode dataset(String nr) {
        return json("{ \"dataset\" : \"dataset"+nr+"\" }");
    }

    @Test
    public void createForbiddenForUserWithoutPermissionsForDataset() {
        assertEquals(
                getResponse(TYPE_ROUTE,
                        "POST", dataset("1"),TestConstants.USER_NAME_2,TestConstants.PASS_WORD).code(),
                Constants.HTTP_FORBIDDEN
        );
    }

    @Test
    public void createByPutForbiddenForUserWithoutPermissionsForDataset() {
        assertEquals(
                getResponse(TYPE_ROUTE+"134",
                        "PUT", dataset("1"),TestConstants.USER_NAME_2,TestConstants.PASS_WORD).code(),
                Constants.HTTP_FORBIDDEN
        );
    }

    @Test
    public void createAllowedForUserWithPermissionsForDataset() {
        assertEquals(
                getResponse(TYPE_ROUTE,
                        "POST", dataset("1"),TestConstants.USER_NAME_1,TestConstants.PASS_WORD).code(),
                Constants.HTTP_CREATED
        );
    }

    @Test
    public void createByPutAllowedForUserWithPermissionsForDataset() {
        assertEquals(
                getResponse(TYPE_ROUTE+"134",
                        "PUT", dataset("1"),TestConstants.USER_NAME_1,TestConstants.PASS_WORD).code(),
                Constants.HTTP_CREATED
        );
    }

    @Test
    public void createAllowedForAdmin() {
        assertEquals(
                getResponse(TYPE_ROUTE,
                        "POST", dataset("1"),TestConstants.USER_NAME_ADMIN,TestConstants.PASS_WORD).code(),
                Constants.HTTP_CREATED
        );
    }

    @Test
    public void createByPutAllowedForAdmin() {
        assertEquals(
                getResponse(TYPE_ROUTE+"134",
                        "PUT", dataset("1"),TestConstants.USER_NAME_ADMIN,TestConstants.PASS_WORD).code(),
                Constants.HTTP_CREATED
        );
    }

    @Test
    public void updateForbiddenForUserWithoutPermissionsForDataset() {

        client.authenticate(TestConstants.USER_NAME_1,TestConstants.PASS_WORD);
        String id= idOf(client.post(TYPE_ROUTE, dataset("1")));

        assertEquals(
                getResponse(id, "PUT", JsonUtils.json(),TestConstants.USER_NAME_2,TestConstants.PASS_WORD).code(),
                Constants.HTTP_FORBIDDEN
        );
    }

    @Test
    public void updateForbiddenForUserWithoutPermissionsForOldDataset() {

        client.authenticate(TestConstants.USER_NAME_ADMIN,TestConstants.PASS_WORD);
        String id= idOf(client.post(TYPE_ROUTE, dataset("2")));

        assertEquals(
                getResponse(id, "PUT", dataset("1"),TestConstants.USER_NAME_1,TestConstants.PASS_WORD).code(),
                Constants.HTTP_FORBIDDEN
        );
    }

    @Test
    public void updateAllowedForAdmin() {

        client.authenticate(TestConstants.USER_NAME_ADMIN,TestConstants.PASS_WORD);
        String id= idOf(client.post(TYPE_ROUTE, dataset("2")));

        assertEquals(
                getResponse(id, "PUT", dataset("1"),TestConstants.USER_NAME_ADMIN,TestConstants.PASS_WORD).code(),
                Constants.HTTP_OK
        );
    }

    @Test
    public void readNotAllowedWithoutPermission() {

        client.authenticate(TestConstants.USER_NAME_ADMIN,TestConstants.PASS_WORD);
        String id= idOf(client.post(TYPE_ROUTE, dataset("1")));

        assertEquals(
                getResponse(id, "GET", dataset("1"),TestConstants.USER_NAME_2,TestConstants.PASS_WORD).code(),
                Constants.HTTP_FORBIDDEN
        );
    }

    @Test
    public void readAllowedForAdmin() {

        client.authenticate(TestConstants.USER_NAME_ADMIN,TestConstants.PASS_WORD);
        String id= idOf(client.post(TYPE_ROUTE, dataset("1")));

        assertEquals(
                getResponse(id, "GET", dataset("1"),TestConstants.USER_NAME_ADMIN,TestConstants.PASS_WORD).code(),
                Constants.HTTP_OK
        );
    }


    @Test
    public void readAllowedWithReaderPermission() {

        client.authenticate(TestConstants.USER_NAME_ADMIN,TestConstants.PASS_WORD);
        String id= idOf(client.post(TYPE_ROUTE, dataset("1")));

        assertEquals(
                getResponse(id, "GET", dataset("1"),
                        TestConstants.USER_NAME_3, // reader for dataset1
                        TestConstants.PASS_WORD).code(),
                Constants.HTTP_OK
        );
    }

    @Test
    public void readAllowedWithEditorPermission() {

        client.authenticate(TestConstants.USER_NAME_ADMIN,TestConstants.PASS_WORD);
        String id= idOf(client.post(TYPE_ROUTE, dataset("1")));

        assertEquals(
                getResponse(id, "GET", dataset("1"),
                        TestConstants.USER_NAME_1, // editor for dataset1
                        TestConstants.PASS_WORD).code(),
                Constants.HTTP_OK
        );
    }


    @Test
    public void searchAndFilterOne() throws JsonProcessingException {

        JsonNode a= JsonUtils.json("{ \"a\" : \"b\" }");

        client.authenticate(TestConstants.USER_NAME_ADMIN,TestConstants.PASS_WORD);
        client.post(TYPE_ROUTE, dataset("1"));
        client.post(TYPE_ROUTE, a);
        ESClientTestUtil.refreshES();

        client.authenticate(
                TestConstants.USER_NAME_2, // has no rights on dataset1
                TestConstants.PASS_WORD);
        JsonTestUtils.jsonAssertEquals(
                client.get(TYPE_ROUTE),
                new Results("results").add(a).j()
        );
    }

    @Test
    public void searchAllowedWithReaderPermission() throws JsonProcessingException {

        client.authenticate(TestConstants.USER_NAME_ADMIN,TestConstants.PASS_WORD);
        client.post(TYPE_ROUTE, dataset("1"));
        ESClientTestUtil.refreshES();

        client.authenticate(
                TestConstants.USER_NAME_3, // reader for dataset1
                TestConstants.PASS_WORD);
        JsonTestUtils.jsonAssertEquals(
                client.get(TYPE_ROUTE),
                new Results("results").add(dataset("1")).j()
        );
    }

    @Test
    public void searchAllowedWithEditorPermission() throws JsonProcessingException {

        client.authenticate(TestConstants.USER_NAME_ADMIN,TestConstants.PASS_WORD);
        client.post(TYPE_ROUTE, dataset("1"));
        ESClientTestUtil.refreshES();

        client.authenticate(
                TestConstants.USER_NAME_1, // editor for dataset1
                TestConstants.PASS_WORD);
        JsonTestUtils.jsonAssertEquals(
                client.get(TYPE_ROUTE),
                new Results("results").add(dataset("1")).j()
        );
    }


    @Test
    public void readNotAllowedWithoutLogin() throws JsonProcessingException {

        client.authenticate(TestConstants.USER_NAME_ADMIN,TestConstants.PASS_WORD);
        String id= idOf(client.post(TYPE_ROUTE, dataset("1")));

        assertEquals(
                getResponse(id, "GET", dataset("1"),
                        null, // no authentication
                        null).code(),
                Constants.HTTP_FORBIDDEN
        );
    }
}
