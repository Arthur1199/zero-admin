const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const { test } = require('node:test');

// 模拟隐藏iframe、异步请求及尺寸变化，不连接数据库或执行器。
function dashboard() {
    const template = fs.readFileSync(path.join(__dirname,
        '../../main/resources/templates/framework/base/dashboard.ftl'), 'utf8');
    const script = template.match(/<script>\s*([\s\S]*?)<\/script>/)[1];
    const elements = Object.fromEntries(['reportLineChart', 'reportPieChart'].map(id =>
        [id, { clientWidth: 0, clientHeight: 350 }]));
    const frames = new Map();
    const instances = new Map();
    const requests = [];
    const events = {};
    const textValues = {};
    const properties = {};
    const attributes = {};
    const nodes = [];
    let sequence = 0;
    let observeResize;
    let changeDate;
    let initCount = 0;
    let selectedRange;
    let selectedGroup = '0';
    const window = {
        ResizeObserver: class {
            constructor(callback) { observeResize = callback; }
            observe() {}
            disconnect() {}
        },
        requestAnimationFrame(callback) { frames.set(++sequence, callback); return sequence; },
        cancelAnimationFrame(id) { frames.delete(id); }
    };
    function $(target) {
        if (typeof target === 'function') { target(); return; }
        const element = {
            target,
            attributes: {},
            children: [],
            attr(name, value) {
                if (name === 'data-today') return '2026-11-18';
                if (name === 'data-max-days') return '31';
                if (typeof name === 'object') Object.assign(this.attributes, name);
                if (arguments.length === 2) (attributes[target] ??= {})[name] = value;
                return this;
            },
            data() { return selectedRange; },
            val() { return selectedGroup; },
            text(value) { textValues[target] = value; this.textContent = value; return this; },
            prop(name, value) {
                const props = properties[target] ??= {};
                if (arguments.length === 1) return props[name];
                props[name] = value;
                return this;
            },
            empty() { return this; },
            append(child) { this.children.push(child); return this; },
            appendTo() { return this; },
            find() { return { text: () => '测试执行器组' }; },
            on(names, callback) {
                names.split(' ').forEach(name => {
                    events[name] = callback;
                    events[target + ':' + name] = callback;
                });
            },
            daterangepicker(options, callback) { changeDate = callback; }
        };
        if (typeof target === 'string' && target.startsWith('<')) nodes.push(element);
        return element;
    }
    $.param = values => new URLSearchParams(values).toString();
    $.ajax = options => {
        const request = { options, aborted: false, abort() {
            this.aborted = true;
            options.error({}, 'abort');
            options.complete();
        } };
        requests.push(request);
        return request;
    };
    const date = { clone() { return this; }, startOf() { return this; }, endOf() { return this; },
        subtract() { return this; }, format() { return '2026-11-18'; } };
    selectedRange = { startDate: date, endDate: date };
    const moment = () => date;
    moment.utc = require('../../main/resources/static/adminlte/bower_components/moment/moment.min.js').utc;
    vm.runInNewContext(script, {
        $, window, ResizeObserver: window.ResizeObserver,
        document: { getElementById: id => elements[id] },
        moment, base_url: '',
        I18n: new Proxy({}, { get: (_, key) => key }),
        echarts: {
            getInstanceByDom: element => instances.get(element),
            init(element) {
                assert.ok(element.clientWidth > 0, '不能在零宽度容器中初始化');
                initCount++;
                const chart = { updates: [], resizes: 0,
                    setOption(option) { this.updates.push(option); },
                    resize() { this.resizes++; },
                    dispose() { instances.delete(element); } };
                instances.set(element, chart);
                return chart;
            }
        }
    });
    return { elements, instances, requests, events, textValues, properties, attributes, nodes,
        initCount: () => initCount,
        show() { Object.values(elements).forEach(element => { element.clientWidth = 600; }); observeResize(); },
        flush() { const callbacks = [...frames.values()]; frames.clear(); callbacks.forEach(callback => callback()); },
        changeDate(start = '2026-11-18', end = '2026-11-18') {
            selectedRange = { startDate: { format: () => start }, endDate: { format: () => end } };
            changeDate(selectedRange.startDate, selectedRange.endDate);
        },
        changeGroup(value) { selectedGroup = value; events['#reportGroup:change'](); },
        respond(index, marker, data = {}) {
            requests[index].options.success({ code: 200, data: {
                trend: [{ day: marker, succeeded: 1, dispatchFailed: 0, executionFailed: 0, pending: 0 }],
                failedJobs: [], pendingJobs: [], pendingTotal: 0, page: 1, pageSize: 10, ...data
            } });
            requests[index].options.complete();
        },
        fail(index) {
            requests[index].options.error({}, 'error');
            requests[index].options.complete();
        }
    };
}

