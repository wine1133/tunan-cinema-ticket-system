<script setup>
</script>
<template>
  <div class="not-found">
    <div class="orb">404</div>
    <span>SCENE NOT FOUND</span>
    <h1>这一帧，暂时没有画面</h1>
    <p>你访问的页面不存在，或已经被移出放映单。</p>
    <router-link to="/"><el-button type="primary" round size="large">返回兔南电影</el-button></router-link>
  </div>
</template>
<style scoped>
.not-found { min-height:100vh; display:grid; place-items:center; align-content:center; gap:13px; background:#0b111b; color:#fff; text-align:center; padding:30px; }
.orb { width:180px; height:180px; border-radius:50%; display:grid; place-items:center; font-size:52px; font-weight:900; color:#ff6542; background:radial-gradient(circle at 35% 30%,rgba(255,101,66,.35),transparent 60%),#141d2b; border:1px solid rgba(255,255,255,.1); box-shadow:0 0 80px rgba(255,101,66,.18); margin-bottom:12px; }
.not-found > span { color:#ffb86b; font-size:9px; letter-spacing:3px; }.not-found h1 { margin:3px 0 0; font-size:32px; }.not-found p { margin:0 0 15px; color:rgba(255,255,255,.45); font-size:13px; }
</style>
