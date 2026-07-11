import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import ChartPanel from './ChartPanel.vue'
import DataTableFrame from './DataTableFrame.vue'
import EmptyState from './EmptyState.vue'
import FilterBar from './FilterBar.vue'
import PageContainer from './PageContainer.vue'
import PageHeader from './PageHeader.vue'
import StatTile from './StatTile.vue'

const globalOptions = {
  directives: {
    loading: {},
  },
  stubs: {
    ElIcon: {
      template: '<i class="icon-stub"><slot /></i>',
    },
    ElSkeleton: {
      template: '<div class="skeleton-stub" />',
    },
    ElTable: {
      props: ['data', 'rowKey', 'stripe'],
      template: '<div class="table-stub"><slot /><slot name="empty" /></div>',
    },
    ElPagination: {
      props: ['pageSize', 'currentPage', 'total', 'pageSizes', 'layout'],
      emits: ['current-change', 'size-change'],
      template: `
        <div class="pagination-stub">
          <button class="page-change" @click="$emit('current-change', 2)">page</button>
          <button class="page-size-change" @click="$emit('size-change', 50)">size</button>
        </div>
      `,
    },
  },
}

describe('common UI components', () => {
  it('renders PageHeader copy, metadata, and actions', () => {
    const wrapper = mount(PageHeader, {
      props: {
        title: '用户管理',
        description: '维护平台用户',
      },
      slots: {
        meta: '<span class="meta-content">最近更新</span>',
        actions: '<button class="header-action">新建</button>',
      },
    })

    expect(wrapper.get('h1').text()).toBe('用户管理')
    expect(wrapper.get('p').text()).toBe('维护平台用户')
    expect(wrapper.get('.meta-content').text()).toBe('最近更新')
    expect(wrapper.get('.header-action').text()).toBe('新建')
  })

  it('composes PageContainer with the shared header', () => {
    const wrapper = mount(PageContainer, {
      props: { title: '工作台' },
      slots: {
        default: '<div class="page-body">内容</div>',
        actions: '<button class="page-action">刷新</button>',
      },
    })

    expect(wrapper.findComponent(PageHeader).exists()).toBe(true)
    expect(wrapper.get('.page-body').text()).toBe('内容')
    expect(wrapper.get('.page-action').text()).toBe('刷新')
  })

  it('supports unframed and flush PageContainer content', async () => {
    const wrapper = mount(PageContainer, {
      props: { title: '工作台', framed: false },
      slots: { default: '<div>内容</div>' },
    })

    expect(wrapper.get('.page-content').classes()).toContain('page-content--unframed')

    await wrapper.setProps({ framed: true, flush: true })
    expect(wrapper.get('.page-content').classes()).not.toContain('page-content--unframed')
    expect(wrapper.get('.page-content').classes()).toContain('page-content--flush')
  })

  it('separates FilterBar fields, actions, and trailing content', () => {
    const wrapper = mount(FilterBar, {
      props: { label: '岗位筛选' },
      slots: {
        default: '<input class="filter-field">',
        actions: '<button class="filter-action">查询</button>',
        trailing: '<span class="filter-trailing">共 20 条</span>',
      },
    })

    expect(wrapper.attributes('role')).toBe('search')
    expect(wrapper.attributes('aria-label')).toBe('岗位筛选')
    expect(wrapper.get('.filter-field').exists()).toBe(true)
    expect(wrapper.get('.filter-action').text()).toBe('查询')
    expect(wrapper.get('.filter-trailing').text()).toBe('共 20 条')
  })

  it('renders EmptyState content and action', () => {
    const wrapper = mount(EmptyState, {
      props: {
        title: '暂无岗位',
        description: '调整筛选条件后重试',
      },
      slots: {
        actions: '<button class="empty-action">清除筛选</button>',
      },
      global: globalOptions,
    })

    expect(wrapper.text()).toContain('暂无岗位')
    expect(wrapper.text()).toContain('调整筛选条件后重试')
    expect(wrapper.get('.empty-action').text()).toBe('清除筛选')
  })

  it('provides DataTableFrame empty state and pagination events', async () => {
    const wrapper = mount(DataTableFrame, {
      props: {
        data: [],
        page: 1,
        pageSize: 20,
        total: 80,
        emptyTitle: '暂无用户',
      },
      global: globalOptions,
    })

    expect(wrapper.findComponent(EmptyState).text()).toContain('暂无用户')

    await wrapper.get('.page-change').trigger('click')
    await wrapper.get('.page-size-change').trigger('click')

    expect(wrapper.emitted('update:page')).toEqual([[2]])
    expect(wrapper.emitted('page-change')).toEqual([[2]])
    expect(wrapper.emitted('update:pageSize')).toEqual([[50]])
    expect(wrapper.emitted('page-size-change')).toEqual([[50]])
  })

  it('renders StatTile value, unit, and semantic tone', () => {
    const wrapper = mount(StatTile, {
      props: {
        label: '平均耗时',
        value: '18.6',
        unit: 'ms',
        tone: 'success',
      },
      global: globalOptions,
    })

    expect(wrapper.classes()).toContain('stat-tile--success')
    expect(wrapper.get('.stat-tile__label').text()).toBe('平均耗时')
    expect(wrapper.get('.stat-tile__value').text()).toBe('18.6ms')
  })

  it('keeps ChartPanel dimensions stable across empty and loading states', async () => {
    const wrapper = mount(ChartPanel, {
      props: {
        title: '岗位趋势',
        empty: true,
        minHeight: 360,
      },
      slots: {
        default: '<div class="chart-content">chart</div>',
      },
      global: globalOptions,
    })

    expect(wrapper.get('.chart-panel__body').attributes('style')).toContain('360px')
    expect(wrapper.findComponent(EmptyState).exists()).toBe(true)
    expect(wrapper.find('.chart-content').exists()).toBe(false)

    await wrapper.setProps({ empty: false, loading: true })
    expect(wrapper.find('.skeleton-stub').exists()).toBe(true)

    await wrapper.setProps({ loading: false })
    expect(wrapper.get('.chart-content').text()).toBe('chart')
  })
})
