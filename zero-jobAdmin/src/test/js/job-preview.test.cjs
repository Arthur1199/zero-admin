const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const { test } = require('node:test');

function jobPreview() {
    const template = fs.readFileSync(path.join(__dirname,
        '../../main/resources/templates/business/job.list.ftl'), 'utf8');
    const script = template.split('// ---------------------- job_next_time ----------------------')[1]
        .split('// ---------------------- add ----------------------')[0];
    const elements = new Map();
    const requests = [];
    const trigger = { focused: false, focus() { this.focused = true; } };
    let selectedJob;

    function $(selector) {
        if (selector === '<li>') return { text(value) { return value; } };
        if (!elements.has(selector)) {
            elements.set(selector, {
                children: [], events: {}, properties: {},
                on(event, handlerOrSelector, handler) {
                    this.events[handler ? handlerOrSelector : event] = handler || handlerOrSelector;
                    return this;
                },
                modal(options) { this.modalOptions = options; return this; },
                toggleClass(name, active) { this.properties[name] = active; return this; },
                text(value) { this.textValue = value; return this; },
                prop(name, value) { this.properties[name] = value; return this; },
                empty() { this.children = []; return this; },
                append(value) { this.children.push(value); return this; }
            });
        }
        return elements.get(selector);
    }
    $.adminTable = { table: { bootstrapTable: () => [selectedJob] } };
    $.ajax = options => {
        const request = { options, aborted: false, abort() {
            this.aborted = true;
            options.error({}, 'abort');
            options.complete();
        } };
        requests.push(request);
        return request;
    };
    vm.runInNewContext(script, {
        $, base_url: '', window: {}, document: { contains: node => node === trigger },
        I18n: new Proxy({}, { get: (_, key) => key })
    });

    return {
        requests, trigger, modal: $('#jobPreviewModal'), status: $('#jobPreviewStatus'), times: $('#jobPreviewTimes'),
        task: $('#jobPreviewTask'), title: $('#jobPreviewTitle'),
        open(job, action = '.job_next_time') {
            selectedJob = job;
            $('#data_operation').events[action].call(trigger);
        }
    };
}

test('关闭预览取消旧查询，后打开的任务只显示自己的执行时间', () => {
    const ui = jobPreview();
    ui.open({ id: 1, name: '旧任务', scheduleType: 'CRON', scheduleConf: '0 0 * * * ?' });
    assert.equal(ui.status.textValue, 'jobinfo_next_time_loading');
    ui.modal.events['hide.bs.modal']();
    ui.modal.events['hidden.bs.modal']();
    assert.equal(ui.requests[0].aborted, true);
    assert.equal(ui.trigger.focused, true);

    ui.open({ id: 2, name: '当前任务 <示例>', scheduleType: 'FIX_RATE', scheduleConf: '10' });
    ui.requests[0].options.success({ code: 200, data: ['旧时间'] });
    assert.equal(ui.task.textValue, '#2 当前任务 <示例>');
    assert.equal(ui.times.properties.hidden, true);
    assert.deepEqual(ui.times.children, []);
    assert.equal(ui.requests[1].options.data.id, 2);
    ui.requests[1].options.success({ code: 200, data: ['2026-11-18 21:15:00'] });
    ui.requests[1].options.complete();
    assert.equal(ui.status.properties.hidden, true);
    assert.equal(ui.times.properties.hidden, false);
    assert.deepEqual(ui.times.children, ['2026-11-18 21:15:00']);
});

test('注册节点与执行时间共用弹窗，切换后忽略旧节点结果', () => {
    const ui = jobPreview();
    ui.open({ id: 1, jobGroup: 12, name: '任务一' }, '.job_registryinfo');
    assert.equal(ui.title.textValue, 'jobinfo_opt_registryinfo');
    assert.equal(ui.modal.properties['job-preview-wide'], true);
    assert.equal(ui.requests[0].options.url, '/jobgroup/loadById');
    assert.equal(ui.requests[0].options.data.id, 12);
    ui.requests[0].options.success({ code: 200, data: { registryList: ['http://example.invalid?q=<value>'] } });
    assert.deepEqual(ui.times.children, ['http://example.invalid?q=<value>']);

    ui.open({ id: 2, scheduleType: 'NONE' });
    assert.equal(ui.requests[0].aborted, true);
    ui.requests[0].options.success({ code: 200, data: { registryList: ['旧地址'] } });
    assert.equal(ui.title.textValue, 'jobinfo_opt_next_time');
    assert.equal(ui.modal.properties['job-preview-wide'], false);
    assert.deepEqual(ui.times.children, []);
    ui.requests[1].options.success({ code: 200, data: [] });
    assert.equal(ui.status.textValue, 'jobinfo_next_time_empty');
});

test('无计划与接口异常在同一个预览弹窗内提示', () => {
    const ui = jobPreview();
    ui.open({ id: 3, scheduleType: 'NONE' });
    ui.requests[0].options.success({ code: 200, data: [] });
    ui.requests[0].options.complete();
    assert.equal(ui.status.textValue, 'jobinfo_next_time_empty');
    assert.equal(ui.times.properties.hidden, true);

    ui.open({ id: 4, scheduleType: 'CRON' });
    ui.requests[1].options.error({}, 'error');
    assert.equal(ui.status.textValue, 'system_opt_fail');
    assert.equal(ui.status.properties.hidden, false);
    assert.equal(ui.times.properties.hidden, true);
});
