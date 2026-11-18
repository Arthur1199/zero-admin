<!DOCTYPE html>
<html>
<head>
	<#-- import macro -->
	<#import "../framework/common/common.macro.ftl" as netCommon>

	<!-- 1-style start -->
	<@netCommon.commonStyle />
	<link rel="stylesheet" href="${request.contextPath}/static/framework/log-detail.css?v=${I18n.admin_version}">
	<!-- 1-style end -->

</head>
<body class="hold-transition skin-blue layout-top-nav" >
<div class="wrapper zero-job-detail-background zero-job-detail-layout" >
	<@netCommon.jobBackground />

	<!-- 2-header start -->
	<header class="main-header">
		<nav class="navbar navbar-static-top">
			<div class="container">
				<#-- icon -->
				<div class="navbar-header">
					<a class="navbar-brand" href="javascript:void(0);" ><b>${I18n.joblog_rolling_log}</b> Console</a>
					<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-collapse">
						<i class="fa fa-bars"></i>
					</button>
				</div>

				<#-- left nav -->
				<div class="collapse navbar-collapse pull-left" id="navbar-collapse">
					<ul class="nav navbar-nav">
						<li class="active" >
							<a href="javascript:;">${I18n.jobinfo_job}：${jobName?html}</a>
						</li>
					</ul>
				</div>

				<#-- right nav -->
				<div class="navbar-custom-menu">
					<ul class="nav navbar-nav">
						<li>
							<a href="javascript:window.location.reload();" >
								<i class="fa fa-fw fa-refresh" ></i>
								${I18n.joblog_rolling_log_refresh}
							</a>
						</li>
						<li>
							<a href="javascript:window.close();" >
								<i class="fa fa-fw fa-close" ></i>
								${I18n.system_close}
							</a>
						</li>
					</ul>
				</div>

			</div>
		</nav>
	</header>
	<!-- 2-header end -->

	<!-- 3-content start -->
	<div class="content-wrapper">
		<section class="content">
			<!-- rolling log -->
			<div class="job-log-console">
				<pre id="logConsole" hidden></pre>
				<div id="logConsoleStatus" role="status"></div>
				<i class="fa fa-refresh fa-spin" id="logConsoleRunning" aria-hidden="true"></i>
			</div>
		</section>
	</div>
	<!-- 3-content end -->

	<!-- 4-footer start -->
	<footer class="main-footer zero-job-console-footer">
		Powered by <b>ZERO-JOB</b> ${I18n.admin_version}
		<div class="pull-right hidden-xs">
			<strong>Copyright &copy; 2026<#if .now?string("yyyy") != "2026">-${.now?string("yyyy")}</#if> ZERO-JOB
			</strong><!-- All rights reserved. -->
		</div>
	</footer>
	<!-- 4-footer end -->

</div>

<!-- 5-script start -->
<@netCommon.commonScript />
<script src="${request.contextPath}/static/framework/admin.setting.js?v=${I18n.admin_version}"></script>
<script>
	$(function () {
		var triggerCode = Number('${triggerCode}');
		var handleCode = Number('${handleCode}');
		var logId = '${logId?c}';
		var output = $('#logConsole');
		var status = $('#logConsoleStatus');
		var spinner = $('#logConsoleRunning');
		var fromLineNum = 1;
		var failureCount = 0;
		var maxFailures = 20;
		var pollingDelay = 3000;
		var timer;
		var request;
		var suspended = false;
		var finished = false;

		function setStatus(message, error) {
			status.toggleClass('text-danger', error).text(message || '');
		}

		function stop(message, error) {
			finished = true;
			spinner.hide();
			setStatus(message, error);
		}

		function reportFailure(message) {
			if (++failureCount >= maxFailures) {
				stop(I18n.joblog_rolling_log_failoften, true);
			} else {
				setStatus(message || I18n.system_opt_fail, true);
			}
		}

		function pullLog() {
			request = $.ajax({
				type: 'POST',
				url: base_url + '/joblog/logDetailCat',
				data: { logId: logId, fromLineNum: fromLineNum, plainText: true },
				dataType: 'json'
			}).done(function (response) {
				var data = response && response.data;
				if (!response || response.code !== 200) {
					reportFailure(response && response.msg);
					return;
				}
				if (!data || data.fromLineNum !== fromLineNum || !Number.isInteger(data.toLineNum)) {
					reportFailure();
					return;
				}

				// 没有新日志是正常状态，只有连续请求失败才计入退出阈值。
				failureCount = 0;
				setStatus('', false);
				var hasNewLines = data.toLineNum >= fromLineNum;
				if (hasNewLines) {
					fromLineNum = data.toLineNum + 1;
				}
				if ((hasNewLines || data.end) && typeof data.logContent === 'string' && data.logContent.length) {
					var viewport = document.scrollingElement;
					var followTail = viewport.scrollHeight - viewport.scrollTop - viewport.clientHeight < 80;
					output.prop('hidden', false);
					output[0].appendChild(document.createTextNode(data.logContent));
					if (followTail) {
						viewport.scrollTop = viewport.scrollHeight;
					}
				}
				if (data.end) {
					stop(I18n.joblog_rolling_log_finished || 'Rolling Log End', false);
				}
			}).fail(function (_, reason) {
				if (reason !== 'abort') {
					reportFailure();
				}
			}).always(function () {
				request = null;
				if (!suspended && !finished) {
					timer = window.setTimeout(pullLog, pollingDelay);
				}
			});
		}

		if (triggerCode !== 200 && handleCode === 0) {
			stop(I18n.joblog_rolling_log_triggerfail, true);
			return;
		}

		$(window).on('pagehide.jobLog', function () {
			suspended = true;
			window.clearTimeout(timer);
			if (request) {
				request.abort();
			}
		}).on('pageshow.jobLog', function () {
			if (suspended && !finished) {
				suspended = false;
				pullLog();
			}
		});
		pullLog();
	});
</script>
<!-- 5-script end -->

</body>
</html>
