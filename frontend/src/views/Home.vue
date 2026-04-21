<template>
  <div class="home">
    <h2>{{ $t('product.marketplace') }}</h2>

    <div class="filter-section">
      <el-form :inline="true" class="search-form">
        <el-form-item :label="$t('common.search')">
          <el-input v-model="searchParams.name" :placeholder="$t('product.productName')" clearable @keyup.enter="fetchProducts" />
        </el-form-item>
        <el-form-item :label="$t('product.price')">
          <el-input-number v-model="searchParams.minPrice" :min="0" :placeholder="$t('product.minPrice')" style="width: 100px" />
          <span style="margin: 0 10px;">-</span>
          <el-input-number v-model="searchParams.maxPrice" :min="0" :placeholder="$t('product.maxPrice')" style="width: 100px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchProducts">{{ $t('common.search') }}</el-button>
          <el-button @click="resetSearch">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-tabs v-model="activeCategory" class="category-tabs" @tab-change="handleTabChange">
      <el-tab-pane :label="$t('common.all')" name="All"></el-tab-pane>
      <el-tab-pane :label="$t('categories.fruits')" name="Fruits"></el-tab-pane>
      <el-tab-pane :label="$t('categories.vegetables')" name="Vegetables"></el-tab-pane>
      <el-tab-pane :label="$t('categories.meat')" name="Meat"></el-tab-pane>
      <el-tab-pane :label="$t('categories.dairy')" name="Dairy"></el-tab-pane>
      <el-tab-pane :label="$t('categories.grains')" name="Grains"></el-tab-pane>
      <el-tab-pane :label="$t('categories.others')" name="Others"></el-tab-pane>
    </el-tabs>

    <div class="product-grid" v-if="products.length > 0" v-loading="loading">
      <el-card v-for="product in products" :key="product.id" class="product-card" :body-style="{ padding: '0px' }" @click="goToProduct(product.id)" style="cursor: pointer;">
        <img :src="product.imageUrl" class="product-image" v-if="product.imageUrl"/>
        <div v-else class="image-placeholder">{{ $t('product.noImage') }}</div>
        <div class="product-info">
          <h3>{{ product.name }}</h3>
          <el-tag size="small" type="info" class="cat-tag">{{ getCategoryLabel(product.category) }}</el-tag>
          <p class="desc">{{ product.description }}</p>
          <div class="bottom">
            <span class="price">￥{{ product.price }}</span>
            <span class="stock">{{ $t('product.stock') }}: {{ product.stock }}</span>
          </div>
          <el-button
            v-if="!userStore.token || userStore.userInfo.role === 'CONSUMER'"
            type="primary"
            style="width: 100%; margin-top: 10px;"
            @click.stop="addToCart(product.id)"
            :disabled="product.stock <= 0"
          >
            {{ $t('product.addToCart') }}
          </el-button>
        </div>
      </el-card>
    </div>
    <el-empty :description="$t('product.noProducts')" v-else></el-empty>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/axios'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const products = ref([])
const activeCategory = ref('All')
const loading = ref(false)

const searchParams = reactive({
  name: '',
  minPrice: null,
  maxPrice: null
})

const getCategoryLabel = (category) => {
  if (!category || category === 'Others') return t('categories.others')
  if (category === 'Fruits') return t('categories.fruits')
  if (category === 'Vegetables') return t('categories.vegetables')
  if (category === 'Meat') return t('categories.meat')
  if (category === 'Dairy') return t('categories.dairy')
  if (category === 'Grains') return t('categories.grains')
  return category
}

const goToProduct = (id) => {
  router.push('/product/' + id)
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchParams.name) params.name = searchParams.name
    if (searchParams.minPrice !== null) params.minPrice = searchParams.minPrice
    if (searchParams.maxPrice !== null) params.maxPrice = searchParams.maxPrice
    if (activeCategory.value !== 'All') params.category = activeCategory.value

    products.value = await request.get('/api/products/public', { params })
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  fetchProducts()
}

const resetSearch = () => {
  searchParams.name = ''
  searchParams.minPrice = null
  searchParams.maxPrice = null
  activeCategory.value = 'All'
  fetchProducts()
}

const addToCart = async (productId) => {
  if (!userStore.token) {
    ElMessage.warning('Please login to add to cart')
    router.push('/login')
    return
  }

  try {
    await request.post('/api/cart', { productId, quantity: 1 })
    ElMessage.success(t('common.success'))
  } catch(e){}
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.filter-section {
  background: #fff;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.05);
}
.category-tabs {
  margin-bottom: 20px;
}
.cat-tag {
  margin-bottom: 5px;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}
.product-card {
  transition: all 0.3s;
}
.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}
.product-image, .image-placeholder {
  width: 100%;
  height: 200px;
  object-fit: cover;
}
.image-placeholder {
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
}
.product-info {
  padding: 14px;
}
.desc {
  color: #666;
  font-size: 14px;
  margin: 10px 0;
  height: 40px;
  overflow: hidden;
}
.bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}
.stock {
  color: #909399;
  font-size: 13px;
}
</style>
