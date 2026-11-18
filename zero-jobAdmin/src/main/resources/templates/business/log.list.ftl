<!DOCTYPE html>
<html>
<head>
	<#-- import macro -->
	<#import "../framework/common/common.macro.ftl" as netCommon>

	<!-- 1-style start -->
	<#-- select2：should before adminlte -->
	<link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/select2/select2.min.css">
	<@netCommon.commonStyle />
	<link rel="stylesheet" href="${request.contextPath}/static/plugins/bootstrap-table/bootstrap-table.min.css">
	<!-- daterangepicker -->
	<link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/bootstrap-daterangepicker/daterangepicker.css">
	<link rel="stylesheet" href="${request.contextPath}/static/framework/date-range.css?v=${I18n.admin_version}">
	<link rel="stylesheet" href="${request.contextPath}/static/framework/log-remark.css?v=${I18n.admin_version}&amp;layout=3">
	<!-- 1-style end -->

</head>
<body class="hold-transition zero-job-tab-page">
<div class="wrapper">
	<template id="standaloneLogBackground">
		<@netCommon.jobBackground />
	</template>
	<script>
		// 报表下钻会打开独立日志页，后台页签则继续使用父页面背景。
		if (window.self === window.top) {
			var backgroundTemplate = document.getElementById('standaloneLogBackground');
			backgroundTemplate.parentElement.classList.add('zero-job-detail-background', 'zero-job-detail-layout');
			backgroundTemplate.replaceWith(backgroundTemplate.content);
		}
	</script>
	<section class="content">

		<!-- 2-content start -->

		<#-- 查询区域 -->
		<div class="box" style="margin-bottom:9px;">
			<div class="box-body">
				<div class="row" id="data_filter" >

					<div class="col-xs-2">
						<div class="input-group">
							<span class="input-group-addon">${I18n.jobinfo_field_jobgroup}</span>
							<select class="form-control" id="jobGroup"  >
								<#list JobGroupList as group>
									<option value="${group.id?c}" <#if jobGroup==group.id>selected</#if> >${(group.name!'')?html}</option>
								</#list>
							</select>
						</div>
					</div>
					<div class="col-xs-3">
						<div class="input-group">
							<span class="input-group-addon">${I18n.jobinfo_job}</span>
							<select class="form-control" id="jobId" >
								<option value="0" >${I18n.system_all}</option>
								<#if reportHistoricalJob>
									<option value="${jobId?c}">#${jobId?c}</option>
								</#if>
								<#if jobInfoList?size gt 0>
									<#list jobInfoList as jobItem>
										<option value="${jobItem.id?c}" >${(jobItem.name!'')?html}</option>
									</#list>
								</#if>
							</select>
						</div>
					</div>
					<div class="col-xs-1">
						<div class="input-group">
							<#--<span class="input-group-addon">${I18n.joblog_status}</span>-->
							<select class="form-control" id="logStatus" >
								<option value="-1" >${I18n.joblog_status_all}</option>
								<option value="1" >${I18n.joblog_status_suc}</option>
								<option value="2" >${I18n.joblog_status_fail}</option>
								<option value="3" >${I18n.joblog_status_running}</option>
							</select>
						</div>
					</div>
					<div class="col-xs-4">
						<div class="input-group">
                		<span class="input-group-addon">
	                  		${I18n.joblog_field_triggerTime}
	                	</span>
							<input type="text" class="form-control" id="filterTime" readonly
								data-today="${reportToday}"
								data-report-start="${reportStart!''}" data-report-end="${reportEnd!''}">
						</div>
					</div>

					<div class="col-xs-1">
						<button class="btn btn-block btn-primary searchBtn" >${I18n.system_search}</button>
					</div>
					<div class="col-xs-1">
						<button class="btn btn-block btn-default resetBtn" >${I18n.system_reset}</button>
					</div>
				</div>
			</div>
		</div>

		<#-- 数据表格区域 -->
		<div class="row">
			<div class="col-xs-12">
				<div class="box">
					<div class="box-header pull-left" id="data_operation" >
						<button class="btn btn-sm btn-warning selectOnlyOne logKill" type="button">${I18n.joblog_kill_log}</button>
						<button class="btn btn-sm btn-danger selectAny clearLog" type="button">${I18n.joblog_clean_log}</button>
						｜
						<button class="btn btn-sm btn-primary selectOnlyOne logDetail" type="button"><#--<i class="fa fa-edit"></i>-->${I18n.joblog_rolling_log}</button>
					</div>
					<div class="box-body" >
						<table id="data_list" class="table table-bordered table-striped" width="100%" >
							<thead></thead>
							<tbody></tbody>
							<tfoot></tfoot>
						</table>
					</div>
				</div>
			</div>
		</div>

		<!-- 日志清理.模态框 -->
		<div class="modal fade" id="clearLogModal" tabindex="-1" role="dialog"  aria-hidden="true">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">
						<h4 class="modal-title" >${I18n.joblog_clean_log}</h4>
					</div>
					<div class="modal-body">
						<form class="form-horizontal form" role="form" >
							<div class="form-group">
								<label class="col-sm-2 control-label">${I18n.jobinfo_field_jobgroup}：</label>
								<div class="col-sm-9">
									<input type="text" class="form-control jobGroupText" readonly >
									<input type="hidden" name="jobGroup" >
								</div>
							</div>

							<div class="form-group">
								<label class="col-sm-2 control-label">${I18n.jobinfo_job}：</label>
								<div class="col-sm-9">
									<input type="text" class="form-control jobIdText" readonly >
									<input type="hidden" name="jobId" >
								</div>
							</div>

							<div class="form-group">
								<label class="col-sm-2 control-label">${I18n.joblog_clean_type}：</label>
								<div class="col-sm-9">
									<select class="form-control" name="type" >
										<option value="1" >${I18n.joblog_clean_type_1}</option>
										<option value="2" >${I18n.joblog_clean_type_2}</option>
										<option value="3" >${I18n.joblog_clean_type_3}</option>
										<option value="4" >${I18n.joblog_clean_type_4}</option>
										<option value="5" >${I18n.joblog_clean_type_5}</option>
										<option value="6" >${I18n.joblog_clean_type_6}</option>
										<option value="7" >${I18n.joblog_clean_type_7}</option>
										<option value="8" >${I18n.joblog_clean_type_8}</option>
										<option value="9" >${I18n.joblog_clean_type_9}</option>
									</select>
								</div>
							</div>

							<hr>
							<div class="form-group">
								<div class="col-sm-offset-4 col-sm-4">
									<button type="button" class="btn btn-primary ok" >${I18n.system_ok}</button>
									<button type="button" class="btn btn-default" data-dismiss="modal">${I18n.system_cancel}</button>
								</div>
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>

		<!-- 2-content end -->

	</section>
