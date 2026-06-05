<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import {
  ChatDotRound,
  CollectionTag,
  Share,
  Star,
  User,
} from '@element-plus/icons-vue'
import AppHeader from '../components/AppHeader.vue'
import PostCard from '../components/PostCard.vue'
import SectionHeading from '../components/SectionHeading.vue'
import { createComment, fetchPost } from '../api/blog'

const route = useRoute()
const loading = ref(true)
const detail = ref(null)
const submittingComment = ref(false)
const commentForm = reactive({
  nickname: '',
  content: '',
})
const displayNickname = computed(() => commentForm.nickname.trim() || '游客')
const commentPreviewAvatar = computed(() => animeAvatarFromSeed(displayNickname.value))

function animeAvatarFromSeed(seed) {
  const imageIds = [
    43078, 43074, 43077, 43073, 43062, 43067, 43066, 42872, 42968, 42280,
    24695, 27301, 25942, 33959, 32331, 38682, 36855, 34883, 39059, 43068,
    43039, 43032, 42990, 43038, 43037, 43036, 43030, 43079, 43031, 42989,
    42948, 43014, 43013, 43011, 43010, 43009, 43008, 43003, 42999, 42991,
    42987, 42949, 42965, 42967, 42946, 42966, 42962, 42937, 42954, 42944,
    42947, 42945, 42963, 42932, 42929, 42873, 42856, 42855, 42938, 42925,
    42924, 42889, 42833, 42743, 42676, 42903, 42901, 42898, 42897, 42896,
    42894, 42893, 42888, 42955, 42871, 42869, 42864, 42961, 42837, 42836,
    42834, 42831, 42757, 42721, 42707, 42680, 42677, 42668, 42657, 42615,
    42592, 42573, 42784, 42782, 42780, 42779, 42778, 42776, 42775, 42774,
  ]
  let hash = 0
  for (const char of seed) {
    hash = ((hash << 5) - hash + char.charCodeAt(0)) | 0
  }
  const index = Math.abs(hash) % imageIds.length
  return `/backup-images/anime/anime-${String(index + 1).padStart(3, '0')}-${imageIds[index]}.jpg`
}

async function loadPost() {
  loading.value = true
  detail.value = await fetchPost(route.params.id)
  loading.value = false
}

async function submitComment() {
  if (!commentForm.nickname.trim()) {
    ElMessage.warning('先填写一个昵称再评论')
    return
  }

  if (!commentForm.content.trim()) {
    ElMessage.warning('写点内容再发表评论')
    return
  }

  submittingComment.value = true
  try {
    await createComment(route.params.id, {
      nickname: commentForm.nickname.trim(),
      content: commentForm.content.trim(),
    })
    commentForm.content = ''
    ElMessage.success('评论发布成功')
    await loadPost()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message ?? '评论发布失败')
  } finally {
    submittingComment.value = false
  }
}

onMounted(loadPost)
watch(() => route.params.id, loadPost)
</script>

