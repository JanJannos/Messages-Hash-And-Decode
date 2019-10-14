
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.core.Application;
import javax.ws.rs.*;

@ApplicationPath("/")
public class MessagesService extends Application {

	HashMap<String, String> persistentMessages = new HashMap<String, String>();
	
	/**
	 * Hash a message
	 * Example : localhost:8080/MessagesHashing?messageToEncode={some message}
	 * @param messageToEncode
	 * @return
	 */
	@POST
	@Path("/messages")
	@Produces("application/json")
	public String encodeMessage(@QueryParam("messageToEncode") String messageToEncode) {

		String failed = "";
		try {

			if (!persistentMessages.containsKey(messageToEncode)) {

				// If the message is not listed already , then hash it 
				// and store in HashMap
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				md.update(messageToEncode.getBytes(StandardCharsets.UTF_8));
				byte[] digest = md.digest();

				String hex = String.format("%064x", new BigInteger(1, digest));
				persistentMessages.put(messageToEncode, hex);
				return hex;
			} else {
				String hashedListed = persistentMessages.get(messageToEncode);
				return hashedListed;
			}

		}

		catch (Exception e) {
			failed = e.getMessage();
		}

		return failed;

	}


	/**
	 * Get a key by value
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @param value
	 * @return
	 */
	private <K, V> K getKey(Map<K, V> map, V value) {
		for (Entry<K, V> entry : map.entrySet()) {
			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Decode a hashed message (from SHA256)
	 * localhost:8080/MessagesHashing/{hashedMessage} ....
	 * @param hashedMessage
	 * @return
	 */
	@GET
	@Path("/messages/{hashedMessage}")
	@Produces("application/json")
	public String decodeHashedMessage(@PathParam("hashedMessage") String hashedMessage) {

		// Check if the hashed value is listed
		if (persistentMessages.containsValue(hashedMessage)) {

			// Value exists - return the KEY
			String value = getKey(this.persistentMessages, hashedMessage);
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}
		
		// Else , return an error message
		return "404 - Message not found";
	}

}