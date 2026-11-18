<!DOCTYPE html>
<html>
<head>
	<#-- import macro -->
	<#import "../common/common.macro.ftl" as netCommon>

	<!-- 1-style start -->
	<@netCommon.commonStyle />
	<link rel="stylesheet" href="${request.contextPath}/static/framework/login.css?v=${I18n.admin_version}&amp;layout=4">
	<!-- 1-style end -->

</head>
<body class="hold-transition login-page zero-job-login">

	<@netCommon.jobBackground />

	<!-- 2-biz start -->
	<form id="loginForm" class="login-box" method="post">
		<div class="login-box-body">
			<h3 class="login-box-msg">ZERO-JOB</h3>
			<div class="form-group has-feedback">
				<input type="text" name="userName" class="form-control" placeholder="${I18n.login_username_placeholder}" aria-label="${I18n.login_username_placeholder}" autocomplete="username" maxlength="20">
				<span class="glyphicon glyphicon-user form-control-feedback" aria-hidden="true"></span>
			</div>
			<div class="form-group has-feedback login-password-field">
				<input id="loginPassword" type="password" name="password" class="form-control" placeholder="${I18n.login_password_placeholder}" aria-label="${I18n.login_password_placeholder}" autocomplete="current-password" maxlength="20">
				<span class="glyphicon glyphicon-lock form-control-feedback" aria-hidden="true"></span>
				<button type="button" class="login-password-toggle" aria-controls="loginPassword" aria-label="${I18n.login_password_show!I18n.login_password_placeholder}" aria-pressed="false">
					<i class="fa fa-eye-slash" aria-hidden="true"></i>
				</button>
			</div>
			<label class="login-remember">
				<input type="checkbox" name="ifRemember"> ${I18n.login_remember_me}
			</label>
			<button type="submit" class="btn btn-primary btn-block">${I18n.login_btn}</button>
		</div>
	</form>
	<!-- 2-biz end -->

<!-- 3-script start -->
<@netCommon.commonScript />
<script>
$(function () {

	$('.login-password-toggle').on('click', function () {
		var password = document.getElementById('loginPassword');
		var showPassword = password.type === 'password';
		password.type = showPassword ? 'text' : 'password';
		$(this).attr('aria-pressed', String(showPassword))
			.find('i').toggleClass('fa-eye', showPassword).toggleClass('fa-eye-slash', !showPassword);
	});

	// login Form Valid
	var loginFormValid = $("#loginForm").validate({
		errorElement : 'span',
		errorClass : 'help-block',
		focusInvalid : true,
		rules : {
			userName : {
				required : true ,
				minlength: 4,
				maxlength: 20
			},
			password : {
				required : true ,
				minlength: 4,
				maxlength: 20
			}
		},
		messages : {
			userName : {
				required  : I18n.login_username_empty,
				minlength : I18n.login_username_lt_4
			},
			password : {
				required  : I18n.login_password_empty  ,
				minlength : I18n.login_password_lt_4
				/*,maxlength:"登录密码不应超过18位"*/
			}
		},
		highlight : function(element) {
			$(element).closest('.form-group').addClass('has-error');
		},
		success : function(label) {
			label.closest('.form-group').removeClass('has-error');
			label.remove();
		},
		errorPlacement : function(error, element) {
			element.parent('div').append(error);
		},
		submitHandler : function(form) {
			$.post(base_url + "/auth/doLogin", $("#loginForm").serialize(), function(data, status) {
				if (data.code === 200) {
					layer.msg( I18n.login_success );
					setTimeout(function(){
						window.location.href = base_url + "/";
					}, 500);
				} else {
					layer.open({
						title: I18n.system_tips,
						btn: [ I18n.system_ok ],
						content: (data.msg || I18n.login_fail ),
						icon: '2'
					});
				}
			});
		}
	});

});
</script>
<!-- 3-script end -->


</body>
</html>