<template>
  <AppHeader />

  <main class="page-shell">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="surface-card detail-skeleton"></div>
      </template>

      <template #default>
        <section class="two-column detail-layout">
          <article class="surface-card detail-main">
            <div class="detail-cover" :style="{ background: detail.post.coverStyle }">
              <div class="detail-cover__mask"></div>
              <div class="detail-cover__content">
                <div class="chip-row">
                  <span class="mini-chip">{{ detail.post.category }}</span>
                  <span class="mini-chip">{{ detail.post.board }}</span>
                  <span class="mini-chip">{{ detail.post.publishedAt }}</span>
                </div>
                <h1>{{ detail.post.title }}</h1>
                <p>{{ detail.post.intro }}</p>
              </div>
            </div>

            <section class="detail-author">
              <div class="detail-author__badge">
                <img
                  v-if="detail.post.author.avatarUrl"
                  :src="detail.post.author.avatarUrl"
                  :alt="detail.post.author.name"
                />
                <span v-else>{{ detail.post.author.avatarLabel }}</span>
              </div>
              <div class="detail-author__copy">
                <strong>{{ detail.post.author.name }}</strong>
                <span>{{ detail.post.author.title }}</span>
              </div>
              <div class="detail-author__meta">
                <span>{{ detail.post.author.followers }} 粉丝</span>
                <span>{{ detail.post.author.articles }} 篇文章</span>
              </div>
              <el-button round type="primary">关注</el-button>
            </section>

            <section class="detail-content">
              <template v-for="(block, index) in detail.post.blocks" :key="`${block.type}-${index}`">
                <h2 v-if="block.type === 'heading'">{{ block.content }}</h2>
                <p v-else-if="block.type === 'paragraph'">{{ block.content }}</p>
                <blockquote v-else-if="block.type === 'quote'">{{ block.content }}</blockquote>
                <ul v-else-if="block.type === 'list'">
                  <li v-for="item in block.items" :key="item">{{ item }}</li>
                </ul>
                <figure v-else-if="block.type === 'image'" class="detail-image">
                  <img :src="block.items?.[0]" :alt="block.content || '文章图片'" />
                  <figcaption v-if="block.content">{{ block.content }}</figcaption>
                </figure>
              </template>
            </section>

            <section class="detail-actions">
              <button class="detail-action">
                <el-icon><Star /></el-icon>
                <span>点赞 {{ detail.post.stats.likes }}</span>
              </button>
              <button class="detail-action">
                <el-icon><CollectionTag /></el-icon>
                <span>收藏 {{ detail.post.stats.favorites }}</span>
              </button>
              <button class="detail-action">
                <el-icon><ChatDotRound /></el-icon>
                <span>评论 {{ detail.post.stats.comments }}</span>
              </button>
              <button class="detail-action">
                <el-icon><Share /></el-icon>
                <span>分享 {{ detail.post.stats.shares }}</span>
              </button>
            </section>

            <section class="detail-comments">
              <SectionHeading
                title="评论区"
                :description="`当前共 ${detail.comments.length} 条评论，支持游客昵称评论。`"
              />
              <div class="comment-editor">
                <div class="comment-editor__badge">
                  <img :src="commentPreviewAvatar" :alt="`${displayNickname} 头像预览`" />
                </div>
                <div class="comment-editor__body">
                  <div class="comment-editor__head">
                    <strong>{{ displayNickname }}</strong>
                    <span>填写昵称后即可直接发表评论，头像会从本地动漫素材库自动生成</span>
                  </div>
                  <el-input
                    v-model="commentForm.nickname"
                    maxlength="40"
                    placeholder="先输入你的昵称"
                  />
                  <el-input
                    v-model="commentForm.content"
                    :rows="3"
                    placeholder="聊聊你对这篇文章的看法吧"
                    type="textarea"
                  />
                  <div class="comment-editor__actions">
                    <el-button
                      :loading="submittingComment"
                      round
                      type="primary"
                      @click="submitComment"
                    >
                      发表评论
                    </el-button>
                  </div>
                </div>
              </div>
              <div class="comment-list">
                <article
                  v-for="(comment, index) in detail.comments"
                  :key="comment.id"
                  class="comment-item"
                >
                  <div class="comment-avatar">
                    <img
                      v-if="comment.authorAvatarUrl"
                      :src="comment.authorAvatarUrl"
                      :alt="comment.author"
                    />
                    <span v-else>{{ comment.authorInitial }}</span>
                  </div>
                  <div class="comment-body">
                    <div class="comment-body__head">
                      <strong>{{ comment.author }}</strong>
                      <span class="comment-floor">#{{ detail.comments.length - index }}</span>
                    </div>
                    <div class="comment-body__meta">
                      <span>{{ comment.time }}</span>
                      <span>{{ comment.ipLocation || 'IP属地：未知' }}</span>
                    </div>
                    <p>{{ comment.content }}</p>
                    <div class="comment-body__actions">
                      <button type="button">
                        <el-icon><Star /></el-icon>
                        <span>点赞</span>
                      </button>
                      <button type="button">
                        <el-icon><ChatDotRound /></el-icon>
                        <span>回复</span>
                      </button>
                    </div>
                  </div>
                </article>
              </div>
            </section>
          </article>

          <aside class="detail-aside">
            <section class="surface-card aside-panel">
              <SectionHeading title="文章信息" description="适合继续扩展目录、标签和相关推荐。" />
              <div class="detail-outline">
                <div class="detail-outline__item">
                  <span>阅读时长</span>
                  <strong>{{ detail.post.readTime }}</strong>
                </div>
                <div class="detail-outline__item">
                  <span>浏览量</span>
                  <strong>{{ detail.post.views }}</strong>
                </div>
                <div class="detail-outline__item">
                  <span>内容分区</span>
                  <strong>{{ detail.post.board }}</strong>
                </div>
                <div class="detail-outline__item">
                  <span>作者身份</span>
                  <strong>{{ detail.post.author.title }}</strong>
                </div>
              </div>
            </section>

            <section class="surface-card aside-panel">
              <SectionHeading title="相关推荐" description="同风格内容继续串起来。" />
              <div class="related-list">
                <PostCard
                  v-for="post in detail.relatedPosts"
                  :key="post.id"
                  :post="post"
                />
              </div>
            </section>
          </aside>
        </section>
      </template>
    </el-skeleton>
  </main>
</template>

<style scoped>
.detail-skeleton {
  height: 860px;
}

.detail-layout {
  align-items: start;
}

.detail-main {
  overflow: hidden;
}

.detail-cover {
  position: relative;
  min-height: 360px;
  padding: 28px;
  color: white;
}

.detail-cover__mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(17, 24, 39, 0.12), rgba(17, 24, 39, 0.7));
}

.detail-cover__content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: end;
  gap: 18px;
  min-height: 304px;
}

.detail-cover__content h1 {
  margin: 0;
  max-width: 760px;
  font-family: var(--font-display);
  font-size: 40px;
  line-height: 1.15;
}

