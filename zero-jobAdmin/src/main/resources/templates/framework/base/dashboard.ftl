<!DOCTYPE html>
<html>
<head>
    <#-- import macro -->
    <#import "../common/common.macro.ftl" as netCommon>

    <!-- 1-style start -->
    <@netCommon.commonStyle />
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/bootstrap-daterangepicker/daterangepicker.css">
    <link rel="stylesheet" href="${request.contextPath}/static/framework/date-range.css?v=${I18n.admin_version}">
    <link rel="stylesheet" href="${request.contextPath}/static/framework/dashboard.css?v=${I18n.admin_version}&amp;layout=4">
    <!-- 1-style end -->

</head>
<body class="hold-transition zero-job-tab-page">
<div class="wrapper">
    <section class="content">

        <#-- 2-biz start -->

        <!-- 任务信息 -->
        <div class="row">

            <#-- 任务信息 -->
            <div class="col-md-4 col-sm-6 col-xs-12">
                <div class="info-box bg-aqua">
                    <span class="info-box-icon"><i class="fa fa-flag-o"></i></span>

                    <div class="info-box-content">
                        <span class="info-box-text">${I18n.job_dashboard_job_num}</span>
                        <span class="info-box-number">${jobInfoCount}</span>

                        <div class="progress">
                            <div class="progress-bar" style="width: 100%"></div>
                        </div>
                        <span class="progress-description">${I18n.job_dashboard_job_num_tip}</span>
                    </div>
                </div>
            </div>

            <#-- 调度信息 -->
            <div class="col-md-4 col-sm-6 col-xs-12" >
                <div class="info-box bg-yellow">
                    <span class="info-box-icon"><i class="fa fa-calendar"></i></span>

                    <div class="info-box-content">
                        <span class="info-box-text">${I18n.job_dashboard_trigger_num}</span>
                        <span class="info-box-number">${jobLogCount}</span>

                        <div class="progress">
                            <div class="progress-bar" style="width: 100%" ></div>
                        </div>
                        <span class="progress-description">
                                ${I18n.job_dashboard_trigger_num_tip}
                            <#--<#if jobLogCount gt 0>
                                调度成功率：${(jobLogSuccessCount*100/jobLogCount)?string("0.00")}<small>%</small>
                            </#if>-->
                            </span>
                    </div>
                </div>
            </div>

            <#-- 执行器 -->
            <div class="col-md-4 col-sm-6 col-xs-12">
                <div class="info-box bg-green">
                    <span class="info-box-icon"><i class="fa ion-ios-settings-strong"></i></span>

                    <div class="info-box-content">
                        <span class="info-box-text">${I18n.job_dashboard_jobgroup_num}</span>
                        <span class="info-box-number">${executorCount}</span>

                        <div class="progress">
                            <div class="progress-bar" style="width: 100%"></div>
                        </div>
                        <span class="progress-description">${I18n.job_dashboard_jobgroup_num_tip}</span>
                    </div>
                </div>
            </div>

        </div>

        <#-- 调度报表：时间区间筛选，左侧折线图 + 右侧饼图 -->
        <div class="row">
            <div class="col-md-12">
                <div class="box">
                    <div class="box-header with-border">
                        <h3 class="box-title">${I18n.job_dashboard_report}</h3>
                        <#--<input type="text" class="form-control" id="filterTime" readonly >-->

                        <!-- tools box -->
                        <div class="pull-right box-tools">
                            <button type="button" class="btn btn-primary btn-sm daterange pull-right" data-toggle="tooltip" id="filterTime" >
                                <i class="fa fa-calendar"></i>
                            </button>
                            <#--<button type="button" class="btn btn-primary btn-sm pull-right" data-widget="collapse" data-toggle="tooltip" title="" style="margin-right: 5px;" data-original-title="Collapse">
                                <i class="fa fa-minus"></i>
                            </button>-->
                        </div>
                        <!-- /. tools -->

                    </div>
                    <div class="box-body">
                        <div class="row">
                            <#-- 左侧折线图 -->
                            <div class="col-md-8">
                                <div id="lineChart" style="height: 350px;"></div>
                            </div>
                            <#-- 右侧饼图 -->
                            <div class="col-md-4">
                                <div id="pieChart" style="height: 350px;"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div id="jobReport" data-today="${reportToday}" data-max-days="${reportMaxDays?c}" class="box">
            <div class="box-header with-border">
                <h2 class="box-title">${I18n.job_report_trend}</h2>
                <div class="job-report-filters">
                    <label for="reportGroup">${I18n.jobgroup}</label>
                    <select id="reportGroup" class="form-control input-sm">
                        <option value="0">${I18n.job_report_all_groups}</option>
                        <#list reportGroups as group>
                            <option value="${group.id?c}">${group.name?html}</option>
                        </#list>
                    </select>
                    <label for="reportFilterTime">${I18n.job_report_dates}</label>
                    <input id="reportFilterTime" class="form-control input-sm" type="text" readonly>
                    <button id="refreshReport" type="button" class="btn btn-primary btn-sm">${I18n.joblog_rolling_log_refresh}</button>
                </div>
            </div>
            <div class="box-body">
                <p class="text-muted">${I18n.job_report_scope}</p>
                <div id="reportStatus" role="status" aria-live="polite"></div>
                <div id="reportContent" hidden>
                    <p>${I18n.job_report_attempts} <strong id="reportAttempts"></strong>
                        · ${I18n.job_report_success_rate} <strong id="reportRate"></strong></p>
                    <div class="row">
                        <div class="col-md-8"><div id="reportLineChart" class="job-report-chart"></div></div>
                        <div class="col-md-4"><div id="reportPieChart" class="job-report-chart"></div></div>
                    </div>

                    <h3 class="job-report-heading">${I18n.job_report_top_failures}</h3>
                    <div class="table-responsive">
                        <table class="table table-bordered table-striped">
                            <thead><tr>
                                <th scope="col">${I18n.jobinfo_field_jobdesc}</th>
                                <th scope="col">${I18n.jobgroup}</th>
                                <th scope="col">${I18n.joblog_status_fail}</th>
                                <th scope="col">${I18n.job_report_attempts}</th>
                                <th scope="col">${I18n.job_report_failure_rate}</th>
                                <th scope="col">${I18n.job_report_last_failure}</th>
                                <th scope="col">${I18n.joblog_name}</th>
                            </tr></thead>
                            <tbody id="failedJobs"></tbody>
                        </table>
                    </div>

                    <h3 class="job-report-heading">${I18n.job_report_pending_list}</h3>
                    <div class="table-responsive">
                        <table class="table table-bordered table-striped">
                            <thead><tr>
                                <th scope="col">${I18n.job_report_log_id}</th>
                                <th scope="col">${I18n.jobinfo_field_jobdesc}</th>
                                <th scope="col">${I18n.jobgroup}</th>
                                <th scope="col">${I18n.job_report_executor_address}</th>
                                <th scope="col">${I18n.joblog_field_triggerTime}</th>
                                <th scope="col">${I18n.job_report_wait_minutes}</th>
                            </tr></thead>
                            <tbody id="pendingJobs"></tbody>
                        </table>
                    </div>
                    <div id="pendingStatus" class="text-muted" role="status"></div>
                    <div id="pendingPagination" class="job-report-pagination" hidden>
                        <button id="previousPending" type="button" class="btn btn-default btn-sm">${I18n.job_report_previous}</button>
                        <span id="pendingPage" aria-live="polite"></span>
                        <button id="nextPending" type="button" class="btn btn-default btn-sm">${I18n.job_report_next}</button>
                    </div>
                </div>
            </div>
        </div>

        <#-- 2-biz end -->

    </section>
