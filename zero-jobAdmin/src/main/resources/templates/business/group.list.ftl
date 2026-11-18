<!DOCTYPE html>
<html>
<head>
	<#-- import macro -->
	<#import "../framework/common/common.macro.ftl" as netCommon>

	<!-- 1-style start -->
	<@netCommon.commonStyle />
	<link rel="stylesheet" href="${request.contextPath}/static/plugins/bootstrap-table/bootstrap-table.min.css">
	<link rel="stylesheet" href="${request.contextPath}/static/adminlte/plugins/iCheck/square/blue.css">
	<style>
		#showRegistryListModal > .modal-dialog {
			width: 760px;
		}
		#registryAddresses {
			min-height: 120px;
			height: 30vh;
			max-height: 280px;
			resize: vertical;
			font: 13px/1.65 Consolas, "Liberation Mono", monospace;
		}
		#registryStatus {
			margin: 8px 0 0;
		}
	</style>
	<!-- 1-style end -->

</head>
<body class="hold-transition zero-job-tab-page">
<div class="wrapper">
	<section class="content">

		<!-- 2-content start -->

		<#-- 查询区域 -->
		<div class="box" style="margin-bottom:9px;">
			<div class="box-body">
				<div class="row" id="data_filter" >

					<div class="col-xs-3">
						<div class="input-group">
							<span class="input-group-addon">AppName</span>
							<input type="text" class="form-control" id="appname" placeholder="${I18n.system_please_input}AppName" >
						</div>
					</div>
					<div class="col-xs-3">
						<div class="input-group">
							<span class="input-group-addon">${I18n.jobgroup_field_title}</span>
							<input type="text" class="form-control" id="name" placeholder="${I18n.jobgroup_field_title}" >
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
						<button class="btn btn-sm btn-info add" type="button"><i class="fa fa-plus" ></i>${I18n.system_opt_add}</button>
						<button class="btn btn-sm btn-warning selectOnlyOne update" type="button"><i class="fa fa-edit"></i>${I18n.system_opt_edit}</button>
						<button class="btn btn-sm btn-danger selectOnlyOne delete" type="button"><i class="fa fa-remove "></i>${I18n.system_opt_del}</button>
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

		<!-- 注册列表查看.模态框 -->
		<div class="modal fade" id="showRegistryListModal" tabindex="-1" role="dialog" aria-labelledby="registryListTitle">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">
						<h4 class="modal-title" id="registryListTitle">${I18n.jobinfo_opt_registryinfo}</h4>
					</div>
					<div class="modal-body">
						<textarea id="registryAddresses" class="form-control" aria-labelledby="registryListTitle"
								  style="display: none" readonly spellcheck="false"></textarea>
						<p id="registryStatus" class="text-muted" role="status"></p>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-primary" id="copyRegistryAddresses" disabled>${I18n.system_opt_copy}</button>
						<button type="button" class="btn btn-default" data-dismiss="modal">${I18n.system_close}</button>
					</div>
				</div>
			</div>
		</div>

		<!-- 新增.模态框 -->
		<div class="modal fade" id="addModal" tabindex="-1" role="dialog"  aria-hidden="true">
			<div class="modal-dialog ">
				<div class="modal-content">
					<div class="modal-header">
						<h4 class="modal-title" >${I18n.system_opt_add} ${I18n.jobgroup}</h4>
					</div>
					<div class="modal-body">
						<form class="form-horizontal form" role="form" >
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">AppName<font color="red">*</font></label>
								<div class="col-sm-9"><input type="text" class="form-control" name="appname" placeholder="${I18n.system_please_input}AppName" maxlength="64" ></div>
							</div>
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">${I18n.jobgroup_field_title}<font color="red">*</font></label>
								<div class="col-sm-9"><input type="text" class="form-control" name="name" placeholder="${I18n.system_please_input}${I18n.jobgroup_field_title}" maxlength="64" ></div>
							</div>
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">${I18n.jobgroup_field_addressType}<font color="red">*</font></label>
								<div class="col-sm-9">
									<input type="radio" name="addressType" value="0" checked />${I18n.jobgroup_field_addressType_0}
									&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" name="addressType" value="1" />${I18n.jobgroup_field_addressType_1}
								</div>
							</div>
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">${I18n.jobgroup_field_registryList}<font color="red">*</font></label>
								<div class="col-sm-9">
									<textarea class="textarea" name="addressList" maxlength="20000" placeholder="${I18n.jobgroup_field_registryList_placeholder}" readonly="readonly" style="background-color:#eee; width: 100%; height: 100px; font-size: 14px; line-height: 15px; border: 1px solid #dddddd; padding: 5px;"></textarea>
								</div>
							</div>
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">AccessToken<font color="red">*</font></label>
								<div class="col-sm-9"><input type="text" class="form-control" name="accessToken" placeholder="${I18n.system_please_input}AccessToken" maxlength="50" ></div>
							</div>
							<hr>
							<div class="form-group">
								<div class="col-sm-offset-4 col-sm-4">
									<button type="submit" class="btn btn-primary"  >${I18n.system_save}</button>
									<button type="button" class="btn btn-default" data-dismiss="modal">${I18n.system_cancel}</button>
								</div>
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>

		<!-- 更新.模态框 -->
		<div class="modal fade" id="updateModal" tabindex="-1" role="dialog"  aria-hidden="true">
			<div class="modal-dialog ">
				<div class="modal-content">
					<div class="modal-header">
						<h4 class="modal-title" >${I18n.system_opt_edit} ${I18n.jobgroup}</h4>
					</div>
					<div class="modal-body">
						<form class="form-horizontal form" role="form" >
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">AppName<font color="red">*</font></label>
								<div class="col-sm-9"><input type="text" class="form-control" name="appname" placeholder="${I18n.system_please_input}AppName" maxlength="64" readonly ></div>
							</div>
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">${I18n.jobgroup_field_title}<font color="red">*</font></label>
								<div class="col-sm-9"><input type="text" class="form-control" name="name" placeholder="${I18n.system_please_input}${I18n.jobgroup_field_title}" maxlength="64" ></div>
							</div>
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">${I18n.jobgroup_field_addressType}<font color="red">*</font></label>
								<div class="col-sm-9">
									<input type="radio" name="addressType" value="0" />${I18n.jobgroup_field_addressType_0}
									&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" name="addressType" value="1" />${I18n.jobgroup_field_addressType_1}
								</div>
							</div>
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">${I18n.jobgroup_field_registryList}<font color="red">*</font></label>
								<div class="col-sm-9">
									<textarea class="textarea" name="addressList" maxlength="20000" placeholder="${I18n.jobgroup_field_registryList_placeholder}" readonly="readonly" style="background-color:#eee; width: 100%; height: 100px; font-size: 14px; line-height: 15px; border: 1px solid #dddddd; padding: 5px;"></textarea>
								</div>
							</div>
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">AccessToken<font color="red">*</font></label>
								<div class="col-sm-9"><input type="text" class="form-control" name="accessToken" placeholder="${I18n.system_please_input}AccessToken" maxlength="50" ></div>
							</div>
							<hr>
							<div class="form-group">
								<div class="col-sm-offset-4 col-sm-4">
									<button type="submit" class="btn btn-primary"  >${I18n.system_save}</button>
									<button type="button" class="btn btn-default" data-dismiss="modal">${I18n.system_cancel}</button>
									<input type="hidden" name="id" >
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

