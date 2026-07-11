module.exports = {
  root: true,
  env: {
    browser: true,
    es2022: true,
    node: true,
  },
  extends: ['eslint:recommended', 'plugin:vue/vue3-recommended'],
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module',
  },
  rules: {
    'vue/multi-word-component-names': 'off',
    'vue/max-attributes-per-line': ['error', {
      singleline: 3,
      multiline: 1,
    }],
  },
  overrides: [
    {
      files: [
        'src/components/ChartPanel.vue',
        'src/views/collect/*.vue',
        'src/views/dashboard/*.vue',
        'src/views/positions/*.vue',
      ],
      rules: {
        'vue/attribute-hyphenation': 'off',
        'vue/attributes-order': 'off',
        'vue/max-attributes-per-line': 'off',
        'vue/singleline-html-element-content-newline': 'off',
      },
    },
  ],
}
