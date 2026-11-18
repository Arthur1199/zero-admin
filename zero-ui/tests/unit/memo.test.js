import { beforeEach, describe, expect, it, vi } from 'vitest'
import Memo from '@/views/systemTools/memo/index.vue'
import { listMemo, getMemo, addMemo, updateMemo, delMemo } from '@/api/systemTools/memo'

vi.mock('@/api/systemTools/memo', () => ({
  listMemo: vi.fn(), getMemo: vi.fn(), addMemo: vi.fn(), updateMemo: vi.fn(), delMemo: vi.fn()
}))

const deferred = () => {
  let resolve
  const promise = new Promise(done => { resolve = done })
  return { promise, resolve }
}

function page() {
  const vm = {
    ...Memo.data(),
    $refs: { form: { validate: vi.fn().mockResolvedValue(true) } },
    $modal: { confirm: vi.fn().mockResolvedValue(), msgSuccess: vi.fn(), msgError: vi.fn() }
  }
  for (const [name, method] of Object.entries(Memo.methods)) vm[name] = method.bind(vm)
  return vm
}

beforeEach(() => {
  vi.resetAllMocks()
  listMemo.mockResolvedValue({ rows: [], total: 0 })
})

describe('备忘录异步请求与敏感表单生命周期', () => {
  it('离开后丢弃迟到的详情，重新激活只刷新元数据', async () => {
    const vm = page(), pending = deferred()
    getMemo.mockReturnValue(pending.promise)
    const task = vm.handleOpen({ memoId: 7 }, true)
    Memo.deactivated.call(vm)
    Memo.activated.call(vm)
    pending.resolve({ data: { memoId: 7, memoContent: 'test-secret', canManage: true } })
    await task
    expect(vm.open).toBe(false)
    expect(vm.form.memoContent).toBe('')
    expect(vm.detailLoading).toBe(false)
    expect(listMemo).toHaveBeenCalledTimes(1)
  })

  it('离开页面后旧列表响应不回写，查询参数使用独立快照', async () => {
    const vm = page(), pending = deferred()
    vm.queryParams.title = 'original'
    listMemo.mockReturnValue(pending.promise)
    const task = vm.getList()
    vm.queryParams.title = 'changed'
    expect(listMemo.mock.calls[0][0].title).toBe('original')
    vm.releaseContent()
    pending.resolve({ rows: [{ title: 'stale' }], total: 1 })
    await task
    expect(vm.memoList).toEqual([])
    expect(vm.loading).toBe(false)
  })

  it('旧关闭动画不会清空重新打开的表单，真正关闭后清空正文', () => {
    const vm = page()
    vm.open = true
    vm.form.memoContent = 'new-draft'
    vm.handleDialogClosed()
    expect(vm.form.memoContent).toBe('new-draft')
    vm.open = false
    vm.handleDialogClosed()
    expect(vm.form.memoContent).toBe('')
  })

  it('保存期间重复点击不重复提交，离开后完成不操作旧页面', async () => {
    const vm = page(), pending = deferred()
    vm.open = true
    vm.form.title = 'test'
    vm.form.memoContent = 'test-secret'
    addMemo.mockReturnValue(pending.promise)
    const task = vm.submitForm()
    await vi.waitFor(() => expect(addMemo).toHaveBeenCalledTimes(1))
    await vm.submitForm()
    vm.releaseContent()
    pending.resolve({ code: 200 })
    await task
    expect(addMemo).toHaveBeenCalledTimes(1)
    expect(vm.$modal.msgSuccess).not.toHaveBeenCalled()
    expect(listMemo).not.toHaveBeenCalled()
    expect(vm.form.memoContent).toBe('')
    expect(vm.submitting).toBe(false)
  })

  it('公开保存确认期间离开页面，即使后来确认也不提交', async () => {
    const vm = page(), confirmation = deferred()
    vm.open = true
    vm.form.visibility = '1'
    vm.$modal.confirm.mockReturnValue(confirmation.promise)
    const task = vm.submitForm()
    await vi.waitFor(() => expect(vm.$modal.confirm).toHaveBeenCalledTimes(1))
    vm.releaseContent()
    confirmation.resolve()
    await task
    expect(addMemo).not.toHaveBeenCalled()
    expect(updateMemo).not.toHaveBeenCalled()
  })

  it('删除确认期间离开页面，不再发出删除请求', async () => {
    const vm = page(), confirmation = deferred()
    vm.$modal.confirm.mockReturnValue(confirmation.promise)
    const task = vm.handleDelete({ memoId: 7, version: 2, canManage: true })
    vm.releaseContent()
    confirmation.resolve()
    await task
    expect(delMemo).not.toHaveBeenCalled()
    expect(vm.deleting).toBe(false)
  })

  it('详情ID不匹配时拒绝开窗，普通账号不能触发他人记录修改', async () => {
    const vm = page()
    await vm.handleOpen({ memoId: 7, canManage: false }, false)
    expect(getMemo).not.toHaveBeenCalled()
    getMemo.mockResolvedValue({ data: { memoId: 8, memoContent: 'wrong-record' } })
    await vm.handleOpen({ memoId: 7 }, true)
    expect(vm.open).toBe(false)
    expect(vm.form.memoContent).toBe('')
    expect(vm.$modal.msgError).toHaveBeenCalledTimes(1)
  })
})
