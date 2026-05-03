<script setup lang="ts">
const { t } = useI18n()
</script>

<template>
  <footer class="border-t border-separator bg-surface-secondary/30">
    <div class="max-w-6xl mx-auto px-4 sm:px-6 py-10">
      <div class="grid grid-cols-2 md:grid-cols-4 gap-8">
        <!-- Brand -->
        <div class="col-span-2 md:col-span-1">
          <div class="flex items-center gap-2 mb-3">
            <Icon name="mdi:shield-key" size="20" class="text-accent" />
            <span class="text-[15px] font-semibold text-text-primary">Auth Center</span>
          </div>
          <p class="text-xs text-text-tertiary leading-relaxed max-w-52">
            {{ t('footer.tagline') || '企业级 OAuth 2.0 认证平台，安全、可靠、开放。' }}
          </p>
        </div>

        <!-- Product -->
        <div>
          <h4 class="text-[11px] font-semibold uppercase tracking-wider text-text-tertiary mb-3">产品</h4>
          <ul class="space-y-2">
            <li><a href="#" class="text-[13px] text-text-secondary hover:text-text-primary transition-colors">OAuth 2.0</a></li>
            <li><a href="#" class="text-[13px] text-text-secondary hover:text-text-primary transition-colors">SSO 单点登录</a></li>
            <li><a href="#" class="text-[13px] text-text-secondary hover:text-text-primary transition-colors">MFA 多因素认证</a></li>
          </ul>
        </div>

        <!-- Resources -->
        <div>
          <h4 class="text-[11px] font-semibold uppercase tracking-wider text-text-tertiary mb-3">资源</h4>
          <ul class="space-y-2">
            <li><a href="#" class="text-[13px] text-text-secondary hover:text-text-primary transition-colors">API 文档</a></li>
            <li><a href="#" class="text-[13px] text-text-secondary hover:text-text-primary transition-colors">SDK 下载</a></li>
            <li><a href="#" class="text-[13px] text-text-secondary hover:text-text-primary transition-colors">集成指南</a></li>
          </ul>
        </div>

        <!-- Support -->
        <div>
          <h4 class="text-[11px] font-semibold uppercase tracking-wider text-text-tertiary mb-3">支持</h4>
          <ul class="space-y-2">
            <li><a href="#" class="text-[13px] text-text-secondary hover:text-text-primary transition-colors">帮助中心</a></li>
            <li><a href="#" class="text-[13px] text-text-secondary hover:text-text-primary transition-colors">社区论坛</a></li>
            <li><a href="#" class="text-[13px] text-text-secondary hover:text-text-primary transition-colors">联系我们</a></li>
          </ul>
        </div>
      </div>

      <div class="mt-10 pt-5 border-t border-separator flex flex-col sm:flex-row items-center justify-between gap-3">
        <p class="text-[11px] text-text-tertiary">&copy; {{ new Date().getFullYear() }} Auth Center. All rights reserved.</p>
        <div class="flex items-center gap-4">
          <a href="#" class="text-[11px] text-text-tertiary hover:text-text-secondary transition-colors">隐私政策</a>
          <a href="#" class="text-[11px] text-text-tertiary hover:text-text-secondary transition-colors">服务条款</a>
        </div>
      </div>
    </div>
  </footer>
</template>
