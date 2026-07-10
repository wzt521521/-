import { init, registerMap, use } from 'echarts/core'
import { BarChart, LineChart, MapChart, PieChart, RadarChart, TreemapChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent, VisualMapComponent } from 'echarts/components'
import { LegacyGridContainLabel } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'

use([
  BarChart, LineChart, MapChart, PieChart, RadarChart, TreemapChart,
  GridComponent, LegendComponent, TooltipComponent, VisualMapComponent,
  LegacyGridContainLabel,
  CanvasRenderer
])

export { init, registerMap }
