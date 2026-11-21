/**
 * 通知正文的内容格式识别、富文本白名单清洗和摘要提取；详情的中性色处理只影响展示，不改发送原文。
 */
import DOMPurify from 'dompurify'

// 与服务端通知白名单一致，旧TEXT内容不按HTML解释。
const tags = ['p','div','br','strong','b','em','i','u','s','blockquote','pre','code','ol','ul','li','span','h1','h2','h3','h4','h5','h6','a','img']
const classPattern = /^(?:ql-align-(?:center|right|justify)|ql-indent-[1-8]|ql-size-(?:small|large|huge)|ql-font-(?:serif|monospace))$/
const colorPattern = /^(?:#[0-9a-f]{3,8}|rgba?\([0-9.,% ]{1,60}\)|black|white|red|green|blue|yellow|orange|purple|gray|grey|transparent|inherit)$/i

function safeUrl(value) {
  if (!value || /[\\\u0000-\u0020\u007f]/.test(value)) return false
  if (value.startsWith('/') && !value.startsWith('//')) return true
  try {
    const url = new URL(value)
    return ['http:', 'https:'].includes(url.protocol) && !url.username && !url.password
  } catch { return false }
}

/** 将正文转换为安全详情 HTML、摘要文本和图片计数；TEXT 不按富文本解释。 */
export function noticeContent(content, format = 'TEXT') {
  if (typeof content !== 'string') return { text: '', plainText: '', html: '', hasImages: false, imageCount: 0 }
  if (format !== 'HTML') return { text: content, plainText: content, html: '', hasImages: false, imageCount: 0 }
  if (content.length > 20000 || typeof DOMPurify.sanitize !== 'function') return { text: '', plainText: '', html: '', hasImages: false, imageCount: 0 }
  const fragment = DOMPurify.sanitize(content, {
    ALLOWED_TAGS: tags,
    ALLOWED_ATTR: ['class','style','href','src','alt','title','data-list'],
    ALLOW_DATA_ATTR: false, ALLOW_ARIA_ATTR: false, RETURN_DOM_FRAGMENT: true
  })
  const wrapper = document.createElement('div')
  wrapper.append(fragment)
  for (const block of wrapper.querySelectorAll('div.ql-code-block-container')) {
    const pre = document.createElement('pre')
    const code = document.createElement('code')
    code.textContent = [...block.children].map(child => child.textContent).join('\n') || block.textContent
    pre.append(code)
    block.replaceWith(pre)
  }
  for (const node of wrapper.querySelectorAll('*')) {
    if (node.hasAttribute('class')) {
      const classes = [...node.classList].filter(value => classPattern.test(value))
      node.removeAttribute('class')
      if (classes.length) node.className = classes.join(' ')
    }
    if (node.hasAttribute('style')) {
      const styles = []
      for (const declaration of node.getAttribute('style').split(';')) {
        const colon = declaration.indexOf(':')
        if (colon < 0) continue
        const key = declaration.slice(0,colon).trim().toLowerCase()
        const value = declaration.slice(colon+1).trim()
        if (['color','background-color'].includes(key) && colorPattern.test(value)) styles.push(key+':'+value)
        if (key === 'text-align' && ['left','center','right','justify'].includes(value)) styles.push(key+':'+value)
      }
      node.removeAttribute('style')
      if (styles.length) node.setAttribute('style',styles.join(';'))
    }
    if (node.hasAttribute('data-list') && !['ordered','bullet'].includes(node.getAttribute('data-list'))) node.removeAttribute('data-list')
    if (node.tagName === 'IMG' && !safeUrl(node.getAttribute('src'))) node.remove()
    if (node.tagName === 'A') {
      if (!safeUrl(node.getAttribute('href'))) node.removeAttribute('href')
      node.setAttribute('target','_blank')
      node.setAttribute('rel','noopener noreferrer')
    }
  }
  // 保留段落间分隔供摘要阅读，不在顶部或右侧直接渲染HTML或加载图片。
  const textWrapper = wrapper.cloneNode(true)
  for (const image of textWrapper.querySelectorAll('img')) image.replaceWith(document.createTextNode(' [图片] '))
  for (const block of textWrapper.querySelectorAll('p,div,li,h1,h2,h3,h4,h5,h6,blockquote,pre,br')) block.append(document.createTextNode(' '))
  const plainWrapper = wrapper.cloneNode(true)
  for (const block of plainWrapper.querySelectorAll('p,div,li,h1,h2,h3,h4,h5,h6,blockquote,pre,br')) block.append(document.createTextNode(' '))
  const imageCount = wrapper.querySelectorAll('img[src]').length
  return { html: wrapper.innerHTML, text: textWrapper.textContent.replace(/\s+/g,' ').trim(),
    plainText: plainWrapper.textContent.replace(/\s+/g,' ').trim(), hasImages: imageCount > 0, imageCount }
}

// 详情去掉复制内容携带的中性前景色和底色，彩色强调仍保留；不修改发送原文或编辑器内容。
export function noticeDetailsHtml(html) {
  if (!html || typeof document === 'undefined') return ''
  const wrapper = document.createElement('div')
  wrapper.innerHTML = html
  const probe = document.createElement('span')
  const neutral = value => {
    probe.style.color = ''
    probe.style.color = value
    const normalized = probe.style.color
    if (['black', 'white', 'gray', 'grey', 'transparent', 'inherit'].includes(normalized)) return true
    const rgb = normalized.match(/^rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,.*)?\)$/)
    return !!rgb && rgb[1] === rgb[2] && rgb[2] === rgb[3]
  }
  for (const node of wrapper.querySelectorAll('[style]')) {
    for (const key of ['color', 'background-color']) {
      if (neutral(node.style.getPropertyValue(key))) node.style.removeProperty(key)
    }
    if (!node.style.cssText) node.removeAttribute('style')
  }
  return wrapper.innerHTML
}
