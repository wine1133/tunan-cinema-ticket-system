<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { ChatDotRound, Close, Delete, Promotion } from '@element-plus/icons-vue'
import http from '@/api/http'

const open = ref(false)
const input = ref('')
const loading = ref(false)
const messagesEl = ref(null)
const sessionId = ref('')
const lastSource = ref('')
const messages = ref([
  {
    role: 'assistant',
    content: '你好，我是小兔客服 🐰\n购票、选座、支付、取票、取消订单或找影院，都可以直接问我。',
    time: currentTime()
  }
])
const quickQuestions = ['怎么在线选座购票？', '取票码在哪里看？', '订单可以取消吗？', '推荐一部电影']
const sourceLabel = computed(() => lastSource.value === 'AI_MODEL' ? 'AI 大模型在线' : '智能客服在线')

function currentTime() {
  return new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function getSessionId() {
  const key = 'tunan_ai_session'
  let value = localStorage.getItem(key)
  if (!value) {
    value = crypto.randomUUID ? crypto.randomUUID() : `guest-${Date.now()}-${Math.random().toString(16).slice(2)}`
    localStorage.setItem(key, value)
  }
  return value
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesEl.value) messagesEl.value.scrollTop = messagesEl.value.scrollHeight
  })
}

function toggle() {
  open.value = !open.value
  if (open.value && messages.value.length > 1) scrollToBottom()
}

function clearChat() {
  messages.value = [{
    role: 'assistant',
    content: '对话已清空。我是小兔客服，有什么可以帮你？',
    time: currentTime()
  }]
  lastSource.value = ''
}

async function ask(question) {
  input.value = question
  await send()
}

