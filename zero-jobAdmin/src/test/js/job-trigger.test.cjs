const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const { test } = require('node:test');

function manualDispatch() {
    const template = fs.readFileSync(path.join(__dirname,
        '../../main/resources/templates/business/job.list.ftl'), 'utf8');
    const script = template.split('// ---------------------- start and stop ----------------------')[1]
        .split('// ---------------------- job_log ----------------------')[0];
    const fields = {};
    const requests = [];
    const notices = [];
    const confirmations = [];
    const events = {};
    let selectedIds = [42];
    let refreshCount = 0;
    let status = '';
    let disabled = false;
    let visible = false;
    let resets = 0;
    const form = {
        0: { reset() { resets++; } },
        serialize() { return new URLSearchParams(fields).toString(); },
        find() { return { prop(name, value) { disabled = value; } }; }
    };
    const modal = {
        find: () => form,
        on(event, callback) { events[event] = callback; },
        modal(options) {
            if (options === 'hide') {
                if (events['hide.bs.modal']() !== false) visible = false;
            } else {
                visible = true;
            }
        }
    };
    function $(selector) {
        if (selector === '<span>') {
            return {
                text(message) { this.message = message; return this; },
                html() { return this.message.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;'); }
            };
        }
        if (selector === '#jobTriggerModal') return modal;
        if (selector === '#jobTriggerStatus') {
            return { text(value) { status = value; }, empty() { status = ''; } };
        }
        return {
            on(event, callbackOrSelector, callback) {
                events[callback ? callbackOrSelector : selector] = callback || callbackOrSelector;
                return this;
            },
            click() { assert.equal(selector, '#data_filter .searchBtn'); refreshCount++; },
            val(value) { fields[selector.match(/name='(.*?)'/)[1]] = value; }
        };
    }
    $.adminTable = {
        selectIds: () => selectedIds,
        table: { bootstrapTable: () => [{ id: 42, executorParam: 'fixture' }] }
    };
    $.ajax = options => requests.push(options);
    vm.runInNewContext(script, {
        $, base_url: '', window: {}, I18n: new Proxy({}, { get: (_, key) => key }),
        layer: {
            msg: value => notices.push(value),
            confirm(message, options, confirm) { confirmations.push({ options, confirm }); },
            close(index) { confirmations[index].options.end(); }
        }
    });
    return { fields, requests, notices, confirmations, status: () => status, disabled: () => disabled,
        select: ids => { selectedIds = ids; },
        start: () => events['.job_resume'](), stop: () => events['.job_pause'](),
        refreshCount: () => refreshCount,
        visible: () => visible, resets: () => resets,
        open: () => events['.job_trigger'](),
        submit: () => events['#jobTriggerModal .ok'](),
        close: () => modal.modal('hide')
    };
}

test('启停只接受单条选择，确认和请求期间不会重复提交', () => {
    const ui = manualDispatch();
    ui.select([12, 42]);
    ui.start();
    assert.equal(ui.confirmations.length, 0);
    ui.select([42]);
    ui.start();
    ui.stop();
    assert.equal(ui.confirmations.length, 1);
    ui.confirmations[0].options.end();
    ui.start();
    ui.confirmations[1].confirm(1);
    ui.confirmations[1].confirm(1);
    ui.stop();
    assert.equal(ui.confirmations.length, 2);
    assert.equal(ui.requests.length, 1);
    assert.equal(ui.requests[0].url, '/jobinfo/start');
    assert.deepEqual(ui.requests[0].data.ids, [42]);

    ui.requests[0].success({ code: 200 });
    ui.requests[0].complete();
    assert.equal(ui.notices.at(-1), 'jobinfo_opt_startsystem_success');
    assert.equal(ui.refreshCount(), 1);
    ui.stop();
    ui.confirmations[2].confirm(2);
    assert.equal(ui.requests[1].url, '/jobinfo/stop');
});

test('启停响应超时或状态冲突时提示并重新查询状态', () => {
    const ui = manualDispatch();
    ui.stop();
    ui.confirmations[0].confirm(0);
    ui.requests[0].error();
    ui.requests[0].complete();
    assert.equal(ui.notices.at(-1), 'jobinfo_state_unconfirmed');
    assert.equal(ui.requests.length, 1);
    assert.equal(ui.refreshCount(), 1);

    ui.stop();
    ui.confirmations[1].confirm(1);
    ui.requests[1].success({ code: 500, msg: '<状态已改变>' });
    ui.requests[1].complete();
    assert.equal(ui.notices.at(-1), '&lt;状态已改变&gt;');
    assert.equal(ui.refreshCount(), 2);
});

test('执行一次校验失败保留参数，多次点击只提交一次', () => {
    const ui = manualDispatch();
    ui.open();
    ui.fields.addressList = 'https://';
    ui.submit();
    ui.submit();
    ui.close();
    assert.equal(ui.requests.length, 1);
    assert.equal(ui.visible(), true);
    assert.equal(ui.disabled(), true);
    assert.equal(ui.status(), 'jobinfo_trigger_submitting');
    assert.equal(new URLSearchParams(ui.requests[0].data).get('executorParam'), 'fixture');
    ui.requests[0].success({ code: 500, msg: '地址非法' });
    ui.requests[0].complete();
    assert.equal(ui.status(), '地址非法');
    assert.equal(ui.disabled(), false);
    assert.equal(ui.resets(), 0);
    assert.equal(ui.fields.addressList, 'https://');
});

test('网络异常提示核对日志，提交成功只提示已提交调度', () => {
    const ui = manualDispatch();
    ui.open();
    ui.submit();
    ui.requests[0].error();
    ui.requests[0].complete();
    assert.equal(ui.status(), 'jobinfo_trigger_unconfirmed');
    assert.equal(ui.requests.length, 1);
    assert.equal(ui.visible(), true);

    ui.submit();
    ui.requests[1].success({ code: 200 });
    ui.requests[1].complete();
    assert.equal(ui.visible(), false);
    assert.equal(ui.resets(), 1);
    assert.deepEqual(ui.notices, ['jobinfo_trigger_queued']);
});
