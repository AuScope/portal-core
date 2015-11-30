package org.auscope.portal.core.services.cloud;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.PortalServiceException;

/**
 * This class was created due to our inability to use the AWS Java SDK due to a number of dependency clashes.
 *
 * it implements a tiny subset of the EC2 API that JClouds doesn't
 *
 * @see https://jira.csiro.au/browse/ANVGL-3
 * @author Josh Vote (CSIRO)
 *
 */
public class SimpleEC2Client {

    private final Log logger = LogFactory.getLog(getClass());

    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded; charset=utf-8";
    private static final String SIGNED_HEADERS = "content-type;host;x-amz-date";

    private HttpServiceCaller httpService;
    private String region;
    private String accessKey;
    private String secretKey;
    private String service = "ec2";

    public SimpleEC2Client(HttpServiceCaller httpService, String accessKey, String secretKey, String region) {
        super();
        this.httpService = httpService;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    protected String getService() {
        return service;
    }

    protected void setService(String service) {
        this.service = service;
    }

    protected String getRegion() {
        return region;
    }

    protected void setRegion(String region) {
        this.region = region;
    }

    private String sha256Hash(String data) {
        MessageDigest crypt;
        try {
            crypt = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            logger.error("sha256 Algorithm not available. Unable to hash EC2 requests.", e);
            throw new RuntimeException("sha256 Algorithm not available. Unable to hash EC2 requests");
        }
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

        crypt.update(dataBytes);

        return new String(Hex.encodeHex(crypt.digest(), true));
    }

    private byte[] HmacSHA256(String data, byte[] key) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {
        String algorithm = "HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes("UTF8"));
    }

    private byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {
        byte[] kSecret = ("AWS4" + key).getBytes("UTF8");
        byte[] kDate = HmacSHA256(dateStamp, kSecret);
        byte[] kRegion = HmacSHA256(regionName, kDate);
        byte[] kService = HmacSHA256(serviceName, kRegion);
        byte[] kSigning = HmacSHA256("aws4_request", kService);
        return kSigning;
    }

    private String getISO8601DateString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    private String getSimpleDateString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    private String generateEndpoint() {
        if (region == null) {
            return  "http://" + service + ".amazonaws.com/";
        } else {
            return "http://" + service + "." + region + ".amazonaws.com/";
        }
    }

    /**
     * Generates a canonical request as per: http://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
     * @param uri
     * @param date
     * @param payload
     * @return
     */
    protected String generateCanonicalRequest(URI uri, Date date, String payload) {
        StringBuilder canonicalRequest = new StringBuilder();
        canonicalRequest.append("GET\n"); //HTTPRequestMethod
        canonicalRequest.append("/\n"); //CanonicalURI
        canonicalRequest.append(uri.getQuery()); //CanonicalQueryString
        canonicalRequest.append("\n");
        canonicalRequest.append("content-type:");
        canonicalRequest.append(CONTENT_TYPE);
        canonicalRequest.append("\n");
        canonicalRequest.append("host:");
        canonicalRequest.append(uri.getHost());
        canonicalRequest.append("\n");
        canonicalRequest.append("x-amz-date:");
        canonicalRequest.append(getISO8601DateString(date));
        canonicalRequest.append("\n");
        canonicalRequest.append(payload); //Payload
        canonicalRequest.append("\n");
        canonicalRequest.append(SIGNED_HEADERS);
        canonicalRequest.append("\n");
        canonicalRequest.append(sha256Hash(payload)); //Hashed payload
        return canonicalRequest.toString();
    }

    /**
     * Generates a string to sign as per: http://docs.aws.amazon.com/general/latest/gr/sigv4-create-string-to-sign.html
     * @param date
     * @param hashedCanonicalRequest
     * @return
     */
    protected String generateStringToSign(Date date, String hashedCanonicalRequest) {
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append("AWS4-HMAC-SHA256\n");
        stringToSign.append(getISO8601DateString(date));
        stringToSign.append("\n");
        stringToSign.append(getSimpleDateString(date));
        stringToSign.append("/");
        stringToSign.append(region);
        stringToSign.append("/");
        stringToSign.append(service);
        stringToSign.append("/aws4_request\n");
        stringToSign.append(hashedCanonicalRequest);

        return stringToSign.toString();
    }

    /**
     * Generates an AWS signature as per: http://docs.aws.amazon.com/general/latest/gr/sigv4-calculate-signature.html
     * @param stringToSign
     * @param date
     * @return
     * @throws PortalServiceException
     */
    protected String generateSignature(String stringToSign, Date date) throws PortalServiceException {
        try {
            byte[] signingKeyBytes = getSignatureKey(secretKey, getSimpleDateString(date), region, service);
            byte[] signatureBytes = HmacSHA256(stringToSign, signingKeyBytes);
            return new String(Hex.encodeHex(signatureBytes, true));
        } catch (Exception e) {
            logger.error("Error creating EC2 signing key", e);
            throw new PortalServiceException("Error creating EC2 signing key", e);
        }
    }

    /**
     * Generates an authorization header for an AWS request as per: http://docs.aws.amazon.com/general/latest/gr/sigv4-add-signature-to-request.html
     * @param signature
     * @param date
     * @return
     */
    protected String generateAuthorizationHeader(String signature, Date date) {
        StringBuilder authorization = new StringBuilder();
        authorization.append("AWS4-HMAC-SHA256 Credential=");
        authorization.append(accessKey);
        authorization.append("/");
        authorization.append(getSimpleDateString(date));
        authorization.append("/");
        authorization.append(region);
        authorization.append("/");
        authorization.append(service);
        authorization.append("/aws4_request, SignedHeaders=");
        authorization.append(SIGNED_HEADERS);
        authorization.append(", Signature=");
        authorization.append(signature);

        return authorization.toString();
    }

    protected HttpGet makeRequest(String action, Date date, Map<String, String> params) throws URISyntaxException, PortalServiceException {
        HttpGet method = new HttpGet();

        URIBuilder builder = new URIBuilder(generateEndpoint());
        builder.setParameter("action", action); //The access token I am getting after the Login
        builder.setParameter("version", "2010-05-08");
        for (String key : params.keySet()) {
            builder.setParameter(key, params.get(key));
        }


        URI uri = builder.build();
        String host = uri.getHost();
        String contentType = "application/x-www-form-urlencoded; charset=utf-8";
        String payload = ""; //(we are using GET - so empty string is the correct value)

        String canonicalRequest = generateCanonicalRequest(uri, date, payload);
        String hashedCanonicalRequest = sha256Hash(canonicalRequest);
        String stringToSign = generateStringToSign(date, hashedCanonicalRequest);
        String signature = generateSignature(stringToSign, date);
        String authorization = generateAuthorizationHeader(signature, date);

        method.setHeader("Host", host);
        method.setHeader("Content-Type", contentType);
        method.setHeader("X-Amz-Date", getISO8601DateString(date));
        method.setHeader("Authorization", authorization.toString());
        method.setURI(uri);
        return method;
    }

    /**
     * Sets the instanceInitiatedShutdownBehavior attribute for a given running instance
     * @param instanceId
     * @param behaviour
     */
    public void setInstanceInitiatedShutdownBehaviour(String instanceId, InstanceInitiatedShutdownBehaviour behaviour) throws PortalServiceException {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("InstanceId", instanceId);
        switch(behaviour) {
        case Stop:
            params.put("InstanceInitiatedShutdownBehavior.Value", "stop");
            break;
        case Terminate:
            params.put("InstanceInitiatedShutdownBehavior.Value", "terminate");
            break;
        }

        try {
            HttpGet request = makeRequest("ModifyInstanceAttribute", new Date(), params);
            String response = httpService.getMethodResponseAsString(request);
            if (!response.contains("<return>true</return>")) { //This could be so much more robust...
                logger.debug("Error response from AWS: " + response);
                throw new IOException("AWS has returned an error");
            }

        } catch (Exception ex) {
            logger.error("Error setting instanceInitiatedShutdownBehavior for " + instanceId);
            logger.debug("Exception: ", ex);
            throw new PortalServiceException("Error setting instanceInitiatedShutdownBehavior for " + instanceId, ex);
        }
    }

    /**
     * Gets the console logs as reported by AWS for the specified instance.
     * @param instanceId
     * @throws PortalServiceException
     */
    public String getConsoleOutput(String instanceId) throws PortalServiceException {
        throw new NotImplementedException();
    }

    public enum InstanceInitiatedShutdownBehaviour {
        Terminate,
        Stop
    }
}