async function send() {
  const text = input.value.trim()
  if (!text || loading.value) return
  input.value = ''
  messages.value.push({ role: 'user', content: text, time: currentTime() })
  loading.value = true
  scrollToBottom()
  try {
    const { data } = await http.post('/public/ai-chat', {
      message: text,
      sessionId: sessionId.value
    }, { timeout: 25000 })
    lastSource.value = data.source || ''
    messages.value.push({
      role: 'assistant',
      content: data.reply || '我暂时没有找到答案，请换个说法再试一次。',
      time: data.createdAt || currentTime()
    })
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: '网络有点忙，我暂时没有连上客服服务。请稍后再试，或查看“公告中心”和影院联系电话。',
      time: currentTime()
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

onMounted(() => {
  sessionId.value = getSessionId()
})
</script>

<template>
  <div class="ai-service">
    <transition name="ai-panel">
      <section v-if="open" class="ai-panel" role="dialog" aria-label="兔南 AI 客服">
        <header class="ai-head">
          <div class="ai-identity">
            <div class="ai-face">🐰<span></span></div>
            <div>
              <strong>小兔客服</strong>
              <small><i></i>{{ sourceLabel }}</small>
            </div>
          </div>
          <div class="ai-head-actions">
            <button type="button" title="清空对话" aria-label="清空对话" @click="clearChat"><el-icon><Delete /></el-icon></button>
            <button type="button" title="关闭" aria-label="关闭客服" @click="open = false"><el-icon><Close /></el-icon></button>
          </div>
        </header>

        <div ref="messagesEl" class="ai-messages">
          <div class="ai-day">今天 · 自动回复</div>
          <article v-for="(message, index) in messages" :key="index" class="ai-message" :class="message.role">
            <div v-if="message.role === 'assistant'" class="message-avatar">🐰</div>
            <div class="message-body">
              <div class="message-bubble">{{ message.content }}</div>
              <time>{{ message.time }}</time>
            </div>
          </article>

          <article v-if="loading" class="ai-message assistant">
            <div class="message-avatar">🐰</div>
            <div class="message-body">
              <div class="message-bubble typing"><i></i><i></i><i></i></div>
            </div>
          </article>

          <div v-if="messages.length <= 1 && !loading" class="quick-wrap">
            <span>你可以这样问</span>
            <button v-for="question in quickQuestions" :key="question" type="button" @click="ask(question)">
              {{ question }}<b>↗</b>
            </button>
          </div>
        </div>

        <form class="ai-composer" @submit.prevent="send">
          <textarea v-model="input" maxlength="500" rows="2" placeholder="输入你的问题，按 Enter 发送…" aria-label="客服问题" @keydown.enter.exact.prevent="send"></textarea>
          <button type="submit" :disabled="loading || !input.trim()" aria-label="发送">
            <el-icon><Promotion /></el-icon>
          </button>
        </form>
        <div class="ai-footnote">AI 自动回复 · 密码及银行卡信息请勿发送</div>
      </section>
    </transition>

    <button class="ai-launcher" :class="{ active: open }" type="button" :aria-label="open ? '收起 AI 客服' : '打开 AI 客服'" @click="toggle">
      <span class="launcher-icon"><el-icon v-if="!open"><ChatDotRound /></el-icon><el-icon v-else><Close /></el-icon></span>
      <span v-if="!open" class="launcher-copy"><strong>AI 客服</strong><small>有问题问我</small></span>
      <i v-if="!open"></i>
    </button>
  </div>
</template>

<style scoped>
.ai-service { position: fixed; right: 28px; bottom: 26px; z-index: 120; font-family: inherit; }
.ai-launcher {
  position: relative; z-index: 2; min-width: 58px; height: 58px; padding: 0; border: 0; border-radius: 20px;
  display: flex; align-items: center; gap: 10px; color: #fff; cursor: pointer;
  background: linear-gradient(140deg, #ff7759 0%, #f04d2d 60%, #db3c24 100%);
  box-shadow: 0 14px 34px rgba(215, 62, 33, .36), inset 0 1px 0 rgba(255,255,255,.28);
  transition: transform .25s ease, border-radius .25s ease, box-shadow .25s ease;
}
.ai-launcher:hover { transform: translateY(-3px); box-shadow: 0 18px 42px rgba(215, 62, 33, .43); }
.ai-launcher.active { border-radius: 50%; min-width: 58px; justify-content: center; }
.launcher-icon { width: 58px; height: 58px; display: grid; place-items: center; font-size: 25px; flex: 0 0 58px; }
.launcher-copy { display: grid; padding-right: 19px; text-align: left; line-height: 1.15; }
.launcher-copy strong { font-size: 14px; letter-spacing: .5px; }
.launcher-copy small { margin-top: 4px; color: rgba(255,255,255,.72); font-size: 10px; }
.ai-launcher > i { position: absolute; top: 7px; right: 7px; width: 9px; height: 9px; border: 2px solid #fff; border-radius: 50%; background: #3ad29f; }
.ai-panel {
  position: absolute; right: 0; bottom: 76px; width: min(390px, calc(100vw - 28px)); height: min(610px, calc(100vh - 126px));
  display: flex; flex-direction: column; overflow: hidden; border: 1px solid rgba(255,255,255,.68); border-radius: 25px;
  background: #fbf9f4; box-shadow: 0 30px 90px rgba(19, 26, 38, .26), 0 0 0 1px rgba(25,34,51,.05);
  transform-origin: right bottom;
}
.ai-head {
  position: relative; min-height: 86px; padding: 17px 18px; display: flex; align-items: center; justify-content: space-between;
  overflow: hidden; color: #fff; background: #111a27;
}
.ai-head::before { content: ''; position: absolute; width: 210px; height: 210px; right: -80px; top: -145px; border-radius: 50%; background: radial-gradient(circle, rgba(255,103,71,.55), transparent 66%); }
.ai-head::after { content: 'TUNAN'; position: absolute; right: 60px; bottom: -8px; color: rgba(255,255,255,.035); font-size: 35px; font-weight: 900; letter-spacing: 7px; }
.ai-identity { position: relative; z-index: 1; display: flex; align-items: center; gap: 12px; }
.ai-face {
  position: relative; width: 48px; height: 48px; display: grid; place-items: center; border-radius: 15px;
  background: linear-gradient(145deg, #ffe1cc, #ffb68f); font-size: 26px; box-shadow: inset 0 -4px 10px rgba(210,84,38,.12);
}
.ai-face span { position: absolute; right: -2px; bottom: -2px; width: 12px; height: 12px; border: 2px solid #111a27; border-radius: 50%; background: #3ad29f; }
.ai-identity strong { display: block; font-size: 15px; letter-spacing: .4px; }
.ai-identity small { display: flex; align-items: center; gap: 5px; margin-top: 5px; color: rgba(255,255,255,.58); font-size: 10px; }
.ai-identity small i { width: 6px; height: 6px; border-radius: 50%; background: #3ad29f; box-shadow: 0 0 0 3px rgba(58,210,159,.13); }
.ai-head-actions { position: relative; z-index: 1; display: flex; gap: 4px; }
.ai-head-actions button { width: 30px; height: 30px; display: grid; place-items: center; border: 0; border-radius: 9px; color: rgba(255,255,255,.65); background: rgba(255,255,255,.07); cursor: pointer; transition: .2s; }
.ai-head-actions button:hover { color: #fff; background: rgba(255,255,255,.14); }
.ai-messages {
  flex: 1; overflow-y: auto; padding: 16px 15px 24px;
  background-image: radial-gradient(rgba(255,101,66,.11) .7px, transparent .7px); background-size: 14px 14px;
  scrollbar-width: thin; scrollbar-color: #d9d2c8 transparent;
}
.ai-day { width: fit-content; margin: 0 auto 16px; padding: 5px 10px; border-radius: 999px; color: #9b958c; background: #f0ede7; font-size: 9px; letter-spacing: .4px; }
.ai-message { display: flex; align-items: flex-start; gap: 8px; margin: 0 0 15px; }
.ai-message.user { justify-content: flex-end; }
.message-avatar { flex: 0 0 29px; width: 29px; height: 29px; display: grid; place-items: center; border-radius: 10px; background: #fff; box-shadow: 0 4px 12px rgba(20,29,43,.09); font-size: 15px; }
.message-body { max-width: 79%; }
.ai-message.user .message-body { display: flex; flex-direction: column; align-items: flex-end; }
.message-bubble { padding: 11px 13px; border-radius: 5px 15px 15px 15px; color: #343c4a; background: #fff; box-shadow: 0 5px 18px rgba(23,31,44,.07); font-size: 12px; line-height: 1.75; white-space: pre-wrap; word-break: break-word; }
.ai-message.user .message-bubble { color: #fff; border-radius: 15px 5px 15px 15px; background: linear-gradient(135deg, #ff7759, #ec4d2f); box-shadow: 0 6px 18px rgba(231,77,47,.18); }
.message-body time { display: block; margin-top: 5px; padding: 0 3px; color: #b3ada4; font-size: 8px; }
.ai-message.user time { text-align: right; }
.typing { display: flex; align-items: center; gap: 4px; min-width: 50px; min-height: 36px; }
.typing i { width: 5px; height: 5px; border-radius: 50%; background: #ff7657; animation: typing 1.05s infinite ease-in-out; }
.typing i:nth-child(2) { animation-delay: .13s; }
.typing i:nth-child(3) { animation-delay: .26s; }
.quick-wrap { display: grid; gap: 8px; margin: 18px 0 5px 37px; }
.quick-wrap > span { color: #a39c92; font-size: 9px; letter-spacing: .5px; }
.quick-wrap button { width: 100%; padding: 10px 12px; display: flex; justify-content: space-between; align-items: center; border: 1px solid #eee8de; border-radius: 11px; color: #525b69; background: rgba(255,255,255,.83); font-size: 11px; text-align: left; cursor: pointer; transition: .2s; }
.quick-wrap button:hover { transform: translateX(2px); border-color: #ffc4b5; color: var(--orange); background: #fff; }
.quick-wrap button b { color: #c4bdb2; font-weight: 500; }
.ai-composer { padding: 11px 12px 8px; display: flex; align-items: flex-end; gap: 8px; border-top: 1px solid #ebe6dc; background: #fbf9f4; }
.ai-composer textarea { flex: 1; min-height: 45px; max-height: 98px; resize: none; padding: 12px 13px; border: 1px solid #e5dfd5; border-radius: 14px; outline: none; color: #343c4a; background: #fff; font-size: 12px; line-height: 1.55; transition: .2s; }
.ai-composer textarea:focus { border-color: #ff9c84; box-shadow: 0 0 0 3px rgba(255,101,66,.08); }
.ai-composer button { flex: 0 0 43px; width: 43px; height: 43px; display: grid; place-items: center; border: 0; border-radius: 13px; color: #fff; background: linear-gradient(140deg, #ff7759, #e8492b); font-size: 19px; cursor: pointer; transition: .2s; }
.ai-composer button:disabled { opacity: .38; cursor: not-allowed; transform: none; }
.ai-composer button:not(:disabled):hover { transform: translateY(-1px); box-shadow: 0 7px 16px rgba(231,77,47,.22); }
.ai-footnote { padding: 0 13px 9px; color: #b1aaa0; background: #fbf9f4; font-size: 8px; text-align: center; }
.ai-panel-enter-active, .ai-panel-leave-active { transition: opacity .24s ease, transform .3s cubic-bezier(.2,.75,.25,1); }
.ai-panel-enter-from, .ai-panel-leave-to { opacity: 0; transform: translateY(16px) scale(.96); }
@keyframes typing { 0%, 60%, 100% { transform: translateY(0); opacity: .45; } 30% { transform: translateY(-4px); opacity: 1; } }
@media (max-width: 680px) {
  .ai-service { right: 14px; bottom: 76px; }
  .ai-launcher { height: 52px; border-radius: 17px; }
  .launcher-icon { width: 52px; height: 52px; flex-basis: 52px; font-size: 22px; }
  .launcher-copy { padding-right: 14px; }
  .launcher-copy small { display: none; }
  .ai-launcher.active { min-width: 52px; }
  .ai-panel { position: fixed; right: 10px; bottom: 70px; width: calc(100vw - 20px); height: calc(100dvh - 148px); max-height: 650px; border-radius: 22px; }
  .ai-head { min-height: 76px; padding: 14px 15px; }
  .ai-face { width: 43px; height: 43px; border-radius: 13px; font-size: 23px; }
  .message-body { max-width: 84%; }
}
</style>