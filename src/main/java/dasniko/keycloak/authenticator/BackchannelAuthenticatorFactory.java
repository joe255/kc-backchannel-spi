package dasniko.keycloak.authenticator;

import com.google.auto.service.AutoService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
@AutoService(AuthenticatorFactory.class)
public class BackchannelAuthenticatorFactory implements AuthenticatorFactory {

	public static final String PROVIDER_ID = "backchannel-authenticator";

	private static final BackchannelAuthenticator SINGLETON = new BackchannelAuthenticator();
	public static Jedis JEDISSINGLETON = null;

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getDisplayType() {
		return "Backchannel Authentication";
	}

	@Override
	public String getHelpText() {
		return "Validates a backchannel authentication request.";
	}

	@Override
	public String getReferenceCategory() {
		return "backchannel";
	}

	@Override
	public boolean isConfigurable() {
		return true;
	}

	@Override
	public boolean isUserSetupAllowed() {
		return true;
	}

	@Override
	public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
		return REQUIREMENT_CHOICES;
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return List.of(
				new ProviderConfigProperty(BackchannelConstants.BACKCHANNEL_TTL, "Time-to-live",
						"The time to live in seconds for the code to be valid.", ProviderConfigProperty.STRING_TYPE,
						"300"));
	}

	@Override
	public Authenticator create(KeycloakSession session) {
		return SINGLETON;
	}

	@SuppressWarnings("resource")
	@Override
	public void init(Config.Scope config) {
		JEDISSINGLETON = new JedisPool("redis", 6379).getResource();
	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {
	}

	@Override
	public void close() {
	}

}
