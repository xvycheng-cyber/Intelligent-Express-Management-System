<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import PracticeNav from '@/components/basics/PracticeNav.vue'
import PracticeStatus from '@/components/basics/PracticeStatus.vue'
import InlineDemo from '@/components/basics/InlineDemo.vue'
import BlockDemo from '@/components/basics/BlockDemo.vue'
import InlineBlockDemo from '@/components/basics/InlineBlockDemo.vue'
import FlexDemo from '@/components/basics/FlexDemo.vue'
import GridDemo from '@/components/basics/GridDemo.vue'
import BoxModelDemo from '@/components/basics/BoxModelDemo.vue'
import BoxSizingDemo from '@/components/basics/BoxSizingDemo.vue'
import NormalFlowDemo from '@/components/basics/NormalFlowDemo.vue'
import RelativePositionDemo from '@/components/basics/RelativePositionDemo.vue'
import AbsolutePositionDemo from '@/components/basics/AbsolutePositionDemo.vue'
import PositioningContextDemo from '@/components/basics/PositioningContextDemo.vue'
import FixedPositionDemo from '@/components/basics/FixedPositionDemo.vue'
import StickyPositionDemo from '@/components/basics/StickyPositionDemo.vue'
import StickyConstraintDemo from '@/components/basics/StickyConstraintDemo.vue'
import OffsetPropertiesDemo from '@/components/basics/OffsetPropertiesDemo.vue'
import OffsetConflictDemo from '@/components/basics/OffsetConflictDemo.vue'
import StackingContextDemo from '@/components/basics/StackingContextDemo.vue'
import LayoutPrinciplesDemo from '@/components/basics/LayoutPrinciplesDemo.vue'
import type { PracticeChapter, PracticeGroup } from '@/types/practice'

const displayChapters: PracticeChapter[] = [
  { number: '2.1', title: '行内显示类型', status: 'ready', to: '/web-basics#chapter-2.1' },
  { number: '2.2', title: '区块显示类型', status: 'ready', to: '/web-basics#chapter-2.2' },
  { number: '2.3', title: '行内区块显示类型', status: 'ready', to: '/web-basics#chapter-2.3' },
  { number: '2.4', title: '弹性显示类型', status: 'ready', to: '/web-basics#chapter-2.4' },
  { number: '2.5', title: '网格显示类型', status: 'ready', to: '/web-basics#chapter-2.5' }
]

const boxChapters: PracticeChapter[] = [
  { number: '2.6', title: '盒模型各部分', status: 'ready', to: '/web-basics#chapter-2.6' },
  { number: '2.7', title: '标准盒模型', status: 'ready', to: '/web-basics#chapter-2.7' },
  { number: '2.8', title: '替代盒模型', status: 'ready', to: '/web-basics#chapter-2.8' }
]

const positioningChapters: PracticeChapter[] = [
  { number: '2.9', title: '常规流布局（静态定位）', status: 'ready', to: '/web-basics#chapter-2.9' },
  { number: '2.10', title: '相对定位', status: 'ready', to: '/web-basics#chapter-2.10' },
  { number: '2.11', title: '绝对定位', status: 'ready', to: '/web-basics#chapter-2.11' },
  { number: '2.12', title: '改变绝对定位的定位上下文', status: 'ready', to: '/web-basics#chapter-2.12' },
  { number: '2.13', title: '固定定位', status: 'ready', to: '/web-basics#chapter-2.13' },
  { number: '2.14', title: '粘性定位', status: 'ready', to: '/web-basics#chapter-2.14' },
  { number: '2.15', title: '粘性约束矩形', status: 'ready', to: '/web-basics#chapter-2.15' },
  { number: '2.16', title: '定位属性上下左右', status: 'ready', to: '/web-basics#chapter-2.16' },
  { number: '2.17', title: '定位属性之间的覆盖影响', status: 'ready', to: '/web-basics#chapter-2.17' }
]