test('隐藏页签收到数据后，显示时自动绘图并在尺寸变化时复用实例', () => {
    const page = dashboard();
    page.respond(0, 'initial');
    page.flush();
    assert.equal(page.initCount(), 0);
    page.show();
    page.flush();
    assert.equal(page.initCount(), 2);
    page.events['resize.dashboardCharts']();
    page.flush();
    assert.equal(page.initCount(), 2);
    assert.equal(page.instances.get(page.elements.reportLineChart).resizes, 2);
});

test('快速切换日期时旧请求不会覆盖最新报表，请求失败有提示', () => {
    const page = dashboard();
    page.changeDate();
    assert.equal(page.requests[0].aborted, true);
    assert.equal(page.textValues['#reportStatus'], 'job_report_loading');
    page.respond(1, 'latest');
    page.respond(0, 'stale');
    page.show();
    page.flush();
    assert.equal(page.instances.get(page.elements.reportLineChart).updates.at(-1).xAxis[0].data[0], 'latest');
    page.changeDate();
    page.requests[2].options.error({}, 'error');
    assert.equal(page.textValues['#reportStatus'], 'job_dashboard_report_loaddata_fail');
});

test('离开页面清理图表，浏览器恢复页面时可重新绘图', () => {
    const page = dashboard();
    page.respond(0, 'initial');
    page.show();
    page.flush();
    page.events['pagehide.dashboardCharts']();
    assert.equal(page.instances.size, 0);
    page.events['pageshow.dashboardCharts']();
    page.flush();
    assert.equal(page.instances.size, 2);
});

test('待回调翻页保留报表和当前页，失败可直接重试', () => {
    const page = dashboard();
    page.respond(0, 'initial', { pendingTotal: 21 });
    const next = page.events['#nextPending:click'];
    next();
    next();
    assert.equal(page.requests.length, 2);
    assert.equal(page.requests[1].options.data.page, 2);
    assert.equal(page.properties['#reportContent'].hidden, false);
    assert.equal(page.attributes['#pendingJobs']['aria-busy'], 'true');
    assert.equal(page.textValues['#pendingStatus'], 'job_report_pending_loading');

    page.fail(1);
    assert.equal(page.properties['#reportContent'].hidden, false);
    assert.equal(page.textValues['#pendingPage'], '1 / 3 · 21');
    assert.equal(page.textValues['#pendingStatus'], 'job_report_pending_failed');
    assert.equal(page.attributes['#pendingJobs']['aria-busy'], 'false');

    next();
    assert.equal(page.requests[2].options.data.page, 2);
    page.respond(2, 'retried', { pendingTotal: 11, page: 2 });
    assert.equal(page.textValues['#pendingPage'], '2 / 2 · 11');
    assert.equal(page.textValues['#pendingStatus'], '');
    next();
    assert.equal(page.requests.length, 3);
});

test('刷新报表保留待回调页码，失败时继续显示上次结果', () => {
    const page = dashboard();
    page.respond(0, 'initial', { pendingTotal: 21 });
    page.events['#nextPending:click']();
    page.respond(1, 'second page', { page: 2, pendingTotal: 21 });
    const refresh = page.events['#refreshReport:click'];
    refresh();
    assert.equal(page.requests[2].options.data.page, 2);
    assert.equal(page.properties['#reportContent'].hidden, false);
    page.fail(2);
    assert.equal(page.textValues['#pendingPage'], '2 / 3 · 21');
    assert.equal(page.textValues['#reportStatus'], 'job_report_refresh_failed');
    refresh();
    page.respond(3, 'fewer records', { page: 1, pendingTotal: 3 });
    assert.equal(page.textValues['#pendingPage'], '1 / 1 · 3');
    assert.equal(page.textValues['#reportStatus'], '');
});

test('刷新收到不完整数据时保留当前报表，后续可继续查询', () => {
    const page = dashboard();
    page.respond(0, 'initial', { pendingTotal: 21 });
    const refresh = page.events['#refreshReport:click'];
    refresh();
    page.respond(1, 'invalid', { pendingJobs: null });
    assert.equal(page.properties['#reportContent'].hidden, false);
    assert.equal(page.textValues['#pendingPage'], '1 / 3 · 21');
    assert.equal(page.textValues['#reportStatus'], 'job_report_refresh_failed');
    page.events['#nextPending:click']();
    assert.equal(page.requests[2].options.data.page, 2);
});

