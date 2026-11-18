<!DOCTYPE html>
<html>
<head>
	<#-- import macro -->
	<#import "../common/common.macro.ftl" as netCommon>

	<!-- 1-style start -->
	<@netCommon.commonStyle />
	<link rel="stylesheet" href="${request.contextPath}/static/framework/help.css?v=${I18n.admin_version}">
	<!-- 1-style end -->

</head>
<body class="hold-transition zero-job-tab-page">
<div class="wrapper">
	<section class="content">

		<#-- 2-biz start -->
		<section class="zero-job-help-card" aria-labelledby="help-title">
			<h1 id="help-title">${I18n.admin_name_full}</h1>
			<a class="zero-job-help-link" target="_blank" rel="noopener" href="https://www.xuxueli.com/xxl-job/">
				<i class="fa fa-book" aria-hidden="true"></i>
				${I18n.admin_help_document}
				<i class="fa fa-external-link" aria-hidden="true"></i>
			</a>
		</section>
		<#-- 2-biz end -->

	</section>
</div>

<!-- 3-script start -->
<@netCommon.commonScript />
<!-- 3-script end -->

</body>
</html>