.detail-cover__content p {
  margin: 0;
  max-width: 620px;
  line-height: 1.85;
  opacity: 0.92;
}

.detail-author {
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  align-items: center;
  gap: 16px;
  padding: 24px 28px;
  border-bottom: 1px solid var(--line-soft);
}

.detail-author__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 68px;
  height: 68px;
  overflow: hidden;
  border-radius: 22px;
  background: linear-gradient(135deg, #fb7299, #5ac8fa);
  color: white;
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 800;
}

.detail-author__badge img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-author__copy strong,
.detail-author__copy span {
  display: block;
}

.detail-author__copy strong {
  font-size: 20px;
}

.detail-author__copy span,
.detail-author__meta span {
  color: var(--text-muted);
}

.detail-author__meta {
  display: grid;
  gap: 6px;
  text-align: right;
}

.detail-content {
  padding: 30px 28px;
}

.detail-content h2 {
  margin: 28px 0 10px;
  font-size: 26px;
}

.detail-content p,
.detail-content li,
.detail-content blockquote {
  color: #26303d;
  line-height: 1.95;
  font-size: 16px;
}

.detail-content blockquote {
  margin: 20px 0;
  padding: 18px 22px;
  border-left: 4px solid var(--brand-pink);
  border-radius: 18px;
  background: rgba(251, 114, 153, 0.08);
}

.detail-content ul {
  padding-left: 22px;
}

.detail-image {
  margin: 24px 0;
}

.detail-image img {
  display: block;
  width: 100%;
  max-height: 680px;
  object-fit: contain;
  border-radius: 22px;
  background: var(--surface-soft);
}

.detail-image figcaption {
  margin-top: 10px;
  color: var(--text-muted);
  font-size: 14px;
  text-align: center;
}

.detail-actions {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  padding: 0 28px 28px;
}

.detail-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px;
  border: none;
  border-radius: 22px;
  background: var(--surface-soft);
  color: var(--text-primary);
  cursor: pointer;
}

.detail-comments {
  padding: 0 28px 28px;
}

.comment-editor {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 14px;
  margin-bottom: 16px;
  padding: 18px 0;
  border-top: 1px solid var(--line-soft);
  border-bottom: 1px solid var(--line-soft);
  background: transparent;
}

.comment-editor__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  overflow: hidden;
  border-radius: 50%;
  background: linear-gradient(135deg, #fb7299, #5ac8fa);
  color: white;
  font-weight: 800;
}

.comment-editor__badge img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-editor__body {
  display: grid;
  gap: 12px;
}

.comment-editor__head strong,
.comment-editor__head span {
  display: block;
}

.comment-editor__head span {
  margin-top: 4px;
  color: var(--text-muted);
  font-size: 13px;
}

.comment-editor__actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.comment-list {
  display: grid;
  gap: 0;
}

.comment-item {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 14px;
  padding: 18px 0;
  border-bottom: 1px solid var(--line-soft);
  background: transparent;
}

.comment-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  overflow: hidden;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(251, 114, 153, 0.8), rgba(90, 200, 250, 0.85));
  color: white;
  font-weight: 800;
}

.comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-body__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.comment-body__head strong {
  color: #18191c;
  font-size: 15px;
}

.comment-floor,
.comment-body__meta,
.comment-body p {
  color: var(--text-muted);
}

.comment-floor {
  flex: 0 0 auto;
  font-size: 12px;
}

.comment-body__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 4px;
  font-size: 12px;
}

.comment-body p {
  margin: 10px 0 0;
  color: #18191c;
  line-height: 1.8;
}

.comment-body__actions {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-top: 10px;
}

.comment-body__actions button {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 0;
  padding: 0;
  background: transparent;
  color: var(--text-muted);
  font-size: 13px;
  cursor: pointer;
  transition: color 0.18s ease;
}

.comment-body__actions button:hover {
  color: var(--brand-pink-deep);
}

.detail-aside {
  position: sticky;
  top: 92px;
  display: grid;
  gap: 18px;
}

.aside-panel {
  padding: 20px;
}

.detail-outline {
  display: grid;
  gap: 12px;
}

.detail-outline__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-radius: 18px;
  background: var(--surface-soft);
}

.detail-outline__item span {
  color: var(--text-muted);
}

.related-list {
  display: grid;
  gap: 14px;
}

.related-list :deep(.post-card) {
  padding: 14px;
}

.related-list :deep(.post-card__cover) {
  min-height: 120px;
}

.related-list :deep(h3) {
  font-size: 18px;
}

@media (max-width: 1080px) {
  .detail-aside {
    position: static;
  }
}

@media (max-width: 720px) {
  .detail-author {
    grid-template-columns: 1fr;
    justify-items: start;
  }

  .detail-author__meta {
    text-align: left;
  }

  .detail-actions {
    grid-template-columns: repeat(2, 1fr);
  }

  .detail-cover__content h1 {
    font-size: 32px;
  }

  .comment-editor {
    grid-template-columns: 1fr;
  }

  .comment-editor__actions {
    flex-direction: column;
  }
}
</style>