<!-- 3-script start -->
<@netCommon.commonScript />
<script src="${request.contextPath}/static/plugins/bootstrap-table/bootstrap-table.min.js"></script>
<script src="${request.contextPath}/static/plugins/bootstrap-table/locale/<#if I18n.admin_i18n?? && I18n.admin_i18n == 'en'>bootstrap-table-en-US.min.js<#else>bootstrap-table-zh-CN.min.js</#if>"></script>
<script src="${request.contextPath}/static/adminlte/plugins/iCheck/icheck.min.js"></script>
<#-- admin table -->
<script src="${request.contextPath}/static/framework/admin.table.js"></script>
<script>
	$(function() {

		/**
		 * init table
		 */
		$.adminTable.initTable({
			table: '#data_list',
			url: base_url + "/jobgroup/pageList",
			queryParams: function (params) {
				var obj = {};
				obj.appname = $('#appname').val();
				obj.name = $('#name').val();
				obj.offset = params.offset;
				obj.pagesize = params.limit;
				return obj;
			},
			columns:[
				{
					checkbox: true,
					field: 'state',
					width: '5',
					widthUnit: '%',
					align: 'center',
					valign: 'middle'
				},{
					title: 'AppName',
					field: 'appname',
					escape: true,
					width: '25',
					widthUnit: '%',
					align: 'left'
				},{
					title: I18n.jobgroup_field_title,
					field: 'name',
					escape: true,
					width: '25',
					widthUnit: '%'
				},{
					title: 'AccessToken',
					field: 'accessToken',
					escape: true,
					width: '20',
					widthUnit: '%',
					align: 'left'
				},{
					title: I18n.jobgroup_field_addressType,
					field: 'addressType',
					width: '10',
					widthUnit: '%',
					formatter: function(value, row, index) {
						if (row.addressType == 0) {
							return I18n.jobgroup_field_addressType_0;
						} else {
							return I18n.jobgroup_field_addressType_1;
						}
					}
				},{
					title: 'OnLine' + I18n.jobgroup_field_registryList,
					field: 'registryList',
					width: '15',
					widthUnit: '%',
					formatter: function(value, row) {
						if (!Array.isArray(value) || !value.length) {
							return I18n.system_empty;
						}
						return $('<button>', {
							type: 'button',
							class: 'btn btn-link btn-xs show_registryList',
							'data-group-id': row.id,
							'aria-haspopup': 'dialog'
						}).text(I18n.system_show + ' (' + value.length + ')').prop('outerHTML');
					}
				}
			]
		});

		var registryModal = $('#showRegistryListModal');
		var registryAddresses = $('#registryAddresses');
		var registryStatus = $('#registryStatus');
		var copyRegistry = $('#copyRegistryAddresses');
		var registryRequest;
		var registryTrigger;
		var registryVersion = 0;
		$("#data_list").on('click', '.show_registryList', function () {
			var version = ++registryVersion;
			if (registryRequest) {
				registryRequest.abort();
			}
			registryTrigger = this;
			registryAddresses.val('').hide();
			copyRegistry.prop('disabled', true);
			registryStatus.text(I18n.jobgroup_registry_loading);
			registryModal.modal({backdrop: false, show: true});
			registryRequest = $.ajax({
				url: base_url + '/jobgroup/loadById',
				type: 'POST',
				data: {id: this.dataset.groupId},
				dataType: 'json'
			}).done(function (result) {
				if (version !== registryVersion) {
					return;
				}
				var addresses = result && result.code === 200 && result.data && result.data.registryList;
				if (!Array.isArray(addresses)) {
					registryStatus.text(I18n.system_opt_fail);
				} else if (!addresses.length) {
					registryStatus.text(I18n.system_empty);
				} else {
					registryAddresses.val(addresses.join('\n')).show();
					copyRegistry.prop('disabled', false);
					registryStatus.empty();
				}
			}).fail(function () {
				if (version === registryVersion) {
					registryStatus.text(I18n.system_opt_fail);
				}
			});
		});
		registryModal.on('hide.bs.modal', function () {
			registryVersion++;
			if (registryRequest) {
				registryRequest.abort();
				registryRequest = null;
			}
		}).on('hidden.bs.modal', function () {
			if (registryTrigger && document.contains(registryTrigger)) {
				registryTrigger.focus();
			}
			registryTrigger = null;
		});
		copyRegistry.on('click', async function () {
			var version = registryVersion;
			try {
				await navigator.clipboard.writeText(registryAddresses.val());
				if (version === registryVersion) {
					registryStatus.text(I18n.system_opt_copy + ' ' + I18n.system_success);
				}
			} catch (_) {
				if (version === registryVersion) {
					var addressField = registryAddresses.get(0);
					addressField.focus();
					addressField.select();
					registryStatus.text(I18n.jobgroup_registry_copy_manual);
				}
			}
		});

		/**
		 * init delete
		 */
		$.adminTable.initDelete({
			url: base_url + "/jobgroup/delete"
		});

		/**
		 * init add
		 */
		// add validator method
		jQuery.validator.addMethod("appnameValid", function(value, element) {
			var valid = /^[a-z][a-z0-9-]*$/;
			return this.optional(element) || valid.test(value);
		}, I18n.jobgroup_field_appname_limit );
		// add validator method
		jQuery.validator.addMethod("accessTokenValid", function(value, element) {
			var valid = /^[a-z][a-z0-9_]*$/;
			return this.optional(element) || valid.test(value);
		}, I18n.jobgroup_field_accesstoken_limit );
		$.adminTable.initAdd( {
			url: base_url + "/jobgroup/insert",
			rules : {
				appname : {
					required : true,
					rangelength:[4,64],
					appnameValid : true
				},
				name : {
					required : true,
					rangelength:[4, 64]
				},
				accessToken : {
					required : true,
					rangelength:[4, 50],
					accessTokenValid: true
				}
			},
			messages : {
				appname : {
					required : I18n.system_please_input+"AppName",
					rangelength: I18n.system_length_limit + ' [4~64]'
				},
				name : {
					required : I18n.system_please_input + I18n.jobgroup_field_title ,
					rangelength: I18n.system_length_limit + ' [4~64]' ,
				},
				accessToken : {
					required : I18n.system_please_input,
					rangelength: I18n.system_length_limit + " [4-50]"
				}
			},
			writeFormData: function() {
				$("#addModal .form input[name='addressType']:checked").trigger('change');
			},
			readFormData: function() {
				// request
				return $("#addModal .form").serializeArray();
			}
		});

		// addressType change
		$("#addModal input[name=addressType], #updateModal input[name=addressType]").on('change', function () {
			var automatic = this.value === '0';
			var addresses = $(this).closest('form').find('textarea[name=addressList]');
			addresses.prop('readOnly', automatic).css('background-color', automatic ? '#eee' : '');
			if (automatic) {
				addresses.val('');
			}
		});

		/**
		 * init update
		 */
		$.adminTable.initUpdate( {
			url: base_url + "/jobgroup/update",
			writeFormData: function(row) {

				$("#updateModal .form input[name='id']").val( row.id );
				$("#updateModal .form input[name='appname']").val( row.appname );
				$("#updateModal .form input[name='name']").val( row.name );

				// 注册方式
				$("#updateModal .form input[name='addressType'][value='"+ (Number(row.addressType) === 0 ? '0' : '1') +"']")
					.prop('checked', true).trigger('change');
				// 机器地址
				$("#updateModal .form textarea[name='addressList']").val( row.addressList );
				// accessToken
				$("#updateModal .form input[name='accessToken']").val( row.accessToken );

			},
			rules : {
				name : {
					required : true,
					rangelength:[4, 64]
				},
				accessToken : {
					required : false,
					rangelength:[4, 50],
					accessTokenValid: true
				}
			},
			messages : {
				name : {
					required : I18n.system_please_input + I18n.jobgroup_field_title ,
					rangelength: I18n.system_length_limit + ' [4~64]'
				},
				accessToken : {
					required : I18n.system_please_input,
					rangelength: I18n.system_length_limit + " [4-50]"
				}
			},
			readFormData: function() {
				// request
				return $("#updateModal .form").serializeArray();
			}
		});

	});

</script>
<!-- 3-script end -->

</body>
</html>
