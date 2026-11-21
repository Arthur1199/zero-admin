import { WebSocketMessageType } from '../../src/utils/websocketMessageType'

export const groups = [
  { path: '/system', label: '系统管理', children: ['user', 'role', 'menu', 'dept', 'post', 'dict', 'config', 'notice', 'apiAuth', 'google', 'ipWhite'] },
  { path: '/monitor', label: '系统监控', children: ['operlog', 'logininfor', 'online', 'job', 'server', 'cache', 'cache/list', 'druid'] },
  { path: '/tool', label: '系统工具', children: ['gen', 'build', 'swagger'] },
  { path: '/reportSystem', label: '业务管理', children: ['bizDict'] },
  { path: '/systemTools', label: '文件工具', children: ['fileManagement'] }
]
export const paths = groups.flatMap(group => group.children.map(child => group.path + '/' + child))

export function mockAdminWebSocket(page) {
  return page.routeWebSocket('**/ws/admin', socket => {
    socket.onMessage(() => socket.send(JSON.stringify({
      msgType: { messageType: WebSocketMessageType.CREATE_CONNECTION },
      msgData: { heartbeatInterval: 30000, idleTimeout: 60000 }
    })))
  })
}

export async function mockApi(page, options = {}) {
  const requests = []
  await page.route('**/dev-api/**', async route => {
    const request = route.request(), url = new URL(request.url()), pathname = url.pathname.replace('/dev-api', '')
    requests.push({ path: pathname, method: request.method(), body: request.postData(), headers: request.headers() })
    let result = { code: 200, msg: '操作成功', data: {}, rows: [], total: 0 }
    if (pathname === '/captchaImage') result = { code: 200, captchaEnabled: false, googleAuthEnabled: false }
    else if (pathname === '/login') result = { code: 200, token: 'mock-token' }
    else if (pathname === '/getInfo') result = { code: 200, user: { userId: 1, userName: 'test-admin', nickName: '测试管理员', avatar: '', dept: { deptName: '测试部门' } }, roles: options.roles || ['admin'], permissions: options.permissions || ['*:*:*'], isDefaultModifyPwd: options.passwordReset || false }
    else if (pathname === '/getRouters') result.data = groups.map((group, index) => ({ path: group.path, component: 'Layout', menuId: index + 100, parentId: 0, alwaysShow: true, meta: { title: group.label, icon: 'system' }, children: group.children.map((child, childIndex) => ({ path: child, name: child === 'cache/list' ? 'CacheList' : child[0].toUpperCase() + child.slice(1), component: group.path.slice(1) + '/' + child + (child === 'cache/list' ? '' : '/index'), menuId: (index + 1) * 1000 + childIndex, parentId: index + 100, meta: { title: child, icon: 'list' } })) }))
    else if (pathname === '/system/apiAuth/googleAuthApis') result.data = { isNeedGoogleAuth: Boolean(options.googleApis), authApis: options.googleApis || [] }
    else if (pathname === '/system/role/nextSort') result.data = 3
    else if (pathname.includes('/dict/data/type/')) result.data = [{ dictLabel: '正常', dictValue: '0', listClass: 'success', cssClass: '' }, { dictLabel: '停用', dictValue: '1', listClass: 'danger', cssClass: '' }]
    else if (/^\/system\/dict\/type\/\d+$/.test(pathname)) result.data = { dictId: Number(pathname.split('/').at(-1)), dictName: '测试字典', dictType: 'test_dictionary' }
    else if (pathname === '/system/dict/type/optionselect') result.data = [{ dictId: 1, dictName: '测试字典', dictType: 'test_dictionary' }]
    else if (pathname.includes('/deptTree') || pathname.includes('/treeselect') || pathname === '/system/dept/list' || pathname === '/system/menu/list') result.data = [{ id: 100, label: '测试部门', deptId: 100, deptName: '测试部门', menuId: 100, menuName: '测试菜单', children: [{ id: 101, label: '子部门', deptId: 101, deptName: '子部门', menuId: 101, menuName: '子菜单' }] }]
    else if (pathname === '/system/user/profile') { result.data = { userId: 1, userName: 'test-admin', nickName: '测试用户', dept: { deptName: '测试部门' } }; result.roleGroup = '管理员'; result.postGroup = '测试岗位' }
    else if (pathname === '/system/user/') { result.data = {}; result.posts = []; result.roles = [] }
    else if (pathname.includes('/upload/config')) result.data = { maxFileSize: 31457280, maxFileSizeText: '30MB' }
    else if (pathname === '/monitor/cache') result.data = { info: { redis_version: '7.0', used_memory_human: '1M', uptime_in_days: '1', connected_clients: '1' }, dbSize: 0, commandStats: [{ name: 'get', value: 1 }] }
    else if (pathname === '/monitor/cache/getNames') result.data = []
    else if (pathname === '/monitor/server') result.data = { cpu: {}, mem: {}, jvm: {}, sys: {}, sysFiles: [] }
    else if (pathname.includes('/group/options')) result.data = []
    else if (pathname.includes('/config/configKey/')) result.data = 'false'
    else if (pathname === '/common/upload' || pathname.endsWith('/upload')) result = { code: 200, url: '/mock.png', fileName: '/mock.png', newFileName: 'mock.png', originalFilename: 'mock.png', data: { url: '/mock.png' } }
    else if (pathname === '/system/user/profile/avatar') result = { code: 200, imgUrl: '/mock.png' }
    if (pathname === '/getRouters' && options.menus) result.data = options.menus
    return route.fulfill({ json: result })
  })
  return requests
}
export async function authenticated(page, options = {}) {
  const requests = await mockApi(page, options)
  await page.context().addCookies([{ name: 'Admin-Token', value: 'mock-token', url: 'http://127.0.0.1:5178' }])
  return requests
}