test('返回页面后恢复因离开而中断的报表请求', () => {
    const page = dashboard();
    page.events['pagehide.dashboardCharts']();
    assert.equal(page.requests[0].aborted, true);
    page.events['pageshow.dashboardCharts']();
    page.events['pageshow.dashboardCharts']();
    assert.equal(page.requests.length, 2);
    assert.equal(page.requests[1].options.data.page, 1);
    page.respond(1, 'restored');
    assert.equal(page.properties['#reportContent'].hidden, false);
    assert.equal(page.textValues['#reportStatus'], '');
});

test('切换执行器组取消正在加载的待回调页并回到第一页', () => {
    const page = dashboard();
    page.respond(0, 'initial', { pendingTotal: 21 });
    page.events['#nextPending:click']();
    page.changeGroup('2');
    assert.equal(page.requests[1].aborted, true);
    assert.equal(page.properties['#reportContent'].hidden, true);
    assert.equal(page.attributes['#pendingJobs']['aria-busy'], 'false');
    assert.equal(page.requests[2].options.data.jobGroup, '2');
    assert.equal(page.requests[2].options.data.page, 1);

    page.respond(2, 'new group', { pendingTotal: 1 });
    page.respond(1, 'old group', { pendingTotal: 21, page: 2 });
    assert.equal(page.textValues['#pendingPage'], '1 / 1 · 1');
    assert.equal(page.textValues['#pendingStatus'], '');
    assert.equal(page.properties['#reportContent'].hidden, false);
});

test('有失败与待回调记录时，失败下钻携带当前筛选值', () => {
    const page = dashboard();
    page.changeDate('2026-11-12', '2026-11-18');
    page.respond(1, '2026-11-18', {
        trend: [{ day: '2026-11-18', succeeded: 2, dispatchFailed: 1, executionFailed: 1, pending: 1 }],
        failedJobs: [{ jobGroup: 2, jobId: 42, jobName: '测试任务 <示例>', failed: 2, attempts: 5 }],
        pendingJobs: [{ logId: 51, jobGroup: 2, jobId: 42, triggerTime: '2026-03-08T02:30:00' }],
        pendingTotal: 1, asOf: '2026-03-08T03:10:00'
    });
    const link = page.nodes.find(node => node.target === '<a>');
    const url = new URL(link.attributes.href, 'http://fixture.invalid');
    assert.equal(url.pathname, '/joblog');
    assert.deepEqual(Object.fromEntries(url.searchParams), {
        jobGroup: '2', jobId: '42', startDate: '2026-11-12', endDate: '2026-11-18', failedOnly: 'true'
    });
    assert.equal(link.attributes.target, '_blank');
    assert.equal(link.attributes.rel, 'noopener');
    assert.equal(page.textValues['#reportAttempts'], 5);
    assert.equal(page.textValues['#reportRate'], '50.0%');
    assert.ok(page.nodes.some(node => node.target === '<td>' && node.textContent === '#42 测试任务 <示例>'));
    assert.ok(page.nodes.some(node => node.target === '<td>' && node.textContent === '2026-03-08 02:30:00'));
    assert.ok(page.nodes.some(node => node.target === '<td>' && node.textContent === 40));
});

test('结果分布清理零值标签，切回有数据时保留状态颜色', () => {
    const page = dashboard();
    page.show();
    const trend = [{ day: '2026-11-18', succeeded: 0, dispatchFailed: 3, executionFailed: 0, pending: 0 }];
    page.respond(0, '2026-11-18', { trend });
    page.flush();
    const pie = page.instances.get(page.elements.reportPieChart);
    const bars = page.instances.get(page.elements.reportLineChart);
    assert.equal(pie.updates.at(-1).series[0].data.length, 1);
    assert.equal(pie.updates.at(-1).series[0].data[0].itemStyle.color, bars.updates.at(-1).color[1]);

    page.changeDate();
    page.respond(1, '2026-11-18', { trend: [{ ...trend[0], dispatchFailed: 0 }] });
    page.flush();
    assert.equal(pie.updates.at(-1).title.subtext, 'job_report_empty');
    assert.equal(pie.updates.at(-1).series[0].data.length, 0);

    page.changeDate();
    page.respond(2, '2026-11-18', { trend });
    page.flush();
    assert.equal(pie.updates.at(-1).title.subtext, '');
    assert.equal(pie.updates.at(-1).series[0].data[0].value, 3);
});
