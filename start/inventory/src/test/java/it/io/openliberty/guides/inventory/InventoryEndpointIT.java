package it.io.openliberty.guides.inventory;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InventoryEndpointIT {

    private static String port;
    private static String baseUrl;
    private static String hostname;

    private Client client;

    private final String INVENTORY_SYSTEMS = "inventory/systems";

    @BeforeAll
    public static void oneTimeSetup() {
        port = System.getProperty("http.port");
        baseUrl = "http://localhost:" + port + "/";
    }

    @BeforeEach
    public void setup() {
        client = ClientBuilder.newClient();
    }

    @AfterEach
    public void teardown() {
        client.close();
    }


    @Test
    @Order(1)
    public void testGetSystems() {
        Response response = this.getResponse(baseUrl + INVENTORY_SYSTEMS);
        this.assertResponse(baseUrl, response);

        JsonArray systems = response.readEntity(JsonArray.class);

        boolean hostnameExists = false;
        boolean recentLoadExists = false;
        for (int n = 0; n < systems.size(); n++) {
            hostnameExists = systems.getJsonObject(n)
                                    .get("hostname").toString().isEmpty();
            recentLoadExists = systems.getJsonObject(n)
                                      .get("systemLoad").toString().isEmpty();

            assertFalse(hostnameExists, "A host was registered, but it was empty");
            assertFalse(recentLoadExists,
                "A recent system load was registered, but it was empty");
            if (!hostnameExists && !recentLoadExists) {
                String host = systems.getJsonObject(n).get("hostname").toString();
                hostname = host.substring(1, host.length() - 1);
                break;
            }
        }
        assertNotNull(hostname, "Hostname should be set by the first test. (1)");
        response.close();
    }

    @Test
    @Order(2)
    public void testGetSystemsWithHost() {
        assertNotNull(hostname, "Hostname should be set by the first test. (2)");

        Response response =
            this.getResponse(baseUrl + INVENTORY_SYSTEMS + "/" + hostname);
        this.assertResponse(baseUrl, response);

        JsonObject system = response.readEntity(JsonObject.class);

        String responseHostname = system.getString("hostname");
        Boolean recentLoadExists = system.get("systemLoad").toString().isEmpty();

        assertEquals(hostname, responseHostname,
            "Hostname should match the one from the TestNonEmpty");
        assertFalse(recentLoadExists, "A recent system load should not be empty");

        response.close();
    }

    @Test
    @Order(3)
    public void testUnknownHost() {
        Response badResponse =
            client.target(baseUrl + INVENTORY_SYSTEMS + "/" + "badhostname")
                  .request(MediaType.APPLICATION_JSON).get();

        assertEquals(404, badResponse.getStatus(),
            "BadResponse expected status: 404. Response code not as expected.");

        String stringObj = badResponse.readEntity(String.class);
        assertTrue(stringObj.contains("hostname does not exist."),
            "badhostname is not a valid host but it didn't raise an error");

        badResponse.close();
    }

    private Response getResponse(String url) {
        return client.target(url).request().get();
    }

    private void assertResponse(String url, Response response) {
        assertEquals(200, response.getStatus(), "Incorrect response code from " + url);
    }

}
