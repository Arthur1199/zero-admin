import { describe, it, expect } from 'vitest'
import { parse, compileTemplate, compileScript } from '@vue/compiler-sfc'
import { mkdirSync, writeFileSync } from 'node:fs'
import { resolvePath } from '@/utils/path'
import { normalizeSize, resolveIcon } from '@/utils/icons'
import { normalizeField } from '@/utils/generator/normalize'
import { timeRangeProps } from '@/utils/generator/timeRange'
import { makeUpHtml, vueTemplate, vueScript, cssStyle } from '@/utils/generator/html'
import { makeUpJs } from '@/utils/generator/js'
import { makeUpCss } from '@/utils/generator/css'
import { formConf, inputComponents, selectComponents } from '@/utils/generator/config'

describe('Vue3迁移边界', () => {
  it('旧时间选择范围保持包含边界', () => {
    const props = timeRangeProps('09:30:15-18:45:30')
    expect(props.disabledHours()).toContain(8)
    expect(props.disabledHours()).not.toContain(9)
    expect(props.disabledMinutes(9)).toContain(29)
    expect(props.disabledSeconds(9, 30)).toContain(14)
    expect(props.disabledSeconds(9, 30)).not.toContain(15)
    expect(props.disabledSeconds(18, 45)).not.toContain(30)
    expect(props.disabledSeconds(18, 45)).toContain(31)
  })
  it('旧尺寸与图标配置转换，业务值不变', () => {
    expect(['default', 'medium', 'small', 'mini'].map(normalizeSize)).toEqual(['default', 'default', 'small', 'small'])
    const field = { size: 'mini', defaultValue: ['1', 2], format: 'yyyy-MM-dd', icon: 'el-icon-search' }
    const normalized = normalizeField(field)
    expect(normalized.size).toBe('small')
    expect(normalized.format).toBe('YYYY-MM-DD')
    expect(normalized.defaultValue).toEqual(['1', 2])
    expect(normalized.icon).toBe(resolveIcon('el-icon-search'))
    expect(field.format).toBe('yyyy-MM-dd')
  })
  it('路由合并支持绝对、相对和多级路径', () => {
    expect(resolvePath('/system', 'user')).toBe('/system/user')
    expect(resolvePath('/system', '/reportSystem', 'bizDict')).toBe('/reportSystem/bizDict')
    expect(resolvePath('/system/user', '../role')).toBe('/system/role')
  })
  for (const type of ['file', 'dialog']) it('设计器' + type + '导出能编译为Vue3组件', () => {
    const fields = JSON.parse(JSON.stringify([...inputComponents, ...selectComponents])).map((field, index) => ({ ...field, formId: index + 1, renderKey: index + 1, layout: 'colFormItem', vModel: 'field' + index }))
    fields[0]['prefix-icon'] = 'el-icon-mobile'
    fields[0]['suffix-icon'] = 'el-icon-search'
    const conf = { ...formConf, fields }
    const source = vueTemplate(makeUpHtml(conf, type)) + vueScript(makeUpJs(conf, type)) + cssStyle(makeUpCss(conf))
    const { descriptor, errors } = parse(source)
    expect(errors).toEqual([])
    expect(compileTemplate({ source: descriptor.template.content, id: type, filename: type + '.vue' }).errors).toEqual([])
    expect(() => compileScript(descriptor, { id: type })).not.toThrow()
    expect(source).not.toMatch(/\.sync|\$listeners|slot=|slot-scope/)
    expect(source).toContain(':prefix-icon="resolveIcon(\'el-icon-mobile\')"')
    expect(source).toContain(':suffix-icon="resolveIcon(\'el-icon-search\')"')
    expect(source).toContain('"mobile":"Iphone"')
    mkdirSync('output/playwright/generated', { recursive: true })
    writeFileSync('output/playwright/generated/' + type + '.vue', source)
  })
})
