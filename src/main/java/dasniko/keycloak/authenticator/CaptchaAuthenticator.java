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
public class CaptchaAuthenticator implements Authenticator {

	private static final String CAPTCHA_FORM = "login-captcha.ftl";

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		AuthenticatorConfigModel config = context.getAuthenticatorConfig();

		// mobileNumber of course has to be further validated on proper format, country
		// code, ...

		int ttl = Integer.parseInt(config.getConfig().get(CaptchaConstants.CAPTCHA_TTL));

		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		authSession.setAuthNote(CaptchaConstants.CAPTCHA_TTL,
				Long.toString(System.currentTimeMillis() + (ttl * 1000L)));
		CaptchaAuthenticatorFactory.JEDISSINGLETON.set(authSession.getTabId(), "0", SetParams.setParams().ex(ttl));

		String captchaQuestion = SecretGenerator.getInstance().randomString(10, SecretGenerator.ALPHANUM);

		authSession.setAuthNote(CaptchaConstants.CAPTCHA_QUESTION, captchaQuestion);

		try {
			context.challenge(context.form().setAttribute("realm", context.getRealm())
					.setAttribute("challenge", captchaQuestion).createForm(CAPTCHA_FORM));
		} catch (Exception e) {
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
					context.form().setError("smsAuthSmsNotSent", e.getMessage())
							.createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
		}
	}

	@Override
	public void action(AuthenticationFlowContext context) {
		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		String ttl = authSession.getAuthNote(CaptchaConstants.CAPTCHA_TTL);

		if (ttl == null) {
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
					context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
			return;
		}
		boolean isValid = authSession.getAuthNote(CaptchaConstants.CAPTCHA_QUESTION).equals(
				context.getHttpRequest().getDecodedFormParameters().getFirst(CaptchaConstants.CAPTCHA_ANSWER));
		if (isValid) {
			if (Long.parseLong(ttl) < System.currentTimeMillis()) {
				// expired
				context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
						context.form().setError("captchaAuthCodeExpired").createErrorPage(Response.Status.BAD_REQUEST));
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
								.setAttribute("challenge", authSession.getAuthNote(CaptchaConstants.CAPTCHA_QUESTION))
								.createForm(CAPTCHA_FORM));
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
