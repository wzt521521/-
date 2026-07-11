# Common UI Components

These components own layout and UI state only. API requests, query objects, permissions, and domain formatting remain in the view that uses them.

## Component Catalog

| Component | Responsibility | Main inputs |
| --- | --- | --- |
| `PageHeader` | Page title, description, metadata, and actions | `title`, `description`, `bordered`; `title`, `description`, `meta`, `actions` slots |
| `PageContainer` | Standard page shell composed with `PageHeader` | `title`, `description`, `framed`, `flush`; `actions`, default slots |
| `FilterBar` | Responsive grouping for filter fields and commands | `label`, `compact`; default, `actions`, `trailing` slots |
| `DataTableFrame` | Table loading, empty state, and pagination | `data`, `loading`, `page`, `pageSize`, `total`, `pagination` |
| `StatTile` | Stable summary metric with semantic tone | `label`, `value`, `unit`, `icon`, `tone`, `loading` |
| `ChartPanel` | Stable chart surface with loading and empty states | `title`, `description`, `loading`, `empty`, `minHeight` |
| `EmptyState` | Shared neutral empty state and recovery action | `title`, `description`, `icon`, `compact` |

## Pagination Contract

`DataTableFrame` uses one-based UI pages. Views backed by zero-based APIs convert at the boundary:

```vue
<DataTableFrame
  :page="query.page + 1"
  :page-size="query.size"
  :total="total"
  @page-change="(value) => { query.page = value - 1; load() }"
  @page-size-change="(value) => { query.size = value; query.page = 0; load() }"
>
  <el-table-column prop="name" label="名称" />
</DataTableFrame>
```

Unknown attributes on `DataTableFrame` are forwarded to its `el-table`, so supported Element Plus table options such as `height`, `max-height`, and row events remain available.

## Chart Contract

`ChartPanel` does not initialize or dispose chart instances. A chart adapter placed in its default slot owns ECharts lifecycle and resize handling. Keep the panel `minHeight` stable while loading, empty, and rendered states switch.

## Usage Rules

- Use `FilterBar` instead of page-local flex wrappers for filters.
- Use `DataTableFrame` for primary page tables; small contextual tables inside dialogs may remain direct `el-table` instances.
- Use semantic `StatTile` tones. Do not select colors directly in a view.
- Use `EmptyState` actions for recovery commands such as clearing filters or creating the first record.
- Do not add API calls or permission checks to common components.
