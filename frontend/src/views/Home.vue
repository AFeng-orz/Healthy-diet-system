<template>
  <section class="hero glass-card">
    <div>
      <span class="status-pill"><span class="status-dot"></span>Phase 3 登录注册已接入</span>
      <h1 class="page-title">用清晰的数据，陪你建立健康饮食节奏。</h1>
      <p class="page-subtitle">
        当前已完成账号注册、登录和登录态保护。下一阶段会进入健康档案，记录身高、体重、目标和运动频率，
        为后续饮食规划提供基础数据。
      </p>
    </div>
    <div class="hero-metric">
      <span>当前账号状态</span>
      <strong>已登录</strong>
      <small>后续会展示今日热量目标与完成度</small>
    </div>
  </section>

  <section class="placeholder-grid">
    <article class="placeholder-card">
      <strong>健康档案</strong>
      <p>记录身高、体重、运动频率和饮食目标，为营养计算提供基础。</p>
    </article>
    <article class="placeholder-card">
      <strong>健康画像</strong>
      <p>通过生活方式问卷识别饮食结构、运动习惯和作息压力。</p>
    </article>
    <article class="placeholder-card">
      <strong>饮食计划</strong>
      <p>后续基于规则引擎生成可解释的一日三餐建议。</p>
    </article>
  </section>
</template>

<style scoped>
.hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 260px;
  gap: 28px;
  align-items: center;
  padding: 42px;
}

.hero .page-title {
  max-width: 720px;
  margin-top: 22px;
}

.hero .page-subtitle {
  max-width: 680px;
}

.hero-metric {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 220px;
  padding: 28px;
  border-radius: 28px;
  background: linear-gradient(145deg, #e8f8ee, #ffffff);
  box-shadow: var(--hd-shadow-soft);
}

.hero-metric span,
.hero-metric small {
  color: var(--hd-muted);
}

.hero-metric strong {
  margin: 18px 0 10px;
  font-size: 42px;
  letter-spacing: -0.05em;
}

@media (max-width: 820px) {
  .hero {
    grid-template-columns: 1fr;
    padding: 28px;
  }
}
</style>