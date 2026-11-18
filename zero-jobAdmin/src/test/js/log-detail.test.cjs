const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const { test } = require('node:test');

function openLog(handleCode = 0, logId = '20') {
    const template = fs.readFileSync(path.join(__dirname,
        '../../main/resources/templates/business/log.detail.ftl'), 'utf8');
    const script = template.match(/<script>\s*([\s\S]*?)<\/script>/)[1]
        .replace('${triggerCode}', '200').replace('${handleCode}', String(handleCode)).replace('${logId?c}', logId);
    const requests = [];
    const handlers = {};
    const content = [];
    let scheduled;
    let status;
    const window = {
        setTimeout(fn) { scheduled = fn; return 1; },
        clearTimeout() { scheduled = undefined; }
    };
    function $(target) {
        if (typeof target === 'function') { target(); return; }
        return {
            0: { appendChild(node) { content.push(node.text); } },
            on(event, fn) { handlers[event] = fn; return this; },
            toggleClass() { return this; },
            text(value) { status = value; },
            hide() {},
            prop() { return this; }
        };
    }
    $.ajax = options => {
        const request = {
            options,
            done(fn) { this.respond = value => { fn(value); this.complete(); }; return this; },
            fail(fn) { this.reject = reason => { fn({}, reason); this.complete(); }; return this; },
            always(fn) { this.complete = fn; return this; },
            abort() { this.reject('abort'); }
        };
        requests.push(request);
        return request;
    };
    vm.runInNewContext(script, {
        $, window, base_url: '', document: {
            createTextNode: text => ({ text }),
            scrollingElement: { scrollHeight: 100, scrollTop: 0, clientHeight: 100 }
        },
        I18n: new Proxy({}, { get: (_, key) => key })
    });
    return {
        requests, handlers, content,
        status: () => status,
        hasTimer: () => Boolean(scheduled),
        next() { const cb = scheduled; scheduled = undefined; cb(); },
        respond(data) { requests.at(-1).respond({ code: 200, data }); }
    };
}

test('长日志编号在轮询请求中保持原始字符串', () => {
    const id = '9007199254740993';
    const page = openLog(0, id);
    assert.equal(page.requests[0].options.data.logId, id);
    page.respond({ fromLineNum: 1, toLineNum: 2, logContent: 'first', end: false });
    page.next();
    assert.equal(page.requests[1].options.data.logId, id);
});

test('执行日志保留原始字符、缩进及结束时的诊断内容', () => {
    const page = openLog();
    assert.equal(page.requests[0].options.data.plainText, true);
    const raw = '  参数 <node> & <b>值</b> ###TAG_BR###\r\n    下一行\n';
    page.respond({ fromLineNum: 1, toLineNum: 2, logContent: raw, end: false });
    page.next();
    page.respond({ fromLineNum: 3, toLineNum: 0, logContent: '日志文件不存在', end: true });
    assert.deepEqual(page.content, [raw, '日志文件不存在']);
    assert.equal(page.hasTimer(), false);
});

test('无新增日志不会触发失败阈值，且每次只发送一个请求', () => {
    const page = openLog();
    for (let i = 0; i < 25; i++) {
        assert.equal(page.hasTimer(), false);
        page.respond({ fromLineNum: 1, toLineNum: 0, end: false });
        page.next();
    }
    assert.equal(page.requests.length, 26);
    assert.equal(page.status(), '');
});

test('已完成任务仍会读完所有分段，按行号接续', () => {
    const page = openLog(200);
    page.respond({ fromLineNum: 1, toLineNum: 2, logContent: 'first', end: false });
    page.next();
    assert.equal(page.requests.at(-1).options.data.fromLineNum, 3);
    page.respond({ fromLineNum: 3, toLineNum: 3, logContent: 'last', end: true });
    assert.deepEqual(page.content, ['first', 'last']);
    assert.equal(page.hasTimer(), false);
    assert.equal(page.status(), 'joblog_rolling_log_finished');
});

test('失败重复达到上限后退出，离开页面会停止请求', () => {
    const page = openLog();
    page.handlers['pagehide.jobLog']();
    assert.equal(page.hasTimer(), false);
    page.handlers['pageshow.jobLog']();
    for (let i = 1; i <= 20; i++) {
        page.requests.at(-1).reject('error');
        if (i < 20) { page.next(); }
    }
    assert.equal(page.status(), 'joblog_rolling_log_failoften');
    assert.equal(page.hasTimer(), false);
});
