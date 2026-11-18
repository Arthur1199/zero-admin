const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const { test } = require('node:test');
const moment = require('../../main/resources/static/adminlte/bower_components/moment/moment.min.js');

function logFilter(reportStart = '', reportEnd = '', jobName = '', search = '', selectedJob = '123') {
    const template = fs.readFileSync(path.join(__dirname,
        '../../main/resources/templates/business/log.list.ftl'), 'utf8');
    const script = template.split('// init filter')[1].split('// ---------------------- page ----------------------')[0]
        .replace('${jobGroup?c}', '2').replace('${jobId?c}', selectedJob);
    const state = {};
    const handlers = {};
    const window = { location: { search } };
    let tableOptions;
    const formatValue = value => moment.isMoment(value) ? value.format('YYYY-MM-DD HH:mm:ss') : value;
    const picker = {
        setStartDate(value) { state.start = formatValue(value); },
        setEndDate(value) { state.end = formatValue(value); }
    };
    function $(selector) {
        return {
            attr: name => name === 'data-report-start' ? reportStart : reportEnd,
            data: () => picker,
            val(value) {
                if (value === undefined) {
                    return selector === '#filterTime' ? state.start + ' - ' + state.end : state[selector];
                }
                state[selector] = value;
                return this;
            },
            on(event, handler) { handlers[selector] = handler; },
            trigger() { return this; },
            find() { return { text: () => jobName }; },
            text(value) { this.textValue = value; return this; },
            html() { return this.textValue.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;'); }
        };
    }
    $.adminTable = { initTable(options) { tableOptions = options; } };
    $.param = params => new URLSearchParams(params).toString();
    const page = vm.createContext({
        $, base_url: '', window, URLSearchParams, moment, formatRemarkAction() {},
        I18n: { daterangepicker_ranges_recent_week: 'week', joblog_name: '日志' },
        rangesConf: { week: ['week-start', 'week-end'] }
    });
    vm.runInContext(script, page);
    const tableScript = template.split('$.adminTable.initTable(')[1].split('\n\t\t});')[0];
    vm.runInContext('$.adminTable.initTable(' + tableScript + '\n\t\t});', page);
    return {
        state, reset: page.resetFilter, tableOptions,
        switchGroup(id) {
            state['#jobGroup'] = id;
            handlers['#jobGroup'].call('#jobGroup');
            return window.location.href;
        }
    };
}

test('失败榜下钻后重置会恢复原执行器组、任务、失败状态及完整日期', () => {
    const { state, reset } = logFilter('2026-11-10', '2026-11-18');
    assert.equal(state.start, '2026-11-10 00:00:00');
    assert.equal(state.end, '2026-11-18 23:59:59');
    state.start = 'changed';
    state.end = 'changed';
    state['#jobId'] = '0';
    state['#logStatus'] = '-1';
    reset();
    assert.deepEqual(state, {
        start: '2026-11-10 00:00:00', end: '2026-11-18 23:59:59',
        '#jobGroup': '2', '#jobId': '123', '#logStatus': '2'
    });
});

test('普通调度日志入口仍重置到最近一周和全部状态', () => {
    const { state, reset } = logFilter();
    state['#logStatus'] = '2';
    reset();
    assert.equal(state.start, 'week-start');
    assert.equal(state.end, 'week-end');
    assert.equal(state['#logStatus'], '-1');
});

test('调度日志表格按纯文本显示任务名称中的尖括号和与号', () => {
    const { tableOptions } = logFilter('', '', '<任务>&');
    const formatter = tableOptions.columns.find(column => column.field === 'jobId').formatter;
    assert.equal(formatter(123, { jobId: 123 }), '【123】&lt;任务&gt;&amp;');
});

test('切换执行器组继承日期及状态，新页面按该组查询全部任务', () => {
    const first = logFilter('2026-11-10', '2026-11-18');
    const url = new URL(first.switchGroup('9876'), 'http://example.test');
    assert.equal(url.pathname, '/joblog');
    assert.equal(url.searchParams.get('jobGroup'), '9876');
    assert.equal(url.searchParams.get('logStatus'), '2');
    assert.equal(url.searchParams.get('filterTime'), '2026-11-10 00:00:00 - 2026-11-18 23:59:59');
    assert.equal(url.searchParams.has('jobId'), false);
    const destination = logFilter('', '', '', url.search, '0');
    assert.equal(destination.state['#jobId'], '0');
    assert.equal(destination.state['#logStatus'], '2');
    assert.equal(destination.state.start, '2026-11-10 00:00:00');
    destination.reset();
    assert.equal(destination.state.start, 'week-start');
    assert.equal(destination.state['#logStatus'], '-1');
});

test('组切换链接中的非法日期和状态回退到默认筛选', () => {
    const range = '2026-02-30 00:00:00 - 2026-03-01 23:59:59';
    const search = '?' + new URLSearchParams({ jobGroup: '2', filterTime: range, logStatus: '9' });
    const { state } = logFilter('', '', '', search, '0');
    assert.equal(state.start, 'week-start');
    assert.equal(state['#logStatus'], '-1');
});

test('调度日志的日期快捷选项以服务端提供的上海日期为基准', () => {
    const template = fs.readFileSync(path.join(__dirname,
        '../../main/resources/templates/business/log.list.ftl'), 'utf8');
    const script = template.split('var rangesConf = {};')[1].split("$('#filterTime').daterangepicker(")[0];
    const rangesConf = {};
    vm.runInNewContext(script, {
        rangesConf, moment,
        $: () => ({ attr: () => '2026-01-01' }),
        I18n: new Proxy({}, { get: (_, key) => key })
    });
    const format = value => value.format('YYYY-MM-DD HH:mm:ss');
    assert.deepEqual(Array.from(rangesConf.daterangepicker_ranges_today, format),
        ['2026-01-01 00:00:00', '2026-01-01 23:59:59']);
    assert.deepEqual(Array.from(rangesConf.daterangepicker_ranges_yesterday, format),
        ['2025-12-31 00:00:00', '2025-12-31 23:59:59']);
});
