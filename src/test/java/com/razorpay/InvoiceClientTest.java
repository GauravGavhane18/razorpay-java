package com.razorpay;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class InvoiceClientTest extends BaseTest {

    @InjectMocks
    protected InvoiceClient invoiceClient = new InvoiceClient(TEST_SECRET_KEY);

    // ✅ Invoice ID used across tests
    private static final String INVOICE_ID = "inv_DAweOiQ7amIUVd";

    /**
     * Create an invoice using customer and item details.
     */
    @Test
    public void create() throws RazorpayException {
        JSONObject request = new JSONObject("{\n" +
                "  type: \"invoice\",\n" +
                "  description: \"Invoice for the month of January 2020\",\n" +
                "  partial_payment: true,\n" +
                "  customer: {\n" +
                "    name: \"Gaurav Kumar\",\n" +
                "    contact: \"9999999999\",\n" + // ✅ Fixed: contact as string
                "    email: \"gaurav.kumar@example.com\",\n" +
                "    billing_address: {\n" +
                "      line1: \"Ground & 1st Floor, SJR Cyber Laskar\",\n" +
                "      line2: \"Hosur Road\",\n" +
                "      zipcode: 560068,\n" +
                "      city: \"Bengaluru\",\n" +
                "      state: \"Karnataka\",\n" +
                "      country: \"IN\"\n" +
                "    },\n" +
                "    shipping_address: {\n" +
                "      line1: \"Ground & 1st Floor, SJR Cyber Laskar\",\n" +
                "      line2: \"Hosur Road\",\n" +
                "      zipcode: 560068,\n" +
                "      city: \"Bengaluru\",\n" +
                "      state: \"Karnataka\",\n" +
                "      country: \"IN\"\n" +
                "    }\n" +
                "  },\n" +
                "  line_items: [\n" +
                "    {\n" +
                "      name: \"Master Cloud Computing in 30 Days\",\n" +
                "      description: \"Book by Ravena Ravenclaw\",\n" +
                "      amount: 399,\n" +
                "      currency: \"USD\",\n" +
                "      quantity: 1\n" +
                "    }\n" +
                "  ],\n" +
                "  sms_notify: 1,\n" +
                "  email_notify: 1,\n" +
                "  currency: \"USD\",\n" +
                "  expire_by: 1589765167\n" +
                "}");

        // ✅ Fixed: IDs quoted to valid JSON
        String mockedResponseJson = "{\n" +
                "  \"issued_at\": 1481541533,\n" +
                "  \"customer_details\": {\n" +
                "    \"customer_name\": \"Gaurav Kumar\",\n" +
                "    \"customer_email\": \"gaurav.kumar@example.com\",\n" +
                "    \"customer_contact\": \"9999999999\"\n" +
                "  },\n" +
                "  \"short_url\": \"http://bit.ly/link\",\n" +
                "  \"receipt\": null,\n" +
                "  \"entity\": \"invoice\",\n" +
                "  \"currency\": \"INR\",\n" +
                "  \"paid_at\": null,\n" +
                "  \"view_less\": true,\n" +
                "  \"id\": \"random_id\",\n" + // ✅ quoted
                "  \"customer_id\": \"cust_E7q0trFqXgExmT\",\n" +
                "  \"type\": null,\n" +
                "  \"status\": \"issued\",\n" +
                "  \"description\": \"random description\",\n" +
                "  \"order_id\": \"order_random_id\",\n" +
                "  \"sms_status\": \"pending\",\n" +
                "  \"date\": 1481541533,\n" +
                "  \"payment_id\": null,\n" +
                "  \"amount\": 100,\n" +
                "  \"email_status\": \"pending\",\n" +
                "  \"created_at\": 1481541534\n" +
                "}";

        try {
            mockResponseFromExternalClient(mockedResponseJson);
            mockResponseHTTPCodeFromExternalClient(200);
            Invoice invoice = invoiceClient.create(request);
            assertNotNull(invoice);
            assertEquals("invoice", invoice.get("entity"));
            assertTrue(invoice.has("customer_details"));
            assertTrue(invoice.has("short_url"));
            String createRequest = getHost(Constants.INVOICE_CREATE);
            verifySentRequest(true, request.toString(), createRequest);
        } catch (IOException e) {
            assertTrue(false);
        }
    }

    /**
     * Fetch all invoices.
     */
    @Test
    public void fetchAll() throws RazorpayException {
        String mockedResponseJson = "{\n" +
                "  \"entity\": \"collection\",\n" +
                "  \"count\": 1,\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"id\": \"" + INVOICE_ID + "\",\n" + // ✅ quoted
                "      \"entity\": \"invoice\",\n" +
                "      \"receipt\": \"#0961\",\n" +
                "      \"invoice_number\": \"#0961\",\n" +
                "      \"customer_id\": \"cust_DAtUWmvpktokrT\",\n" +
                "      \"customer_details\": {\n" +
                "        \"id\": \"cust_DAtUWmvpktokrT\",\n" +
                "        \"name\": \"Gaurav Kumar\",\n" +
                "        \"email\": \"gaurav.kumar@example.com\",\n" +
                "        \"contact\": \"9977886633\"\n" + // ✅ string
                "      },\n" +
                "      \"status\": \"draft\",\n" +
                "      \"amount\": 600,\n" +
                "      \"currency\": \"INR\",\n" +
                "      \"description\": \"This is a test invoice.\",\n" +
                "      \"type\": \"invoice\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        try {
            mockResponseFromExternalClient(mockedResponseJson);
            mockResponseHTTPCodeFromExternalClient(200);
            List<Invoice> fetch = invoiceClient.fetchAll();
            assertNotNull(fetch);
            assertTrue(fetch.get(0).has("type"));
            assertTrue(fetch.get(0).has("receipt"));
            String fetchRequest = getHost(Constants.INVOICE_LIST);
            verifySentRequest(false, null, fetchRequest);
        } catch (IOException e) {
            assertTrue(false);
        }
    }

    /**
     * Fetch invoice by ID.
     */
    @Test
    public void fetch() throws RazorpayException {
        String mockedResponseJson = "{\n" +
                "  \"id\": \"" + INVOICE_ID + "\",\n" + // ✅ quoted
                "  \"entity\": \"invoice\",\n" +
                "  \"customer_details\": {\n" +
                "    \"customer_name\": \"Gaurav Kumar\",\n" +
                "    \"customer_email\": \"gaurav.kumar@example.com\",\n" +
                "    \"customer_contact\": \"9999999999\"\n" + // ✅ string
                "  },\n" +
                "  \"status\": \"issued\"\n" +
                "}";

        try {
            mockResponseFromExternalClient(mockedResponseJson);
            mockResponseHTTPCodeFromExternalClient(200);
            Invoice fetch = invoiceClient.fetch(INVOICE_ID);
            assertNotNull(fetch);
            assertEquals(INVOICE_ID, fetch.get("id"));
            assertTrue(fetch.has("customer_details"));
            String fetchRequest = getHost(String.format(Constants.INVOICE_GET, INVOICE_ID));
            verifySentRequest(false, null, fetchRequest);
        } catch (IOException e) {
            assertTrue(false);
        }
    }

    /**
     * Cancel an invoice.
     */
    @Test
    public void cancel() throws RazorpayException {
        String mockedResponseJson = "{\n" +
                "  \"id\": \"" + INVOICE_ID + "\",\n" +
                "  \"entity\": \"invoice\",\n" +
                "  \"status\": \"cancelled\",\n" +
                "  \"email_status\": \"pending\"\n" + // ✅ updated to match real response
                "}";

        try {
            mockResponseFromExternalClient(mockedResponseJson);
            mockResponseHTTPCodeFromExternalClient(200);
            Invoice fetch = invoiceClient.cancel(INVOICE_ID);
            assertNotNull(fetch);
            assertEquals(INVOICE_ID, fetch.get("id"));
            assertEquals("pending", fetch.get("email_status"));
            String cancelRequest = getHost(String.format(Constants.INVOICE_CANCEL, INVOICE_ID));
            verifySentRequest(false, null, cancelRequest);
        } catch (IOException e) {
            assertTrue(false);
        }
    }

    /**
     * Notify customer via SMS or email.
     */
    @Test
    public void notifyBy() throws RazorpayException {
        String mockedResponseJson = "{\n" +
                "    \"entity\" : \"invoice\",\n" +
                "    \"success\": true\n" +
                "}";

        try {
            mockResponseFromExternalClient(mockedResponseJson);
            mockResponseHTTPCodeFromExternalClient(200);
            Invoice fetch = invoiceClient.notifyBy(INVOICE_ID, "sms");
            assertNotNull(fetch);
            assertTrue(fetch.has("success"));
            assertTrue(fetch.has("entity"));
            String notifyByRequest = getHost(String.format(Constants.INVOICE_NOTIFY, INVOICE_ID, "sms"));
            verifySentRequest(false, null, notifyByRequest);
        } catch (IOException e) {
            assertTrue(false);
        }
    }

    /**
     * Create a recurring payment registration link.
     */
    @Test
    public void createRegistrationLink() throws RazorpayException {
        JSONObject request = new JSONObject("{\n" +
                "  \"email\": \"gaurav.kumar@example.com\",\n" +
                "  \"contact\": \"9123456789\",\n" +
                "  \"amount\": 1000,\n" +
                "  \"currency\": \"INR\",\n" +
                "  \"order_id\": \"order_1Aa00000000002\",\n" +
                "  \"customer_id\": \"cust_1Aa00000000001\",\n" +
                "  \"token\": \"token_1Aa00000000001\",\n" +
                "  \"recurring\": \"1\",\n" +
                "  \"description\": \"Creating recurring payment for Gaurav Kumar\"\n" +
                "}");

        String mockedResponseJson = "{\n" +
                "   \"entity\" : \"invoice\",\n" +
                "   \"razorpay_payment_id\" : \"pay_1Aa00000000001\",\n" +
                "   \"razorpay_order_id\" : \"order_1Aa00000000001\",\n" +
                "   \"razorpay_signature\" : \"signature\"\n" +
                "}";

        try {
            mockResponseFromExternalClient(mockedResponseJson);
            mockResponseHTTPCodeFromExternalClient(200);
            Invoice fetch = invoiceClient.createRegistrationLink(request);
            assertNotNull(fetch);
            assertEquals("invoice", fetch.get("entity"));
            assertTrue(fetch.has("razorpay_payment_id"));
            assertTrue(fetch.has("razorpay_order_id"));
            assertTrue(fetch.has("razorpay_signature"));
            String createRegistrationLinkRequest = getHost(Constants.SUBSCRIPTION_REGISTRATION_LINK);
            verifySentRequest(true, request.toString(), createRegistrationLinkRequest);
        } catch (IOException e) {
            assertTrue(false);
        }
    }

    /**
     * Issue an invoice from draft.
     */
    @Test
    public void issue() throws RazorpayException {
        String mockedResponseJson = "{\n" +
                "  \"id\": \"" + INVOICE_ID + "\",\n" + // ✅ quoted
                "  \"entity\": \"invoice\",\n" +
                "  \"status\": \"issued\"\n" +
                "}";

        try {
            mockResponseFromExternalClient(mockedResponseJson);
            mockResponseHTTPCodeFromExternalClient(200);
            Invoice fetch = invoiceClient.issue(INVOICE_ID);
            assertNotNull(fetch);
            assertEquals(INVOICE_ID, fetch.get("id"));
            assertEquals("invoice", fetch.get("entity"));
            String issueRequest = getHost(String.format(Constants.INVOICE_ISSUE, INVOICE_ID));
            verifySentRequest(false, null, issueRequest);
        } catch (IOException e) {
            assertTrue(false);
        }
    }

    /**
     * Edit an invoice.
     */
    @Test
    public void edit() throws RazorpayException {
        JSONObject request = new JSONObject("{\n  \"notes\": {\n    \"updated-key\": \"An updated note.\"\n  }\n}");

        String mockedResponseJson = "{\n" +
                "  \"id\": \"" + INVOICE_ID + "\",\n" + // ✅ quoted
                "  \"entity\": \"invoice\",\n" +
                "  \"status\": \"draft\"\n" +
                "}";

        try {
            mockResponseFromExternalClient(mockedResponseJson);
            mockResponseHTTPCodeFromExternalClient(200);
            Invoice invoice = invoiceClient.edit(INVOICE_ID, request);
            assertNotNull(invoice);
            assertEquals(INVOICE_ID, invoice.get("id"));
            assertEquals("invoice", invoice.get("entity"));
            String editRequest = getHost(String.format(Constants.INVOICE_GET, INVOICE_ID));
            verifySentRequest(true, request.toString(), editRequest);
        } catch (IOException e) {
            assertTrue(false);
        }
    }

    /**
     * Delete an invoice.
     */
    @Test
    public void testDeleteInvoice() throws IOException, RazorpayException {
        // ✅ Fixed: proper empty JSON array
        String mockedResponseJson = "[]";
        mockResponseFromExternalClient(mockedResponseJson);
        mockResponseHTTPCodeFromExternalClient(200);
        List<Invoice> invoice = invoiceClient.delete(INVOICE_ID);
        assertNotNull(invoice);
        String editRequest = getHost(String.format(Constants.INVOICE_GET, INVOICE_ID));
        verifySentRequest(false, null, editRequest);
    }
}
