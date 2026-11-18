const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const { test } = require('node:test');

function logFilter(reportStart = '', reportEnd = '', jobName = '') {
    const template = fs.readFileSync(path.join(__dirname,
        '../../main/resources/templates/business/log.list.ftl'), 'utf8');
    const script = template.split('// init filter')[1].split('// ---------------------- page ----------------------')[0]
        .replace('${jobGroup}', '2').replace('${jobId}', '123');
    const state = {};
    let tableOptions;
    const picker = {
        setStartDate(value) { state.start = value; },
        setEndDate(value) { state.end = value; }
    };
    function $(selector) {
        return {
            attr: name => name === 'data-report-start' ? reportStart : reportEnd,
            data: () => picker,
            val(value) { state[selector] = value; return this; },
            trigger() { return this; },
            find() { return { text: () => jobName }; },
            text(value) { this.textValue = value; return this; },
            html() { return this.textValue.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;'); }
        };
    }
    $.adminTable = { initTable(options) { tableOptions = options; } };
    const page = vm.createContext({
        $, base_url: '', formatRemarkAction() {},
        I18n: { daterangepicker_ranges_recent_week: 'week', joblog_name: '日志' },
        rangesConf: { week: ['week-start', 'week-end'] },
        moment(date) {
            return { startOf: () => date + ' 00:00:00', endOf: () => date + ' 23:59:59' };
        }
    });
    vm.runInContext(script, page);
    const tableScript = template.split('$.adminTable.initTable(')[1].split('\n\t\t});')[0];
    vm.runInContext('$.adminTable.initTable(' + tableScript + '\n\t\t});', page);
    return { state, reset: page.resetFilter, tableOptions };
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

test('调度日志的日期快捷选项以服务端提供的上海日期为基准', () => {
    const template = fs.readFileSync(path.join(__dirname,
        '../../main/resources/templates/business/log.list.ftl'), 'utf8');
    const script = template.split('var rangesConf = {};')[1].split("$('#filterTime').daterangepicker(")[0];
    const moment = require('../../main/resources/static/adminlte/bower_components/moment/moment.min.js');
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
