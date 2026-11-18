const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const { test } = require('node:test');

function glueIde() {
    const template = fs.readFileSync(path.join(__dirname, '../../main/resources/templates/business/job.code.ftl'), 'utf8');
    const script = template.split('<script>').at(-1).split('</script>')[0];
    const handlers = new Map();
    const requests = [];
    const confirmations = [];
    const dialogs = [];
    const controls = { prop(name, value) { this[name] = value; } };
    let reloads = 0;
    const editor = {
        value: '',
        getValue() { return this.value; },
        setValue(value) { this.value = value; },
        setOption(name, value) { this[name] = value; }
    };
    function $(selector) {
        if (typeof selector === 'function') return selector();
        if (typeof selector === 'object') return { attr: () => selector.version };
        if (selector === '#version_now') return { val: () => 'current source' };
        if (selector === '#version_1') return { val: () => 'historical source' };
        if (selector === '#glueRemark') return { val: () => 'test remark' };
        if (selector === '<span>') {
            return {
                text(value) { this.value = value; return this; },
                html() { return this.value.replaceAll('<', '&lt;').replaceAll('>', '&gt;'); }
            };
        }
        return {
            on(event, callback) { handlers.set(event, callback); },
            click(callback) { handlers.set(selector, callback); },
            modal() { return this; },
            find() { return controls; }
        };
    }
    $.ajax = request => requests.push(request);
    vm.runInNewContext(script, {
        $, base_url: '', document: { getElementById: () => ({}) },
        CodeMirror(_, options) { editor.setValue(options.value); return editor; },
        I18n: new Proxy({}, { get: (_, key) => key }),
        layer: {
            confirm(message, options, yes) { confirmations.push({ message, yes }); },
            close() {},
            alert(message) { dialogs.push({ content: message }); },
            open(options) { dialogs.push(options); }
        },
        window: { location: { reload() { reloads++; } } }
    });
    return {
        editor, controls, requests, confirmations, dialogs, reloads: () => reloads,
        choose(version) { handlers.get('.source_version').call({ version }); },
        save() { handlers.get('#saveModal .ok')(); },
        hide() { return handlers.get('hide.bs.modal')(); }
    };
}

test('切换历史版本前确认未保存的修改，历史版本可切回当前版本', () => {
    const ui = glueIde();
    ui.editor.setValue('unsaved edits');
    ui.choose('version_1');
    assert.equal(ui.confirmations[0].message, 'jobinfo_glue_discard_changes');
    assert.equal(ui.editor.getValue(), 'unsaved edits');
    ui.confirmations[0].yes(1);
    assert.equal(ui.editor.getValue(), 'historical source');
    ui.choose('version_now');
    assert.equal(ui.confirmations.length, 1);
    assert.equal(ui.editor.getValue(), 'current source');
});

test('GLUE 保存请求期间防止重复和切换，失败后保留源码并恢复编辑', () => {
    const ui = glueIde();
    ui.editor.setValue('unsaved edits');
    ui.save();
    ui.save();
    ui.choose('version_1');
    assert.equal(ui.requests.length, 1);
    assert.equal(ui.requests[0].data.glueSource, 'unsaved edits');
    assert.equal(ui.editor.readOnly, true);
    assert.equal(ui.controls.disabled, true);
    assert.equal(ui.hide(), false);
    ui.requests[0].error();
    ui.requests[0].complete();
    assert.equal(ui.dialogs.at(-1).content, 'system_save_unconfirmed');
    assert.equal(ui.editor.getValue(), 'unsaved edits');
    assert.equal(ui.editor.readOnly, false);
    assert.equal(ui.controls.disabled, false);
    ui.save();
    ui.requests[1].success({ code: 500, msg: '版本冲突 <请刷新>' });
    ui.requests[1].complete();
    assert.equal(ui.dialogs.at(-1).content, '版本冲突 &lt;请刷新&gt;');
    ui.save();
    ui.requests[2].success({ code: 200 });
    ui.requests[2].complete();
    assert.equal(ui.editor.readOnly, true);
    ui.dialogs.at(-1).end();
    assert.equal(ui.reloads(), 1);
});
