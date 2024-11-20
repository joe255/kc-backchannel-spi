<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
	<#if section = "header">
		${msg("captchaAuthTitle",realm.displayName)}
	<#elseif section = "show-username">
		<h1>${msg("captchaAuthCodeTitle", realm.displayName)}</h1>
	<#elseif section = "form">
		<form id="kc-captcha-code-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
			<div class="${properties.kcFormGroupClass!}">
				<div class="${properties.kcLabelWrapperClass!}">
					<label for="captchaQuestion" class="${properties.kcLabelClass!}">Captcha:</label>
				</div>
				<div class="${properties.kcLabelWrapperClass!}">
					<label for="captchaQuestion" class="${properties.kcLabelClass!}">${challenge}</label>
				</div>
				<div class="${properties.kcLabelWrapperClass!}">
					<label for="answer" class="${properties.kcLabelClass!}">${msg("captchaAuthLabel")}</label>
				</div>
				<div class="${properties.kcInputWrapperClass!}">
					<input type="text" id="answer" name="answer" class="${properties.kcInputClass!}" autofocus/>
				</div>
			</div>
			<div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
				<div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
					<input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSubmit")}"/>
				</div>
			</div>
		</form>
	<#elseif section = "info" >
		${msg("captchaAuthInstruction")}
	</#if>
</@layout.registrationLayout>
