import { readdirSync, readFileSync, existsSync, statSync } from 'node:fs'
import { resolve, dirname, relative, sep } from 'node:path'
import { parse, compileTemplate, compileScript } from '@vue/compiler-sfc'
import { parse as parseJs } from '@babel/parser'
const files = []
function walk(dir) { for (const entry of readdirSync(dir, { withFileTypes: true })) { const file = resolve(dir, entry.name); if (entry.isDirectory()) walk(file); else if (/\.(vue|jsx?)$/.test(file)) files.push(file) } }
walk('src')
let failed = false
for (const file of files) {
  const source = readFileSync(file, 'utf8')
  const errors = []
  // 模板能编译不代表运行时兼容，Vue3已移除的属性访问须提前阻止。
  if (/\$options\s*\.\s*propsData\b/.test(source)) errors.push('Vue2运行时残留：$options.propsData，请直接读取已声明的props')
  if (file.endsWith('.vue')) {
    const parsed = parse(source, { filename: file }), descriptor = parsed.descriptor
    errors.push(...parsed.errors)
    if (descriptor.template) errors.push(...compileTemplate({ source: descriptor.template.content, filename: file, id: file }).errors)
    if (descriptor.script || descriptor.scriptSetup) { try { compileScript(descriptor, { id: file, babelParserPlugins: ['jsx'] }) } catch (error) { errors.push(error) } }
  } else { try { parseJs(source, { sourceType: 'module', plugins: ['jsx'] }) } catch (error) { errors.push(error) } }
  // Windows构建可能掩盖大小写错误，提前按Linux文件名检查所有静态本地导入。
  for (const [, , request] of source.matchAll(/\b(?:from|import)\s*(?:\(\s*)?(['"])([^'"]+)\1/g)) {
    if ((!request.startsWith('@/') && !request.startsWith('.')) || /[$*]/.test(request)) continue
    const target = request.startsWith('@/') ? resolve('src', request.slice(2)) : resolve(dirname(file), request)
    const candidates = [target, ...['.js', '.jsx', '.vue', '.json', '/index.js', '/index.vue'].map(extension => target + extension)]
    const found = candidates.find(candidate => existsSync(candidate) && statSync(candidate).isFile())
    if (!found) { errors.push('本地导入不存在：' + request); continue }
    let current = resolve('src')
    for (const part of relative(current, found).split(sep)) {
      if (!readdirSync(current).includes(part)) { errors.push('导入大小写不一致：' + request); break }
      current = resolve(current, part)
    }
  }
  if (errors.length) { failed = true; console.error(file, errors.map(error => error.message || error)) }
}
if (failed) process.exitCode = 1
else console.log(`Vue3编译及导入检查通过：${files.filter(file => file.endsWith('.vue')).length}个组件、${files.length}个源码文件`)
