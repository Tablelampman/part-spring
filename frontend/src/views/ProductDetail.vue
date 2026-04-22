<template>
  <div class="product-detail" v-loading="loading">
    <el-page-header @back="$router.go(-1)" :title="$t('product.back')" class="mb-4">
      <template #content>
        <span class="text-large font-600 mr-3"> {{ $t('product.details') }} </span>
      </template>
    </el-page-header>

    <div v-if="product" class="detail-container">
      <div class="product-card">
        <div class="product-image-container">
          <el-image :src="product.imageUrl" class="main-image" fit="cover" v-if="product.imageUrl" />
          <div v-else class="image-placeholder">{{ $t('product.noImage') }}</div>
        </div>
        <div class="product-info">
          <h1>{{ product.name }}</h1>
          <el-tag type="info" style="margin-bottom: 15px;">{{ getCategoryLabel(product.category) }}</el-tag>
          <div class="price">￥{{ product.price }}</div>
          <div class="stock">{{ $t('product.stock') }}: {{ product.stock }}</div>
          <p class="description">{{ product.description }}</p>

          <el-button
            v-if="!userStore.token || userStore.userInfo.role === 'CONSUMER'"
            type="primary"
            size="large"
            @click="addToCart"
            :disabled="product.stock <= 0"
            class="action-btn"
          >
            {{ $t('product.addToCart') }}
          </el-button>
        </div>
      </div>

      <el-divider border-style="dashed" />

      <!-- Comments Section -->
      <div class="comments-section">
        <h3>{{ $t('comment.title') }}</h3>

        <div class="comment-form">
          <el-input
            v-model="newComment"
            type="textarea"
            :rows="3"
            :placeholder="$t('comment.placeholder')"
            :disabled="!userStore.token"
          />
          <div style="margin-top: 10px; text-align: right;">
            <el-button v-if="!userStore.token" type="primary" @click="$router.push('/login')">
              {{ $t('comment.loginToComment') }}
            </el-button>
            <el-button v-else type="primary" @click="submitComment" :disabled="!newComment.trim()">
              {{ $t('comment.postComment') }}
            </el-button>
          </div>
        </div>

        <div class="comments-list" v-loading="commentsLoading">
          <el-empty v-if="comments.length === 0" :description="$t('comment.noComments')" />

          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-header">
              <span class="comment-author">
                {{ comment.user ? comment.user.username : 'Unknown' }}
                <el-tag size="small" type="info" v-if="comment.user && comment.user.role">{{ $t('roles.' + comment.user.role.toLowerCase()) }}</el-tag>
              </span>
              <span class="comment-time">{{ new Date(comment.createdAt).toLocaleString() }}</span>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
            <div class="comment-actions">
               <el-button
                v-if="userStore.token && (userStore.userInfo.id === comment.userId || userStore.userInfo.role === 'ADMIN')"
                type="danger"
                link
                @click="deleteComment(comment.id)"
              >
                {{ $t('common.delete') }}
              </el-button>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/api/axios'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const commentsLoading = ref(false)
const product = ref(null)
const comments = ref([])
const newComment = ref('')

const getCategoryLabel = (category) => {
  if (!category || category === 'Others') return t('categories.others')
  if (category === 'Fruits') return t('categories.fruits')
  if (category === 'Vegetables') return t('categories.vegetables')
  if (category === 'Meat') return t('categories.meat')
  if (category === 'Dairy') return t('categories.dairy')
  if (category === 'Grains') return t('categories.grains')
  return category
}

const fetchProductDetails = async () => {
  loading.value = true
  try {
    const productId = route.params.id
    product.value = await request.get(`/api/products/public/${productId}`)
    await fetchComments()
  } catch (error) {
    ElMessage.error(t('common.error'))
    router.push('/')
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  commentsLoading.value = true
  try {
    comments.value = await request.get(`/api/comments/product/${route.params.id}`)
  } catch (error) {
    ElMessage.error(t('common.error'))
  } finally {
    commentsLoading.value = false
  }
}

const addToCart = async () => {
  if (!userStore.token) {
    ElMessage.warning('Please login to add to cart')
    router.push('/login')
    return
  }
  try {
    await request.post('/api/cart', { productId: product.value.id, quantity: 1 })
    ElMessage.success(t('common.success'))
  } catch(e){}
}

const submitComment = async () => {
  if (!newComment.value.trim()) return

  try {
    await request.post('/api/comments', {
      productId: product.value.id,
      content: newComment.value.trim()
    })
    ElMessage.success(t('common.success'))
    newComment.value = ''
    fetchComments()
  } catch (e) {}
}

const deleteComment = async (commentId) => {
  ElMessageBox.confirm(
    'Are you sure you want to delete this comment?',
    'Warning',
    {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning',
    }
  ).then(async () => {
    try {
      await request.delete(`/api/comments/${commentId}`)
      ElMessage.success(t('common.success'))
      fetchComments()
    } catch (e) {}
  }).catch(() => {})
}

onMounted(() => {
  fetchProductDetails()
})
</script>

<style scoped>
.mb-4 {
  margin-bottom: 20px;
}
.detail-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.05);
}
.product-card {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}
.product-image-container {
  flex: 0 0 400px;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}
.main-image, .image-placeholder {
  width: 100%;
  height: 100%;
}
.image-placeholder {
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
}
.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.product-info h1 {
  margin-top: 0;
  margin-bottom: 10px;
}
.price {
  font-size: 28px;
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 10px;
}
.stock {
  color: #909399;
  margin-bottom: 20px;
}
.description {
  color: #606266;
  line-height: 1.6;
  flex-grow: 1;
}
.action-btn {
  margin-top: auto;
  width: 200px;
}

.comments-section {
  margin-top: 30px;
}
.comment-form {
  margin-bottom: 30px;
}
.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.comment-author {
  font-weight: bold;
  color: #303133;
}
.comment-time {
  color: #909399;
  font-size: 13px;
}
.comment-content {
  color: #606266;
  line-height: 1.5;
  margin-bottom: 8px;
  white-space: pre-wrap;
}
.comment-actions {
  text-align: right;
}
</style>
