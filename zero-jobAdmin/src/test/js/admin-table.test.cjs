const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const { test } = require('node:test');

const script = fs.readFileSync(path.join(__dirname, '../../main/resources/static/framework/admin.table.js'), 'utf8');

function adminTable(options = {}) {
    const clickHandlers = new Map();
    const refreshPages = [];
    const message = {
        content: '',
        visible: false,
        hide() { this.visible = false; return this; },
        show() { this.visible = true; return this; },
        text(content) { this.content = content; return this; },
        empty() { return this.text(''); },
        insertBefore(target) { assert.equal(target, table); return this; }
    };
    let tableOptions;
    const table = {
        bootstrapTable(action, params) {
            if (typeof action === 'object') {
                tableOptions = action;
                return;
            }
            assert.equal(action, 'refresh');
            refreshPages.push(params.pageNumber);
        }
    };
    function $(selector, options) {
        if (selector === table || selector === '#data_list') return table;
        if (selector === '<div>') {
            assert.equal(options.role, 'status');
            return message;
        }
        return {
            on(event, callback) { clickHandlers.set(selector, callback); },
            click() { clickHandlers.get(selector)(); }
        };
    }
    $.extend = properties => Object.assign($, properties);
    vm.runInNewContext(script, {
        jQuery: $, document: { querySelector: () => null },
        I18n: { system_table_load_failed: '列表加载失败，请重试' }
    });
    $.adminTable.initTable({ table: '#data_list', ...options });
    return { tableOptions, clickHandlers, refreshPages, message };
}

test('分页表格点击搜索或重置均回到第一页', () => {
    let resetCount = 0;
    const ui = adminTable({ resetHandler: () => resetCount++ });
    ui.clickHandlers.get('#data_filter .searchBtn')();
    ui.clickHandlers.get('#data_filter .resetBtn')();
    assert.equal(resetCount, 1);
    assert.deepEqual(ui.refreshPages, [1, 1]);
});

test('列表失败显示就地提示，重试成功后清除提示，空列表仍视为成功', () => {
    const { tableOptions, message } = adminTable();
    assert.equal(tableOptions.responseHandler(null).total, 0);
    assert.equal(message.content, '列表加载失败，请重试');
    assert.equal(message.visible, true);
    tableOptions.responseHandler({ code: 500, msg: '查询失败 <请重试>' });
    assert.equal(message.content, '查询失败 <请重试>');
    tableOptions.onLoadError();
    assert.equal(message.content, '列表加载失败，请重试');
    assert.equal(message.visible, true);
    const result = tableOptions.responseHandler({ code: 200, data: { total: 0, data: [] } });
    assert.equal(result.rows.length, 0);
    assert.equal(message.visible, false);
    assert.equal(message.content, '');
});

function saveModal(mode) {
    const requests = [];
    const notices = [];
    const modalEvents = {};
    let open, submit, editing;
    let selected = { id: 42 };
    let refreshes = 0;
    const controls = { prop(name, value) { assert.equal(name, 'disabled'); this.disabled = value; } };
    const form = {
        0: { reset() {} },
        find: () => ({ removeClass() {} }),
        validate(options) { submit = options.submitHandler; return { resetForm() {} }; }
    };
    const modal = {
        visible: false,
        find(selector) { return selector === '.form' ? form : controls; },
        on(event, handler) { modalEvents[event] = handler; return this; },
        hasClass() { return this.visible; },
        modal(action) {
            if (action === 'show') this.visible = true;
            if (action === 'hide' && modalEvents['hide.bs.modal']() !== false) this.visible = false;
            return this;
        }
    };
    function $(selector) {
        if (selector === (mode === 'add' ? '#addModal' : '#updateModal')) return modal;
        if (selector === null) return { bootstrapTable: () => [selected] };
        if (selector === (mode === 'add' ? '#data_operation .add' : '#data_operation .update')) {
            return { click(handler) { open = handler; } };
        }
        if (selector === '#data_filter .searchBtn') return { click() { refreshes++; } };
        throw Error('Unexpected selector: ' + selector);
    }
    $.extend = properties => Object.assign($, properties);
    $.ajax = options => requests.push(options);
    vm.runInNewContext(script, {
        jQuery: $, I18n: new Proxy({}, { get: (_, key) => key }),
        layer: { msg: value => notices.push(value) }
    });
    const options = {
        url: '/jobinfo/' + (mode === 'add' ? 'insert' : 'update'),
        writeFormData(row) { editing = row || { id: 0 }; },
        readFormData: () => editing
    };
    const copy = mode === 'add' ? $.adminTable.initAdd(options) : $.adminTable.initUpdate(options);
    return {
        requests, notices, modal, controls,
        open: () => open(), submit: () => submit(), refreshes: () => refreshes, editing: () => editing,
        select: row => { selected = row; },
        copy: row => copy(() => { editing = row; })
    };
}

test('编辑保存期间不能重复提交或切换任务，失败后原表单仍可用', () => {
    const ui = saveModal('update');
    const { requests, notices, modal, controls } = ui;
    ui.open();
    ui.submit();
    ui.submit();
    ui.select({ id: 43 });
    ui.open();
    modal.modal('hide');
    assert.equal(requests.length, 1);
    assert.equal(requests[0].data.id, 42);
    assert.equal(controls.disabled, true);
    assert.equal(modal.visible, true);
    requests[0].error();
    requests[0].complete();
    assert.equal(notices.at(-1), 'system_save_unconfirmed');
    assert.equal(controls.disabled, false);
    assert.equal(ui.editing().id, 42);
    assert.equal(modal.visible, true);
    assert.equal(ui.refreshes(), 0);
    modal.modal('hide');
    ui.open();
    ui.submit();
    assert.equal(requests[1].data.id, 43);
    requests[1].success({ code: 200 });
    requests[1].complete();
    assert.equal(modal.visible, false);
    assert.equal(ui.refreshes(), 1);
});

test('新增和复制共用保存保护，复制不会覆盖等待结果的表单', () => {
    const ui = saveModal('add');
    ui.copy({ name: '复制的任务' });
    ui.submit();
    ui.open();
    ui.copy({ name: '另一条任务' });
    ui.submit();
    assert.equal(ui.requests.length, 1);
    assert.equal(ui.requests[0].url, '/jobinfo/insert');
    assert.equal(ui.requests[0].data.name, '复制的任务');
    ui.requests[0].error();
    ui.requests[0].complete();
    assert.equal(ui.editing().name, '复制的任务');
    assert.equal(ui.modal.visible, true);
    ui.modal.modal('hide');
    ui.open();
    assert.equal(ui.editing().id, 0);
});