const groups: PracticeGroup[] = [
  { id: 'display', label: '显示类型', range: '2.1 — 2.5', chapters: displayChapters },
  { id: 'box', label: '盒模型', range: '2.6 — 2.8', chapters: boxChapters },
  { id: 'positioning', label: '定位布局', range: '2.9 — 2.17', chapters: positioningChapters },
  {
    id: 'wireframe',
    label: '系统线框',
    range: '2.18',
    chapters: [
      {
        number: '2.18',
        title: '使用线框风格仿制XXX网页的布局',
        status: 'ready',
        note: '项目替代：快递通系统原创线框',
        to: '/web-basics/wireframe'
      }
    ]
  },
  {
    id: 'principles',
    label: '布局原则',
    range: '2.19',
    chapters: [{ number: '2.19', title: '布局原则思考', status: 'ready', to: '/web-basics#chapter-2.19' }]
  }
]

const totalChapters = computed(() => groups.reduce((total, group) => total + group.chapters.length, 0))
const implementedCount = computed(
  () => groups.flatMap((group) => group.chapters).filter((chapter) => chapter.status === 'ready').length
)
const skeletonCount = computed(
  () => groups.flatMap((group) => group.chapters).filter((chapter) => chapter.status === 'skeleton').length
)
const notStartedCount = computed(
  () => groups.flatMap((group) => group.chapters).filter((chapter) => chapter.status === 'not-started').length
)
</script>

