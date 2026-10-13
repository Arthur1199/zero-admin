const sources = import.meta.glob('../../assets/icons/svg/*.svg')
export default Object.keys(sources).map(file => file.split('/').pop().replace(/\.svg$/, ''))