</div>

<!-- 3-script start -->
<@netCommon.commonScript />
<!-- daterangepicker -->
<script src="${request.contextPath}/static/adminlte/bower_components/moment/moment.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/bootstrap-daterangepicker/daterangepicker.js"></script>
<!-- echarts -->
<script src="${request.contextPath}/static/plugins/echarts/echarts.common.min.js"></script>
<script id="historicalReportScript">
$(function () {
    var chartData = null;
    var chartFrame = null;
    var chartRequest = null;
    var requestVersion = 0;
    var chartElements = [document.getElementById('lineChart'), document.getElementById('pieChart')];

    // 页签iframe可能仍被隐藏；只在容器尺寸有效后绘图，避免ECharts记住零宽度。
    function renderCharts() {
        chartFrame = null;
        if (!chartData || chartElements.some(function (element) {
            return element.clientWidth <= 0 || element.clientHeight <= 0;
        })) {
            return;
        }
        lineChartInit(chartData);
        pieChartInit(chartData);
        chartElements.forEach(function (element) {
            echarts.getInstanceByDom(element).resize();
        });
    }

    function scheduleCharts() {
        if (chartFrame === null) {
            chartFrame = window.requestAnimationFrame(renderCharts);
        }
    }

    // iframe首次显示、页签切回及侧栏伸缩都会改变容器宽度，无需刷新报表数据。
    var chartObserver = window.ResizeObserver ? new ResizeObserver(scheduleCharts) : null;
    if (chartObserver) {
        chartElements.forEach(function (element) { chartObserver.observe(element); });
    }
    $(window).on('resize.historicalCharts load.historicalCharts', scheduleCharts);
    $(window).on('pageshow.historicalCharts', function () {
        if (chartObserver) {
            chartElements.forEach(function (element) { chartObserver.observe(element); });
        }
        scheduleCharts();
    });
    $(window).on('pagehide.historicalCharts', function () {
        if (chartObserver) {
            chartObserver.disconnect();
        }
        if (chartFrame !== null) {
            window.cancelAnimationFrame(chartFrame);
            chartFrame = null;
        }
        if (chartRequest) {
            chartRequest.abort();
        }
        chartElements.forEach(function (element) {
            var chart = echarts.getInstanceByDom(element);
            if (chart) {
                chart.dispose();
            }
        });
    });

    // filter Time
    var rangesConf = {};
    rangesConf[I18n.daterangepicker_ranges_today] = [moment().startOf('day'), moment().endOf('day')];
    rangesConf[I18n.daterangepicker_ranges_yesterday] = [moment().subtract(1, 'days').startOf('day'), moment().subtract(1, 'days').endOf('day')];
    rangesConf[I18n.daterangepicker_ranges_this_month] = [moment().startOf('month'), moment().endOf('month')];
    rangesConf[I18n.daterangepicker_ranges_last_month] = [moment().subtract(1, 'months').startOf('month'), moment().subtract(1, 'months').endOf('month')];
    rangesConf[I18n.daterangepicker_ranges_recent_week] = [moment().subtract(1, 'weeks').startOf('day'), moment().endOf('day')];
    rangesConf[I18n.daterangepicker_ranges_recent_month] = [moment().subtract(1, 'months').startOf('day'), moment().endOf('day')];

    $('#filterTime').daterangepicker({
        autoApply:false,
        singleDatePicker:false,
        showDropdowns:false,        // 是否显示年月选择条件
        timePicker: true, 			// 是否显示小时和分钟选择条件
        timePickerIncrement: 10, 	// 时间的增量，单位为分钟
        timePicker24Hour : true,
        opens : 'left', //日期选择框的弹出位置
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
        },
        startDate: rangesConf[I18n.daterangepicker_ranges_recent_week][0] ,
        endDate: rangesConf[I18n.daterangepicker_ranges_recent_week][1]
    }, function (start, end, label) {
        freshChartDate(start, end);
    });
    freshChartDate(rangesConf[I18n.daterangepicker_ranges_recent_week][0], rangesConf[I18n.daterangepicker_ranges_recent_week][1]);

    /**
     * fresh Chart Date
     *
     * @param startDate
     * @param endDate
     */
    function freshChartDate(startDate, endDate) {
        var currentVersion = ++requestVersion;
        if (chartRequest) {
            chartRequest.abort();
        }
        chartRequest = $.ajax({
            type: 'POST',
            url: base_url + '/chartInfo',
            data: {
                startDate: startDate.format('YYYY-MM-DD HH:mm:ss'),
                endDate: endDate.format('YYYY-MM-DD HH:mm:ss')
            },
            dataType: 'json',
            success: function (response) {
                if (currentVersion !== requestVersion) {
                    return;
                }
                if (response && response.code === 200 && response.data) {
                    chartData = response.data;
                    scheduleCharts();
                } else {
                    layer.msg(I18n.job_dashboard_report_loaddata_fail, {icon: 2});
                }
            },
            error: function (_, status) {
                if (status !== 'abort' && currentVersion === requestVersion) {
                    layer.msg(I18n.job_dashboard_report_loaddata_fail, {icon: 2});
                }
            },
            complete: function () {
                if (currentVersion === requestVersion) {
                    chartRequest = null;
                }
            }
        });
    }

    /**
     * line Chart Init
     */
    function lineChartInit(data) {
        var option = {
            title: {
                text: I18n.job_dashboard_date_report
            },
            tooltip : {
                trigger: 'axis',
                axisPointer: {
                    type: 'cross',
                    label: {
                        backgroundColor: '#6a7985'
                    }
                }
            },
            legend: {
                data:[I18n.joblog_status_suc, I18n.joblog_status_fail, I18n.joblog_status_running]
            },
            toolbox: {
                feature: {
                    /*saveAsImage: {}*/
                }
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis : [
                {
                    type : 'category',
                    boundaryGap : false,
                    data : data.triggerDayList
                }
            ],
            yAxis : [
                {
                    type : 'value'
                }
            ],
            series : [
                {
                    name:I18n.joblog_status_suc,
                    type:'line',
                    stack: 'Total',
                    areaStyle: {normal: {}},
                    data: data.triggerDayCountSucList
                },
                {
                    name:I18n.joblog_status_fail,
                    type:'line',
                    stack: 'Total',
                    label: {
                        normal: {
                            show: true,
                            position: 'top'
                        }
                    },
                    areaStyle: {normal: {}},
                    data: data.triggerDayCountFailList
                },
                {
                    name:I18n.joblog_status_running,
                    type:'line',
                    stack: 'Total',
                    areaStyle: {normal: {}},
                    data: data.triggerDayCountRunningList
                }
            ],
            color:['#00A65A', '#c23632', '#F39C12']
        };

        var lineChart = echarts.getInstanceByDom(chartElements[0]) || echarts.init(chartElements[0]);
        lineChart.setOption(option);
    }

    /**
     * pie Chart Init
     */
    function pieChartInit(data) {
        var option = {
            title : {
                text: I18n.job_dashboard_rate_report ,
                /*subtext: 'subtext',*/
                x:'center'
            },
            tooltip : {
                trigger: 'item',
                formatter: "{b} : {c} ({d}%)"
            },
            legend: {
                orient: 'vertical',
                left: 'left',
                data: [I18n.joblog_status_suc, I18n.joblog_status_fail, I18n.joblog_status_running ]
            },
            series : [
                {
                    //name: '分布比例',
                    type: 'pie',
                    radius : '55%',
                    center: ['50%', '60%'],
                    data:[
                        {
                            name:I18n.joblog_status_suc,
                            value:data.triggerCountSucTotal
                        },
                        {
                            name:I18n.joblog_status_fail,
                            value:data.triggerCountFailTotal
                        },
                        {
                            name:I18n.joblog_status_running,
                            value:data.triggerCountRunningTotal
                        }
                    ],
                    itemStyle: {
                        emphasis: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                }
            ],
            color:['#00A65A', '#c23632', '#F39C12']
        };
        var pieChart = echarts.getInstanceByDom(chartElements[1]) || echarts.init(chartElements[1]);
        pieChart.setOption(option);
    }

});
</script>
<script>
$(function () {

    var chartData = null;
    var chartFrame = null;
    var chartRequest = null;
    var requestVersion = 0;
    var chartElements = [document.getElementById('reportLineChart'), document.getElementById('reportPieChart')];
    var resumeRequest = false;

    // 页签iframe可能仍被隐藏；只在容器尺寸有效后绘图，避免ECharts记住零宽度。
    function renderCharts() {
        chartFrame = null;
        if (!chartData || chartElements.some(function (element) {
            return element.clientWidth <= 0 || element.clientHeight <= 0;
        })) {
            return;
        }
        reportLineChartInit(chartData);
        reportPieChartInit(chartData);
        chartElements.forEach(function (element) {
            echarts.getInstanceByDom(element).resize();
        });
    }

    function scheduleCharts() {
        if (chartFrame === null) {
            chartFrame = window.requestAnimationFrame(renderCharts);
        }
    }

    // iframe首次显示、页签切回及侧栏伸缩都会改变容器宽度，无需刷新报表数据。
    var chartObserver = window.ResizeObserver ? new ResizeObserver(scheduleCharts) : null;
    if (chartObserver) {
        chartElements.forEach(function (element) { chartObserver.observe(element); });
    }
    $(window).on('resize.dashboardCharts load.dashboardCharts', scheduleCharts);
    $(window).on('pageshow.dashboardCharts', function () {
        if (chartObserver) {
            chartElements.forEach(function (element) { chartObserver.observe(element); });
        }
        if (resumeRequest) {
            resumeRequest = false;
            refreshReport();
        }
        scheduleCharts();
    });
    $(window).on('pagehide.dashboardCharts', function () {
        if (chartObserver) {
            chartObserver.disconnect();
        }
        if (chartFrame !== null) {
            window.cancelAnimationFrame(chartFrame);
            chartFrame = null;
        }
        if (chartRequest) {
            resumeRequest = true;
            chartRequest.abort();
        }
        chartElements.forEach(function (element) {
            var chart = echarts.getInstanceByDom(element);
            if (chart) {
                chart.dispose();
            }
        });
    });

    var report = $('#jobReport');
    var group = $('#reportGroup');
    var dateInput = $('#reportFilterTime');
    var today = moment(report.attr('data-today'), 'YYYY-MM-DD', true);
    var lastWeek = [today.clone().subtract(6, 'days'), today.clone()];
    var ranges = {};
    ranges[I18n.daterangepicker_ranges_today] = [today.clone(), today.clone()];
    ranges[I18n.daterangepicker_ranges_recent_week] = lastWeek;
    ranges[I18n.daterangepicker_ranges_this_month] = [today.clone().startOf('month'), today.clone()];
    ranges[I18n.daterangepicker_ranges_last_month] = [
        today.clone().subtract(1, 'month').startOf('month'),
        today.clone().subtract(1, 'month').endOf('month')
    ];
    var outcomes = [
        { key: 'succeeded', label: I18n.joblog_status_suc },
        { key: 'dispatchFailed', label: I18n.job_report_dispatch_failed },
        { key: 'executionFailed', label: I18n.job_report_execution_failed },
        { key: 'pending', label: I18n.job_report_pending }
    ];
    var outcomeColors = ['#00844c', '#b33d37', '#bb6126', '#3d75a7'];

    dateInput.daterangepicker({
        startDate: lastWeek[0],
        endDate: lastWeek[1],
        maxDate: today.clone().endOf('day'),
        dateLimit: { days: Number(report.attr('data-max-days')) - 1 },
        opens: 'left',
        ranges: ranges,
        locale: {
            format: 'YYYY-MM-DD',
            separator: ' - ',
            customRangeLabel: I18n.daterangepicker_custom_name,
            applyLabel: I18n.system_ok,
            cancelLabel: I18n.system_cancel,
            fromLabel: I18n.daterangepicker_custom_starttime,
            toLabel: I18n.daterangepicker_custom_endtime,
            daysOfWeek: I18n.daterangepicker_custom_daysofweek.split(','),
            monthNames: I18n.daterangepicker_custom_monthnames.split(','),
            firstDay: 1
        }
    }, function () { loadReport(1); });
    dateInput.on('show.daterangepicker', function (_, picker) {
        picker.container.addClass('zero-job-date-picker');
    });
    group.on('change', function () { loadReport(1); });
    $('#refreshReport').on('click', refreshReport);
    $('#previousPending').on('click', function () { goToPendingPage(-1); });
    $('#nextPending').on('click', function () { goToPendingPage(1); });
    loadReport(1);

    function refreshReport() {
        if (chartData && !$('#reportContent').prop('hidden')) {
            loadReport(chartData.page, 'refresh');
        } else {
            loadReport(1);
        }
    }

    function goToPendingPage(offset) {
        if (!chartData || chartRequest || $('#reportContent').prop('hidden')) {
            return;
        }
        var page = chartData.page + offset;
        if (page >= 1 && page <= Math.ceil(chartData.pendingTotal / chartData.pageSize)) {
            loadReport(page, 'page');
        }
    }

    function loadReport(page, mode) {
        var currentVersion = ++requestVersion;
        if (chartRequest) {
            chartRequest.abort();
        }
        var dateRange = dateInput.data('daterangepicker');
        var start = dateRange.startDate.format('YYYY-MM-DD');
        var end = dateRange.endDate.format('YYYY-MM-DD');
        var paging = mode === 'page';
        var statusMessage = $(paging ? '#pendingStatus' : '#reportStatus');
        var failedMessage = I18n.job_dashboard_report_loaddata_fail;
        if (paging) {
            failedMessage = I18n.job_report_pending_failed;
        } else if (mode === 'refresh') {
            failedMessage = I18n.job_report_refresh_failed;
        }
        setPendingBusy(!!mode);
        if (!paging) {
            $('#pendingStatus').text('');
        }
        if (!mode) {
            $('#reportContent').prop('hidden', true);
        }
        statusMessage.text(paging ? I18n.job_report_pending_loading : I18n.job_report_loading);
        chartRequest = $.ajax({
            url: base_url + '/dashboard/report',
            data: { startDate: start, endDate: end, jobGroup: group.val(), page: page },
            dataType: 'json',
            success: function (response) {
                if (currentVersion !== requestVersion) {
                    return;
                }
                var data = response && response.data;
                if (!data || response.code !== 200 || !Array.isArray(data.trend)
                        || !Array.isArray(data.failedJobs) || !Array.isArray(data.pendingJobs)) {
                    statusMessage.text((response && response.msg) || failedMessage);
                    return;
                }
                chartData = data;
                chartData.totals = outcomes.map(function (outcome) {
                    return chartData.trend.reduce(function (sum, day) { return sum + day[outcome.key]; }, 0);
                });
                renderReport(chartData, start, end);
                statusMessage.text('');
                $('#reportStatus').text('');
                $('#reportContent').prop('hidden', false);
                scheduleCharts();
            },
            error: function (_, status) {
                if (status !== 'abort' && currentVersion === requestVersion) {
                    statusMessage.text(failedMessage);
                }
            },
            complete: function () {
                if (currentVersion === requestVersion) {
                    chartRequest = null;
                    setPendingBusy(false);
                }
            }
        });
    }

    function setPendingBusy(busy) {
        $('#pendingJobs').attr('aria-busy', String(busy));
        $('#previousPending, #nextPending').attr('aria-disabled', busy ? 'true' : null);
    }

    function renderReport(data, start, end) {
        var completed = data.totals[0] + data.totals[1] + data.totals[2];
        $('#reportAttempts').text(completed + data.totals[3]);
        $('#reportRate').text(percent(data.totals[0], completed));

        var failed = $('#failedJobs').empty();
        data.failedJobs.forEach(function (job) {
            var query = $.param({ jobGroup: job.jobGroup, jobId: job.jobId, startDate: start, endDate: end, failedOnly: true });
            var link = $('<a>').attr({ href: base_url + '/joblog?' + query, target: '_blank', rel: 'noopener' })
                .text(I18n.job_report_view_logs);
            var row = appendRow(failed, [jobLabel(job), groupLabel(job.jobGroup), job.failed, job.attempts,
                percent(job.failed, job.attempts), formatTime(job.lastFailure)]);
            row.append($('<td>').append(link));
        });
        if (!data.failedJobs.length) {
            appendEmpty(failed, 7);
        }

        var pending = $('#pendingJobs').empty();
        data.pendingJobs.forEach(function (job) {
            var waiting = Math.max(0, moment.utc(data.asOf).diff(moment.utc(job.triggerTime), 'minutes'));
            appendRow(pending, [job.logId, jobLabel(job), groupLabel(job.jobGroup), job.executorAddress || '—',
                formatTime(job.triggerTime), waiting]);
        });
        if (!data.pendingJobs.length) {
            appendEmpty(pending, 6);
        }

        var pageCount = Math.ceil(data.pendingTotal / data.pageSize);
        $('#pendingPagination').prop('hidden', pageCount < 2);
        $('#pendingPage').text(data.page + ' / ' + pageCount + ' · ' + data.pendingTotal);
        $('#previousPending').prop('disabled', data.page <= 1);
        $('#nextPending').prop('disabled', data.page >= pageCount);
    }

    function jobLabel(job) {
        return '#' + job.jobId + (job.jobName ? ' ' + job.jobName : '');
    }

    function groupLabel(id) {
        return group.find('option[value="' + id + '"]').text() || String(id);
    }

    function percent(numerator, denominator) {
        return denominator > 0 ? (100 * numerator / denominator).toFixed(1) + '%' : '—';
    }

    function formatTime(value) {
        // 接口返回上海本地时间，固定解析方式以避免浏览器夏令时调整时钟值。
        return value ? moment.utc(value).format('YYYY-MM-DD HH:mm:ss') : '—';
    }

    function appendRow(table, cells) {
        var row = $('<tr>').appendTo(table);
        cells.forEach(function (value) { row.append($('<td>').text(value)); });
        return row;
    }

    function appendEmpty(table, columns) {
        $('<tr>').append($('<td>').attr('colspan', columns).text(I18n.job_report_empty)).appendTo(table);
    }

    function reportLineChartInit(data) {
        var element = chartElements[0];
        var chart = echarts.getInstanceByDom(element) || echarts.init(element);
        chart.setOption({
            color: outcomeColors,
            tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
            legend: { type: 'scroll', data: outcomes.map(function (outcome) { return outcome.label; }) },
            grid: { left: 16, right: 16, top: 42, bottom: 12, containLabel: true },
            xAxis: [{ type: 'category', data: data.trend.map(function (day) { return day.day; }) }],
            yAxis: [{ type: 'value', minInterval: 1 }],
            series: outcomes.map(function (outcome) {
                return {
                    name: outcome.label, type: 'bar', stack: 'attempts', barMaxWidth: 32,
                    data: data.trend.map(function (day) { return day[outcome.key]; })
                };
            })
        });
    }

    function reportPieChartInit(data) {
        var element = chartElements[1];
        var chart = echarts.getInstanceByDom(element) || echarts.init(element);
        var slices = outcomes.map(function (outcome, index) {
            return { name: outcome.label, value: data.totals[index], itemStyle: { color: outcomeColors[index] } };
        }).filter(function (slice) { return slice.value > 0; });
        chart.setOption({
            title: {
                text: I18n.job_report_distribution, subtext: slices.length ? '' : I18n.job_report_empty,
                left: 'center', textStyle: { fontSize: 14 }
            },
            tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
            series: [{
                type: 'pie', radius: ['44%', '68%'], center: ['50%', '55%'], stillShowZeroSum: false,
                data: slices
            }]
        });
    }

});
</script>
<!-- 3-script end -->

</body>
</html>