<template>
  <div class="wb-page" data-testid="web-basics-page">
    <header class="wb-page__header">
      <div class="wb-page__heading">
        <span class="wb-kicker">MS-06 · 课程实践</span>
        <h1>Web 基础实践</h1>
        <p>
          按课程任务书 2.1～2.19 组织的 CSS 布局实践目录。2.1～2.17、2.19 已提供真实可交互演示；
          2.18 已完成原创线框设计，页面会如实标注各区域状态。
        </p>
      </div>
      <div class="wb-page__actions">
        <RouterLink class="wb-button wb-button--ghost" to="/import">返回系统</RouterLink>
        <RouterLink class="wb-button wb-button--primary" to="/web-basics/wireframe">
          进入 2.18 线框页
        </RouterLink>
      </div>
    </header>

    <div class="wb-summary" aria-label="实践完成概况">
      <div class="wb-summary__item">
        <strong>{{ totalChapters }}</strong>
        <span>课程小节</span>
      </div>
      <div class="wb-summary__item">
        <strong>{{ implementedCount }}</strong>
        <span>已实现</span>
      </div>
      <div class="wb-summary__item">
        <strong>{{ skeletonCount }}</strong>
        <span>骨架已建立</span>
      </div>
      <div class="wb-summary__item">
        <strong>{{ notStartedCount }}</strong>
        <span>未开始</span>
      </div>
    </div>

    <div class="wb-notice" role="status">
      <strong>当前实现范围：</strong>
      2.1～2.17、2.19 的演示已开放；2.18 的两张真实截图与 Plan/Implemented 页面已完成。
      页面上的状态都是真实状态，不用静态文字冒充演示。
    </div>

    <div class="wb-layout">
      <PracticeNav :groups="groups" />

      <div class="wb-content">
        <section
          v-for="group in groups"
          :id="`wb-section-${group.id}`"
          :key="group.id"
          class="wb-group"
        >
          <div class="wb-group__head">
            <div>
              <span class="wb-kicker">{{ group.range }}</span>
              <h2>{{ group.label }}</h2>
            </div>
            <span class="wb-group__count">{{ group.chapters.length }} 节</span>
          </div>

          <div class="wb-chapter-grid">
            <template v-for="chapter in group.chapters" :key="chapter.number">
              <RouterLink
                v-if="chapter.to"
                class="wb-chapter wb-chapter--link"
                :to="chapter.to"
                :data-testid="`chapter-${chapter.number}`"
              >
                <div class="wb-chapter__top">
                  <span class="wb-chapter__number">{{ chapter.number }}</span>
                  <PracticeStatus :status="chapter.status" />
                </div>
                <h3>{{ chapter.title }}</h3>
                <p v-if="chapter.note">{{ chapter.note }}</p>
                <p v-if="chapter.number === '2.18'">原创线框 Plan / Implemented 两种模式，已生成两张真实截图。</p>
                <p v-else-if="chapter.status === 'ready'">真实可交互演示，含参数控制、观察结论和当前 CSS。</p>
                <p v-else>进入本系统原创线框页面的 plan / implemented 两种模式骨架。</p>
                <span class="wb-chapter__link">
                  {{ chapter.number === '2.18' ? '打开线框页' : chapter.status === 'ready' ? '打开本节演示' : '打开线框骨架' }} →
                </span>
              </RouterLink>

              <div
                v-else
                class="wb-chapter is-disabled"
                :data-testid="`chapter-${chapter.number}`"
                aria-disabled="true"
              >
                <div class="wb-chapter__top">
                  <span class="wb-chapter__number">{{ chapter.number }}</span>
                  <PracticeStatus :status="chapter.status" />
                </div>
                <h3>{{ chapter.title }}</h3>
                <p>演示尚未实现，将在对应阶段开放。</p>
              </div>
            </template>
          </div>

          <div v-if="group.id === 'display'" class="wb-demos">
            <div id="chapter-2.1" class="wb-demo-anchor"><InlineDemo /></div>
            <div id="chapter-2.2" class="wb-demo-anchor"><BlockDemo /></div>
            <div id="chapter-2.3" class="wb-demo-anchor"><InlineBlockDemo /></div>
            <div id="chapter-2.4" class="wb-demo-anchor"><FlexDemo /></div>
            <div id="chapter-2.5" class="wb-demo-anchor"><GridDemo /></div>
          </div>

          <div v-if="group.id === 'box'" class="wb-demos">
            <div id="chapter-2.6" class="wb-demo-anchor"><BoxModelDemo /></div>
            <div id="chapter-2.7" class="wb-demo-anchor"><BoxSizingDemo mode="content-box" /></div>
            <div id="chapter-2.8" class="wb-demo-anchor"><BoxSizingDemo mode="border-box" /></div>
          </div>

          <div v-if="group.id === 'positioning'" class="wb-demos">
            <div id="chapter-2.9" class="wb-demo-anchor"><NormalFlowDemo /></div>
            <div id="chapter-2.10" class="wb-demo-anchor"><RelativePositionDemo /></div>
            <div id="chapter-2.11" class="wb-demo-anchor"><AbsolutePositionDemo /></div>
            <div id="chapter-2.12" class="wb-demo-anchor"><PositioningContextDemo /></div>
            <div id="chapter-2.13" class="wb-demo-anchor"><FixedPositionDemo /></div>
            <div id="chapter-2.14" class="wb-demo-anchor"><StickyPositionDemo /></div>
            <div id="chapter-2.15" class="wb-demo-anchor"><StickyConstraintDemo /></div>
            <div id="chapter-2.16" class="wb-demo-anchor"><OffsetPropertiesDemo /></div>
            <div id="chapter-2.17" class="wb-demo-anchor"><OffsetConflictDemo /></div>
          </div>

          <div v-if="group.id === 'positioning'" id="stacking-supplement" class="wb-demos" data-testid="stacking-supplement">
            <StackingContextDemo />
          </div>

          <div v-if="group.id === 'principles'" class="wb-demos">
            <div id="chapter-2.19" class="wb-demo-anchor"><LayoutPrinciplesDemo /></div>
          </div>

          <section v-if="group.id === 'positioning'" class="wb-position-compare" data-testid="positioning-comparison">
            <div class="wb-group__head">
              <div>
                <span class="wb-kicker">补充对比</span>
                <h2>五种定位方式综合对比</h2>
              </div>
            </div>
            <div class="wb-compare-wrap">
              <table class="wb-compare-table">
                <thead>
                  <tr>
                    <th scope="col">方式</th>
                    <th scope="col">脱离正常流</th>
                    <th scope="col">保留原占位</th>
                    <th scope="col">定位参照</th>
                    <th scope="col">滚动行为</th>
                    <th scope="col">典型场景</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <th scope="row">static</th>
                    <td>否</td>
                    <td>保留</td>
                    <td>常规流，无定位参照</td>
                    <td>随页面/容器滚动</td>
                    <td>普通文档流</td>
                  </tr>
                  <tr>
                    <th scope="row">relative</th>
                    <td>否</td>
                    <td>保留</td>
                    <td>自身原位置</td>
                    <td>随页面/容器滚动</td>
                    <td>微调、层级控制</td>
                  </tr>
                  <tr>
                    <th scope="row">absolute</th>
                    <td>是</td>
                    <td>不保留</td>
                    <td>最近定位祖先；无则初始包含块</td>
                    <td>随包含块滚动</td>
                    <td>浮层、角标、弹出层</td>
                  </tr>
                  <tr>
                    <th scope="row">fixed</th>
                    <td>是</td>
                    <td>不保留</td>
                    <td>视口；transform 祖先等会改变</td>
                    <td>相对视口固定</td>
                    <td>固定工具栏、全局浮层</td>
                  </tr>
                  <tr>
                    <th scope="row">sticky</th>
                    <td>否</td>
                    <td>保留</td>
                    <td>最近滚动容器 + 阈值</td>
                    <td>阈值前随流，阈值后吸附，受父容器限制</td>
                    <td>表头、目录、分组标题</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <p class="wb-demo-note">
              2.15 的课程标题仍是“粘性约束矩形”；本表是五种定位方式的补充综合对比，不替代 2.15 的粘性约束实验。
              fixed 的“视口”参照在祖先建立 transform 包含块时可能改变，需按实际包含块判断。
            </p>
          </section>

          <section v-if="group.id === 'display'" class="wb-comparison" data-testid="display-comparison">
            <div class="wb-group__head">
              <div>
                <span class="wb-kicker">对比总结</span>
                <h2>五种显示类型怎么选</h2>
              </div>
            </div>
            <div class="wb-compare-wrap">
              <table class="wb-compare-table">
                <thead>
                  <tr>
                    <th scope="col">对比维度</th>
                    <th scope="col">inline</th>
                    <th scope="col">block</th>
                    <th scope="col">inline-block</th>
                    <th scope="col">flex</th>
                    <th scope="col">grid</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <th scope="row">是否独占一行</th>
                    <td>否，随文字行内排列</td>
                    <td>是（普通常规流）</td>
                    <td>否，随行内内容排列</td>
                    <td>是；<code>display:flex</code> 外部为块级</td>
                    <td>是；<code>display:grid</code> 外部为块级</td>
                  </tr>
                  <tr>
                    <th scope="row">是否支持设置宽高</th>
                    <td>普通非替换行内元素：否</td>
                    <td>是</td>
                    <td>是</td>
                    <td>容器是；项目由 flex 规则决定</td>
                    <td>容器是；项目由 grid 轨道决定</td>
                  </tr>
                  <tr>
                    <th scope="row">默认排列方式</th>
                    <td>文字行内流</td>
                    <td>从上到下块级流</td>
                    <td>文字行内流中的原子行内盒</td>
                    <td>沿主轴一维排列</td>
                    <td>按行列二维排列</td>
                  </tr>
                  <tr>
                    <th scope="row">是否适合一维布局</th>
                    <td>仅适合行内文字片段</td>
                    <td>有限，常规流堆叠</td>
                    <td>有限，适合行内盒子</td>
                    <td><strong>适合</strong></td>
                    <td>可以，但不是主要用途</td>
                  </tr>
                  <tr>
                    <th scope="row">是否适合二维布局</th>
                    <td>否</td>
                    <td>否</td>
                    <td>否</td>
                    <td>不是主要用途</td>
                    <td><strong>适合</strong></td>
                  </tr>
                  <tr>
                    <th scope="row">典型用途</th>
                    <td>文字强调、链接、行内图标</td>
                    <td>页面区块、段落容器</td>
                    <td>按钮/标签/卡片式行内块</td>
                    <td>导航、工具栏、一维分布</td>
                    <td>仪表盘、卡片网格、二维排版</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <p class="wb-demo-note">
              注意：<code>flex</code> 和 <code>grid</code> 首先是布局容器模型，不是“更高级的 inline/block”。
              上表的“独占一行”指默认块级外部显示（<code>display:flex</code>/<code>grid</code>）；若使用
              <code>inline-flex</code>/<code>inline-grid</code>，外部行为会不同，不在本节误导性简化之列。
            </p>
          </section>
        </section>

        <section class="wb-group wb-group--planned" aria-labelledby="servlet-heading">
          <div class="wb-group__head">
            <div>
              <span class="wb-kicker">MS-07</span>
              <h2 id="servlet-heading">Servlet 映射演示</h2>
            </div>
            <PracticeStatus status="planned" />
          </div>
          <p class="wb-group__placeholder">
            Servlet 与请求 URI 映射属于 MS-07，本阶段只保留规划入口，不创建演示路由或虚假功能。
          </p>
        </section>

        <footer class="wb-page__footer">
          <span>截图与运行证据将在对应阶段真实生成，不使用外部网页素材或伪造截图。</span>
          <RouterLink to="/addresses">前往地址簿管理 →</RouterLink>
        </footer>
      </div>
    </div>
  </div>
</template>