</div>

<div class="modal fade" id="logRemarkModal" tabindex="-1" role="dialog" aria-labelledby="logRemarkTitle">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="${I18n.system_close}"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="logRemarkTitle"></h4>
			</div>
			<div class="modal-body">
				<textarea class="log-remark-text" id="logRemarkText" aria-labelledby="logRemarkTitle" readonly spellcheck="false"></textarea>
			</div>
			<div class="modal-footer">
				<span class="log-remark-status" id="logRemarkStatus" role="status"></span>
				<button type="button" class="btn btn-primary" id="copyLogRemark">
					<i class="fa fa-copy" aria-hidden="true"></i> ${I18n.joblog_copy_all!(I18n.system_opt_copy + (I18n.admin_i18n == 'en')?then(' ', '') + I18n.system_all)}
				</button>
				<button type="button" class="btn btn-default" data-dismiss="modal">${I18n.system_close}</button>
			</div>
		</div>
	</div>
</div>

<!-- 3-script start -->
<@netCommon.commonScript />
<script src="${request.contextPath}/static/plugins/bootstrap-table/bootstrap-table.min.js"></script>
<script src="${request.contextPath}/static/plugins/bootstrap-table/locale/<#if I18n.admin_i18n?? && I18n.admin_i18n == 'en'>bootstrap-table-en-US.min.js<#else>bootstrap-table-zh-CN.min.js</#if>"></script>
<#--daterangepicker-->
<script src="${request.contextPath}/static/adminlte/bower_components/moment/moment.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/bootstrap-daterangepicker/daterangepicker.js"></script>
<#-- select2 -->
<script src="${request.contextPath}/static/adminlte/bower_components/select2/select2.min.js"></script>
<#-- admin table -->
<script src="${request.contextPath}/static/framework/admin.table.js"></script>
<script>
	$(function() {

		// ---------------------- filter ----------------------

		// select2: init
		$('#jobGroup').select2();
		$('#jobId').select2();

		/**
		 * filter Time
		 */
		var rangesConf = {};
		var today = moment($('#filterTime').attr('data-today'), 'YYYY-MM-DD', true);
		rangesConf[I18n.daterangepicker_ranges_today] = [today.clone().startOf('day'), today.clone().endOf('day')];
		rangesConf[I18n.daterangepicker_ranges_yesterday] = [today.clone().subtract(1, 'days').startOf('day'), today.clone().subtract(1, 'days').endOf('day')];
		rangesConf[I18n.daterangepicker_ranges_this_month] = [today.clone().startOf('month'), today.clone().endOf('month')];
		rangesConf[I18n.daterangepicker_ranges_last_month] = [today.clone().subtract(1, 'months').startOf('month'), today.clone().subtract(1, 'months').endOf('month')];
		rangesConf[I18n.daterangepicker_ranges_recent_week] = [today.clone().subtract(1, 'weeks').startOf('day'), today.clone().endOf('day')];
		rangesConf[I18n.daterangepicker_ranges_recent_month] = [today.clone().subtract(1, 'months').startOf('day'), today.clone().endOf('day')];

		$('#filterTime').daterangepicker({
			autoApply:false,
			singleDatePicker:false,		// 范围选择 or 单时间选择
			showDropdowns:true,         // 年月 选择条件是否为下拉框
			timePicker: true,
			timePicker24Hour: true,
			timePickerSeconds: true,	// 时间选择是否显示秒
			opens : 'left', 			// 日期选择框的弹出位置
			ranges: rangesConf,
			locale : {
				format: 'YYYY-MM-DD HH:mm:ss',
				separator : ' - ',
				customRangeLabel : I18n.daterangepicker_custom_name ,
				applyLabel : I18n.system_ok ,
				cancelLabel : I18n.system_cancel ,
				fromLabel : I18n.daterangepicker_custom_starttime ,
				toLabel : I18n.daterangepicker_custom_endtime ,
				daysOfWeek : I18n.daterangepicker_custom_daysofweek.split(',') ,        // '日', '一', '二', '三', '四', '五', '六'
				monthNames : I18n.daterangepicker_custom_monthnames.split(',') ,        // '一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'
				firstDay : 1
			}/*,
			startDate: rangesConf[I18n.daterangepicker_ranges_today][0],
			endDate: rangesConf[I18n.daterangepicker_ranges_today][1]*/
		});

		// init filter
		var jobGroup = '${jobGroup?c}';
		var jobId = '${jobId?c}';
		var filterTime = $('#filterTime');
		filterTime.on('show.daterangepicker', function (_, picker) {
			picker.container.addClass('zero-job-date-picker');
		});
		var reportStart = filterTime.attr('data-report-start');
		var reportEnd = filterTime.attr('data-report-end');
		var fromReport = !!(reportStart && reportEnd);
		function resetFilter(){
			var picker = filterTime.data('daterangepicker');
			var range = fromReport
					? [moment(reportStart, 'YYYY-MM-DD').startOf('day'), moment(reportEnd, 'YYYY-MM-DD').endOf('day')]
					: rangesConf[I18n.daterangepicker_ranges_recent_week];
			picker.setStartDate(range[0]);
			picker.setEndDate(range[1]);

			$("#jobGroup").val( jobGroup );
			$("#jobId").val( jobId ).trigger("change");		// select2: change 	; $("#jobId").val( jobId );
			$('#logStatus').val(fromReport ? '2' : '-1');
		}
		resetFilter();

		// 切换执行器组时由服务端重新生成任务选项，仅继承日期和状态。
		var navigation = new URLSearchParams(window.location.search);
		if (!fromReport && navigation.has('jobGroup') && !navigation.has('jobId')) {
			var filterRange = (navigation.get('filterTime') || '').split(' - ');
			if (filterRange.length === 2) {
				var start = moment(filterRange[0], 'YYYY-MM-DD HH:mm:ss', true);
				var end = moment(filterRange[1], 'YYYY-MM-DD HH:mm:ss', true);
				if (start.isValid() && end.isValid() && !start.isAfter(end)) {
					var picker = filterTime.data('daterangepicker');
					picker.setStartDate(start);
					picker.setEndDate(end);
				}
			}
			var status = navigation.get('logStatus');
			if (['-1', '1', '2', '3'].indexOf(status) !== -1) {
				$('#logStatus').val(status);
			}
		}
		$('#jobGroup').on('change', function () {
			window.location.href = base_url + '/joblog?' + $.param({
				jobGroup: $(this).val(),
				logStatus: $('#logStatus').val(),
				filterTime: filterTime.val()
			});
		});

		// ---------------------- page ----------------------

		/**
		 * init table
		 */
		$.adminTable.initTable({
			table: '#data_list',
			url: base_url + "/joblog/pageList",
			queryParams: function (params) {
				var obj = {};
				obj.jobGroup = $('#jobGroup').val();
				obj.jobId = $('#jobId').val();
				obj.logStatus = $('#logStatus').val();
				obj.filterTime = $('#filterTime').val();
				obj.offset = params.offset;
				obj.pagesize = params.limit;
				return obj;
			},
			resetHandler : function() {
				// reset filter
				resetFilter();
			},
			columns:[
				{
					checkbox: true,
					field: 'state',
					width: '5',
					widthUnit: '%',
					align: 'center',
					valign: 'middle'
				},
                {
                    title: I18n.joblog_name + 'ID',
                    field: 'id',
                    width: '10',
                    widthUnit: '%',
                    align: 'left'
                },
                {
					title: I18n.jobinfo_job,
					field: 'jobId',
					width: '25',
					widthUnit: '%',
					align: 'left',
					formatter: function(value, row, index) {
                        // build
                        let jobShow = '【'+ row.jobId +'】';
                        let name = $("#jobId").find("option[value='"+ row.jobId +"']").text();
                        if (name) {
                            jobShow += name;
                        }
                        if (jobShow.length > 15) {
                            jobShow = jobShow.substr(0, 15) + '...';
                        }
						// show
						return $('<span>').text(jobShow).html();
					}
				},{
					title: I18n.joblog_field_triggerTime,
					field: 'triggerTime',
					width: '20',
					widthUnit: '%',
					formatter: function(value) {
						return value || '';
					}
				},{
					title: I18n.joblog_field_triggerCode,
					field: 'triggerCode',
					width: '5',
					widthUnit: '%',
					formatter: function(value, row, index) {
						var html = value;
						if (value == 200) {			// 200, success
							html = '<span style="color: green">'+ I18n.system_success +'</span>';
						} else if (value > 0) {		// >0 or 500, fail
							html = '<span style="color: red">'+ I18n.system_fail +'</span>';
						} else if (value == 0) {		// 0, original pass
							html = '';
						}
						return html;
					}
				},{
					title: I18n.joblog_field_triggerMsg,
					field: 'triggerMsg',
					width: '5',
					widthUnit: '%',
					formatter: formatRemarkAction,
					events: { 'click .logTips': function(event, value) { showRemark(I18n.joblog_field_triggerMsg, value, event.currentTarget); } }
				},{
					title: I18n.joblog_field_handleTime,
					field: 'handleTime',
					width: '20',
					widthUnit: '%',
					formatter: function(value) {
						return value || '';
					}
				},{
					title: I18n.joblog_field_handleCode,
					field: 'handleCode',
					width: '5',
					widthUnit: '%',
					formatter: function(value, row, index) {
						var html = value;
						if (value == 200) {			// 200, success
							html = '<span style="color: green">'+ I18n.joblog_handleCode_200 +'</span>';
						} else if (value == 502) {	// 502, timeout
							html = '<span style="color: red">'+ I18n.joblog_handleCode_502 +'</span>';
						} else if (value > 0) {		// >0 or 500, fail
							html = '<span style="color: red">'+ I18n.joblog_handleCode_500 +'</span>';
						} else if (value == 0) {		// 0, original pass
							html = '';
						}
						return html;
					}
				},{
					title: I18n.joblog_field_handleMsg,
					field: 'handleMsg',
					width: '5',
					widthUnit: '%',
					formatter: formatRemarkAction,
					events: { 'click .logTips': function(event, value) { showRemark(I18n.joblog_field_handleMsg, value, event.currentTarget); } }
				}
			]
		});

		/**
		 * logDetail
		 */
		$("#data_operation").on('click', '.logDetail',function() {
			// get select rows
			var rows = $.adminTable.table.bootstrapTable('getSelections');

			// find select row
			if (rows.length !== 1) {
				layer.msg(I18n.system_please_choose + I18n.system_one + I18n.system_data);
				return;
			}
			var row = rows[0];

			window.open(base_url + '/joblog/logDetailPage?id=' + row.id);
		});

		/**
		 * log Kill
		 */
		var killInProgress = false;
		$('#data_operation').on('click', '.logKill', function(){
			if (killInProgress) {
				return;
			}
			// get select rows
			var rows = $.adminTable.table.bootstrapTable('getSelections');

			// find select row
			if (rows.length !== 1) {
				layer.msg(I18n.system_please_choose + I18n.system_one + I18n.system_data);
				return;
			}
			var row = rows[0];
			var submitted = false;
			killInProgress = true;

			// do kill
			layer.confirm( (I18n.system_ok + I18n.joblog_kill_log + '?'), {
				icon: 3,
				title: I18n.system_tips ,
				btn: [ I18n.system_ok, I18n.system_cancel ],
				end: function () {
					if (!submitted) {
						killInProgress = false;
					}
				}
			}, function(index){
				if (submitted) {
					return;
				}
				submitted = true;
				layer.close(index);

				$.ajax({
					type : 'POST',
					url : base_url + '/joblog/logKill',
					data : {
						"id": row.id
					},
					dataType : "json",
					success: function(data) {
						var success = !!data && Number(data.code) === 200;
						showLogActionResult(success, success ? I18n.system_opt_suc : (data && data.msg) || I18n.system_opt_fail);
					},
					error: function () {
						showLogActionResult(false, I18n.joblog_kill_unconfirmed);
					},
					complete: function () {
						killInProgress = false;
					},
				});
			});

		});

		function showLogActionResult(success, message) {
			layer.open({
				title: I18n.system_tips,
				btn: [ I18n.system_ok ],
				content: $('<div>').text(message).prop('outerHTML'),
				icon: success ? 1 : 2,
				end: function () {
					$('#data_filter .searchBtn').click();
				}
			});
		}

		/**
		 * clear Log
		 */
		$('#data_operation').on('click', '.clearLog', function(){

			var jobGroup = $('#jobGroup').val();
			var jobId = $('#jobId').val();

			var jobGroupText = $("#jobGroup").find("option:selected").text();
			var jobIdText = $("#jobId").find("option:selected").text();

			$('#clearLogModal input[name=jobGroup]').val(jobGroup);
			$('#clearLogModal input[name=jobId]').val(jobId);

			$('#clearLogModal .jobGroupText').val(jobGroupText);
			$('#clearLogModal .jobIdText').val(jobIdText);

			$('#clearLogModal').modal('show');

		});
		$("#clearLogModal .ok").on('click', function(){
			var button = $(this);
			button.prop('disabled', true);
			$.post(base_url + "/joblog/clearLog", $("#clearLogModal .form").serialize())
				.done(function (data) {
					var success = !!data && Number(data.code) === 200;
					$('#clearLogModal').modal('hide');
					showLogActionResult(success, success
						? I18n.joblog_clean_log + I18n.system_success
						: (data && data.msg) || I18n.joblog_clean_incomplete);
				})
				.fail(function () {
					$('#clearLogModal').modal('hide');
					showLogActionResult(false, I18n.joblog_clean_incomplete);
				})
				.always(function () {
					button.prop('disabled', false);
				});
		});
		$("#clearLogModal").on('hide.bs.modal', function () {
			$("#clearLogModal .form")[0].reset();
		});

		var remarkModal = $('#logRemarkModal');
		var remarkText = document.getElementById('logRemarkText');
		var remarkStatus = $('#logRemarkStatus');
		var remarkVersion = 0;
		var remarkTrigger;

		function formatRemarkAction(value) {
			return value
				? '<button type="button" class="btn btn-link btn-xs logTips" aria-haspopup="dialog">' + I18n.system_show + '</button>'
				: I18n.system_empty;
		}

		function showRemark(title, value, trigger) {
			// 将历史日志里的格式标签转成纯文本，备注内容直接放入只读文本框。
			var text = value.replace(/<br\s*\/?>/gi, '\n').replace(/<\/?(?:span|b)(?:\s[^<>]*)?>/gi, '');
			var decoder = document.createElement('textarea');
			decoder.innerHTML = text.replace(/</g, '&lt;');
			remarkVersion++;
			remarkTrigger = trigger;
			$('#logRemarkTitle').text(title);
			remarkText.value = decoder.value;
			remarkStatus.empty();
			// 页签容器由 admin.dialog.js 提供遮罩，独立页面使用 Bootstrap 自带遮罩。
			remarkModal.modal({backdrop: window.self === window.top, show: true});
		}

		remarkModal.on('shown.bs.modal', function () {
			remarkText.scrollTop = 0;
		}).on('hidden.bs.modal', function () {
			remarkVersion++;
			remarkText.value = '';
			remarkStatus.empty();
			if (remarkTrigger && document.contains(remarkTrigger)) {
				remarkTrigger.focus();
			}
			remarkTrigger = null;
		});

		$('#copyLogRemark').on('click', async function () {
			var version = remarkVersion;
			var copyButton = this;
			try {
				await navigator.clipboard.writeText(remarkText.value);
			} catch (_) {
				if (version !== remarkVersion) {
					return;
				}
				var scrollTop = remarkText.scrollTop;
				remarkText.focus();
				remarkText.select();
				var copied = false;
				try {
					copied = document.execCommand('copy');
				} catch (_) {
					// 保留选区，便于手动复制。
				}
				if (!copied) {
					remarkStatus.text(I18n.joblog_copy_manual || I18n.system_opt_fail);
					return;
				}
				remarkText.setSelectionRange(0, 0);
				remarkText.scrollTop = scrollTop;
				copyButton.focus();
			}
			if (version === remarkVersion) {
				remarkStatus.text(I18n.joblog_copy_success || (I18n.system_opt_copy + ' ' + I18n.system_success));
			}
		});

	});
</script>
<!-- 3-script end -->

</body>
</html>
