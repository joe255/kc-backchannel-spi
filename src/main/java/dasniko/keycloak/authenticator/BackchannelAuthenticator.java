package dasniko.keycloak.authenticator;

import dasniko.keycloak.authenticator.gateway.SmsServiceFactory;
import jakarta.ws.rs.core.Response;
import redis.clients.jedis.params.SetParams;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
public class BackchannelAuthenticator implements Authenticator {

	private static final String BACKCHANNEL_FORM = "login-backchannel.ftl";

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		AuthenticatorConfigModel config = context.getAuthenticatorConfig();

		// mobileNumber of course has to be further validated on proper format, country
		// code, ...

		int ttl = Integer.parseInt(config.getConfig().get(BackchannelConstants.BACKCHANNEL_TTL));

		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		authSession.setAuthNote(SmsConstants.CODE_TTL, Long.toString(System.currentTimeMillis() + (ttl * 1000L)));
		BackchannelAuthenticatorFactory.JEDISSINGLETON.set(authSession.getTabId(), "0", SetParams.setParams().ex(ttl));
		try {
			context.challenge(context.form().setAttribute("realm", context.getRealm()).createForm(BACKCHANNEL_FORM));
		} catch (Exception e) {
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
					context.form().setError("smsAuthSmsNotSent", e.getMessage())
							.createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
		}
	}

	@Override
	public void action(AuthenticationFlowContext context) {
		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		String ttl = authSession.getAuthNote(BackchannelConstants.BACKCHANNEL_TTL);

		if (ttl == null) {
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
					context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
			return;
		}

		boolean isValid = BackchannelAuthenticatorFactory.JEDISSINGLETON.get(authSession.getTabId()).equals("1");
		if (isValid) {
			if (Long.parseLong(ttl) < System.currentTimeMillis()) {
				// expired
				context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
						context.form().setError("smsAuthCodeExpired").createErrorPage(Response.Status.BAD_REQUEST));
			} else {
				// valid
				context.success();
			}
		} else {
			// invalid
			AuthenticationExecutionModel execution = context.getExecution();
			if (execution.isRequired()) {
				context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
						context.form().setAttribute("realm", context.getRealm())
								.createForm(BACKCHANNEL_FORM));
			} else if (execution.isConditional() || execution.isAlternative()) {
				context.attempted();
			}
		}
	}

	@Override
	public boolean requiresUser() {
		return true;
	}

	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		return true; // TODO: user has app enabled
	}

	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
		// this will only work if you have the required action from here configured:
		// https://github.com/dasniko/keycloak-extensions-demo/tree/main/requiredaction
		// user.addRequiredAction("mobile-number-ra");
	}

	@Override
	public void close() {
	}

}
