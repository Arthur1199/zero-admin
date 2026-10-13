import SvgIcon from '@/components/SvgIcon'
const sources = import.meta.glob('./svg/*.svg', { eager: true, query: '?raw', import: 'default' })
export default function registerIcons(app) {
  app.component('svg-icon', SvgIcon)
  const namespace = 'http://www.w3.org/2000/svg'
  const sprite = document.createElementNS(namespace, 'svg')
  sprite.setAttribute('aria-hidden', 'true')
  sprite.style.display = 'none'
  for (const [file, source] of Object.entries(sources)) {
    const svg = new DOMParser().parseFromString(source, 'image/svg+xml').documentElement
    const symbol = document.createElementNS(namespace, 'symbol')
    symbol.id = 'icon-' + file.split('/').pop().replace(/\.svg$/, '')
    symbol.setAttribute('viewBox', svg.getAttribute('viewBox') || '0 0 128 128')
    while (svg.firstChild) symbol.appendChild(svg.firstChild)
    sprite.appendChild(symbol)
  }
  document.body.appendChild(sprite)
}
