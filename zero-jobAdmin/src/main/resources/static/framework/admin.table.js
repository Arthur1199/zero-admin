/*!
* Admin Table for XXL-BOOT
* ================
*
* 1、data_filter：
*      searchBtn：       搜索
*      resetBtn：        重置
* 2、data_operation：
*      action：
*          - add：       新增
*          - update：    更新
*          - delete：    删除
*      style：
*          - selectOnlyOne： 单选
*          - selectAny：     多选
* 3、data_list
*      mainDataTable：   表格
*
* @author       xuxueli
* @repository   https://github.com/xuxueli/xxl-boot
*/
(function($) {

    $.extend({
        adminTable: {
            table :null,
            options: {},
            selectIds: function () {
                // get select rows
                let rows = this.table.bootstrapTable('getSelections');
                // find select ids
                return (rows && rows.length > 0) ? rows.map(row => row.id) : [];
            },
            selectRows: function () {
                // get select rows
                return this.table.bootstrapTable('getSelections');
            },
            initTable: function(options) {
                // parse param
                this.table = $(options.table);
                this.options = options;

                // init filter
                initSearch(this.table, options, true);
                initReset(options);

                // init table
                initAdminTable(this.table, options);
            },
            initTreeTable: function(options) {
                // parse param
                this.table = $(options.table);

                // init filter
                initSearch(this.table, options);
                initReset(options);

                // init tree table
                initAdminTreeTable(this.table, options);
            },
            initDelete: function(options) {
                initDeleteFun(this.table, options);
            },
            initAdd: function(options) {
                return initAddFun(options);
            },
            initUpdate: function(options) {
                initUpdateFun(this.table, options);
            }
        }
    });

    /**
     * init search
     */
    function initSearch(table, options, paginated){
        // search
        $('#data_filter .searchBtn').on('click', function(){

            // searchHandler
            let searchHandler = options.searchHandler;
            if (typeof searchHandler === 'function') {
                searchHandler();
                return;
            }

            // do search
            if (paginated) {
                $(table).bootstrapTable('refresh', { pageNumber: 1 });
            } else {
                $(table).bootstrapTable('refresh');
            }

        });
    }

    /**
     * init reset
     */
    function initReset(options) {

        // reset
        $('#data_filter .resetBtn').on('click', function(){

            // reset
            let resetHandler = options.resetHandler;
            if (typeof resetHandler === 'function') {
                // resetHandler
                resetHandler();
            } else {
                // do reset

                // input
                $('#data_filter input[type="text"]').val('');
                // select
                $('#data_filter select').each(function() {
                    $(this).prop('selectedIndex', 0);
                });
                // checkbox
                $('#data_filter input[type="checkbox"]').prop('checked', false);
                // radio
                $('#data_filter input[type="radio"]').prop('checked', false);
            }

            // do search
            $('#data_filter .searchBtn').click();
        });
    }

    /**
     * init admin table
     *
     * @param table
     * @param url
     * @param queryParams
     * @param columns
     */
    function initAdminTable(table, options) {

        // parse param
        let url = options.url;
        let queryParams = options.queryParams;
        let columns = options.columns;
        const loadError = $('<div>', { class: 'alert alert-warning', role: 'status' })
            .hide().insertBefore(table);

        // init table
        $(table).bootstrapTable({
            url: url,
            method: "post",
            contentType: "application/x-www-form-urlencoded",
            queryParamsType: "limit",
            queryParams: queryParams,       // bootstrapTable -> queryParams
            sidePagination: "server",		// server side page
            responseHandler: function (result) {
                loadError.hide().empty();
                // custome
                if (options.responseHandler) {
                    return options.responseHandler(result);
                }

                // valid
                if (!result || result.code !== 200 || !result.data || !Array.isArray(result.data.data)) {
                    loadError.text(result && result.code !== 200 && result.msg || I18n.system_table_load_failed).show();
                    return {
                        total: 0,
                        rows: []
                    }
                }

                // default
                return {
                    "total": result.data.total,
                    "rows": result.data.data
                };
            },
            onLoadError: function () {
                loadError.text(I18n.system_table_load_failed).show();
            },
            columns: columns,
            clickToSelect: true, 			// 是否启用点击选中行
            multipleSelectRow: true,        // 启动多选行：点击 选择单行，Shift+点击 选择连续行, Commond+点击 非连续选择多行
            sortable: false, 				// 是否启用排序
            pagination: true, 				// 是否显示分页
            pageNumber: 1, 					// 默认第一页
            pageList: [10, 25, 50, 100] , 	// 可供选择的每页的行数（*）
            smartDisplay: false,			// 当总记录数小于分页数，是否显示可选项
            paginationParts:  ['pageInfoShort', 'pageSize', 'pageList'],
            paginationPreText: '<<',		// 跳转页面的 上一页按钮
            paginationNextText: '>>',		// 跳转页面的 下一页按钮
            paginationLoop: false,          // 是否循环翻页
            showRefresh: true,				// 显示刷新按钮
            showColumns: true,				// 显示/隐藏列
            minimumCountColumns: 2,			// 最少允许的列数
            // onLoadSuccess: function(data) {}
            onAll: function(name, args) {
                // filter
                if (!(['check.bs.table', "uncheck.bs.table", "check-all.bs.table", "uncheck-all.bs.table", 'post-body.bs.table'].indexOf(name) > -1)) {
                    return false;
                }
                var rows = $(table).bootstrapTable('getSelections');
                var selectLen = rows.length;

                if (selectLen > 0) {
                    $("#data_operation .selectAny").removeClass('disabled');
                } else {
                    $("#data_operation .selectAny").addClass('disabled');
                }
                if (selectLen === 1) {
                    $("#data_operation .selectOnlyOne").removeClass('disabled');
                } else {
                    $("#data_operation .selectOnlyOne").addClass('disabled');
                }
            }
        });

        // toolbar 样式调整；
        var toolbarElement = document.querySelector('.fixed-table-toolbar');
        if (toolbarElement) {
            toolbarElement.classList.remove('fixed-table-toolbar');
        }
    }

    function initAdminTreeTable(table, options) {

        // parse param
        let url = options.url;
        let queryParams = options.queryParams;
        let columns = options.columns;

        // table
        var mainDataTable = $("#data_list").bootstrapTable({
            url: url,
            method: "post",
            contentType: "application/x-www-form-urlencoded",
            queryParams: queryParams,
            responseHandler: function(result) {
                if (result.code !== 200) {
                    layer.open({
                        icon: '2',
                        content: result.msg
                    });
                    return ;
                }
                return result.data;
            },
            treeEnable:true,
            idField: 'id',					// 树形id
            parentIdField: 'parentId',		// 父级字段
            treeShowField: 'name',			// 树形字段
            onPostBody: function(data) {
                $("#data_list").treegrid({
                    treeColumn: 1,												// 选择第几列作为树形字段
                    initialState: 'expanded',									// 默认展开；expanded、collapsed
                    expanderExpandedClass: 'fa fa-fw  fa-minus-square-o',		// 树形展开图标
                    expanderCollapsedClass: 'fa fa-fw  fa-plus-square-o',		// 树形折叠图标
                    onChange () {
                        $("#data_list").bootstrapTable('resetView')				// 树形表格重绘
                    }
                })
            },
            columns:columns,
            clickToSelect: true, 			// 是否启用点击选中行
            multipleSelectRow: true,        // 启动多选行：点击 选择单行，Shift+点击 选择连续行, Commond+点击 非连续选择多行
            sortable: false, 				// 是否启用排序
            showRefresh: true,				// 显示刷新按钮
            showColumns: true,				// 显示/隐藏列
            minimumCountColumns: 2,			// 最少允许的列数
            onAll: function(name, args) {
                // filter
                if (!(['check.bs.table', "uncheck.bs.table", "check-all.bs.table", "uncheck-all.bs.table"].indexOf(name) > -1)) {
                    return false;
                }
                var rows = mainDataTable.bootstrapTable('getSelections');
                var selectLen = rows.length;

                if (selectLen > 0) {
                    $("#data_operation .selectAny").removeClass('disabled');
                } else {
                    $("#data_operation .selectAny").addClass('disabled');
                }
                if (selectLen === 1) {
                    $("#data_operation .selectOnlyOne").removeClass('disabled');
                } else {
                    $("#data_operation .selectOnlyOne").addClass('disabled');
                }
            }
        });

        // toolbar 样式调整；
        var toolbarElement = document.querySelector('.fixed-table-toolbar');
        if (toolbarElement) {
            toolbarElement.classList.remove('fixed-table-toolbar');
        }
    }

    /**
     * init delete
     */
    function initDeleteFun(table, options) {
        // parse param
        let url = options.url;

        // delete
        $("#data_operation").on('click', '.delete',function() {
            // get select rows
            var rows = $(table).bootstrapTable('getSelections');

            // find select ids
            const selectIds = (rows && rows.length > 0) ? rows.map(row => row.id) : [];
            if (selectIds.length <= 0) {
                layer.msg(I18n.system_please_choose + I18n.system_data);
                return;
            }

            // do delete
            layer.confirm( I18n.system_ok + I18n.system_opt_del + '?', {
                icon: 3,
                title: I18n.system_tips ,
                btn: [ I18n.system_ok, I18n.system_cancel ]
            }, function(index){
                layer.close(index);

                $.ajax({
                    type : 'POST',
                    url : url,
                    data : {
                        "ids" : selectIds
                    },
                    dataType : "json",
                    success : function(data){
                        if (data.code === 200) {
                            layer.msg( I18n.system_opt_del + I18n.system_success );
                            // refresh table
                            $('#data_filter .searchBtn').click();
                        } else {
                            layer.msg( data.msg || I18n.system_opt_del + I18n.system_fail );
                        }
                    },
                    error: function(xhr, status, error) {
                        // Handle error
                        console.log("Error: " + error);
                        layer.open({
                            icon: '2',
                            content: (I18n.system_opt_del + I18n.system_fail)
                        });
                    }
                });
            });
        });
    }

    /**
     * 初始化新增表单，并提供给任务复制入口复用。
     */
    function initAddFun(options) {
        const open = initSaveModal('#addModal', options, I18n.system_opt_add);
        function openAdd(fillCopy) {
            open(function () {
                if (typeof options.writeFormData === 'function') {
                    options.writeFormData();
                }
                if (typeof fillCopy === 'function') {
                    fillCopy();
                }
            });
        }
        $("#data_operation .add").click(function () {
            openAdd();
        });
        return openAdd;
    }

    /**
     * 编辑使用打开弹窗时选中的记录。
     */
    function initUpdateFun(table, options) {
        const open = initSaveModal('#updateModal', options, I18n.system_opt_edit);
        $("#data_operation .update").click(function () {
            var rows = $(table).bootstrapTable('getSelections');
            if (rows.length !== 1) {
                layer.msg(I18n.system_please_choose + I18n.system_one + I18n.system_data);
                return;
            }
            open(function () {
                options.writeFormData(rows[0]);
            });
        });
    }

    /**
     * 保存期间保留表单快照，失败后恢复操作，成功后关闭并刷新列表。
     */
    function initSaveModal(selector, options, action) {
        const modal = $(selector);
        const form = modal.find('.form');
        const saveTimeoutMs = 30000;
        let saving = false;

        modal.on('hide.bs.modal', function () {
            if (saving) {
                return false;
            }
        });

        const validator = form.validate({
            errorElement : 'span',
            errorClass : 'help-block',
            focusInvalid : true,
            rules : options.rules,
            messages : options.messages,
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
            submitHandler : function() {
                if (saving) {
                    return;
                }
                const data = options.readFormData();
                const controls = modal.find(':input:enabled');
                let saved = false;
                saving = true;
                controls.prop('disabled', true);
                $.ajax({
                    url: options.url,
                    type: 'POST',
                    data: data,
                    dataType: 'json',
                    timeout: saveTimeoutMs,
                    success: function (response) {
                        saved = !!response && response.code === 200;
                        if (!saved) {
                            layer.msg($('<span>').text((response && response.msg) || action + I18n.system_fail).html());
                        }
                    },
                    error: function () {
                        layer.msg(I18n.system_save_unconfirmed);
                    },
                    complete: function () {
                        saving = false;
                        controls.prop('disabled', false);
                        if (saved) {
                            modal.modal('hide');
                            layer.msg(action + I18n.system_success);
                            $('#data_filter .searchBtn').click();
                        }
                    }
                });
            }
        });

        return function (fillForm) {
            if (saving || modal.hasClass('in')) {
                return;
            }
            form[0].reset();
            form.find('.form-group').removeClass('has-error');
            validator.resetForm();
            fillForm();
            modal.modal({backdrop: false, keyboard: false}).modal('show');
        };
    }

})(jQuery);
