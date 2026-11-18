const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const { test } = require('node:test');

function logActions() {
    const template = fs.readFileSync(path.join(__dirname,
        '../../main/resources/templates/business/log.list.ftl'), 'utf8');
    const script = template.split('var killInProgress = false;')[1].split('/**')[0];
    const confirmations = [];
    const requests = [];
    const results = [];
    let clickKill;
    let refreshed = 0;
    function $(selector) {
        return {
            on(event, operation, handler) { clickKill = handler; },
            click() { assert.equal(selector, '#data_filter .searchBtn'); refreshed++; },
            text(value) { this.value = value; return this; },
            prop() { return '<div>' + this.value.replaceAll('&', '&amp;').replaceAll('<', '&lt;') + '</div>'; }
        };
    }
    $.adminTable = { table: { bootstrapTable: () => [{ id: '9007199254740993' }] } };
    $.ajax = options => requests.push(options);
    vm.runInNewContext('var killInProgress = false;' + script, {
        $, base_url: '', I18n: new Proxy({}, { get: (_, key) => key }),
        layer: {
            confirm(message, options, confirm) { confirmations.push({ options, confirm }); },
            close(index) { confirmations[index].options.end(); },
            open(options) { results.push(options); }
        }
    });
    return { requests, results, confirmations, click: () => clickKill(), refreshed: () => refreshed };
}

test('终止任务取消确认后可以重开，多次点击只提交一次', () => {
    const ui = logActions();
    ui.click();
    ui.click();
    assert.equal(ui.confirmations.length, 1);
    ui.confirmations[0].options.end();
    ui.click();
    ui.confirmations[1].confirm(1);
    ui.confirmations[1].confirm(1);
    ui.click();
    assert.equal(ui.confirmations.length, 2);
    assert.equal(ui.requests.length, 1);
    assert.equal(ui.requests[0].data.id, '9007199254740993');
    ui.requests[0].success({ code: 200 });
    ui.requests[0].complete();
    assert.equal(ui.results[0].icon, 1);
    ui.results[0].end();
    assert.equal(ui.refreshed(), 1);
});

test('终止请求结果不明或状态变化时会提示并刷新列表', () => {
    const ui = logActions();
    ui.click();
    ui.confirmations[0].confirm(0);
    ui.requests[0].error();
    ui.requests[0].complete();
    assert.equal(ui.results[0].icon, 2);
    assert.match(ui.results[0].content, /joblog_kill_unconfirmed/);
    ui.results[0].end();

    ui.click();
    ui.confirmations[1].confirm(1);
    ui.requests[1].success({ code: 500, msg: '<状态已变化>' });
    ui.requests[1].complete();
    assert.match(ui.results[1].content, /&lt;状态已变化/);
    ui.results[1].end();
    assert.equal(ui.refreshed(), 2);
});
