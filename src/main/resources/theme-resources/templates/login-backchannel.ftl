<#import "template.ftl" as layout>
<style>
	.loader {
		border: 16px solid #f3f3f3; /* Light grey */
		border-top: 16px solid #3498db; /* Blue */
		border-radius: 50%;
		width: 120px;
		height: 120px;
		animation: spin 2s linear infinite;
	  }

	  @keyframes spin {
		0% { transform: rotate(0deg); }
		100% { transform: rotate(360deg); }
	  }
</style>
<!--javascript to submit the form every 15 seconds-->
<script>
setInterval(function() {
	document.getElementById("kc-backchannel-login-form").submit();
}, 15000);
</script>
<@layout.registrationLayout displayInfo=true; section>
	<#if section = "header">
		${msg("backchannelTitle",realm.displayName)}
	<#elseif section = "show-username">
		<h1>${msg("backchannelSpinnerTitle", realm.displayName)}</h1>
	<#elseif section = "form">
		<form id="kc-backchannel-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
			<div class="${properties.kcFormGroupClass!}">
				<!--A loading spinner-->
				<div class="loader"></div>
			</div>
			<div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
				<div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}" hidden>
					<input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSubmit")}"/>
				</div>
			</div>
		</form>
	<#elseif section = "info" >
		${msg("smsAuthInstruction")}
	</#if>
</@layout.registrationLayout>
