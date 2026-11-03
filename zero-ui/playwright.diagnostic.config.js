import config from './playwright.config.js'
export default { ...config, testDir: './tests/diagnostics', workers: 1 }
