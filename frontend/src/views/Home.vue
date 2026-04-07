<template>
  <div class="home">
    <h2>Marketplace</h2>

    <div class="filter-section">
      <el-form :inline="true" class="search-form">
        <el-form-item label="Search">
          <el-input v-model="searchParams.name" placeholder="Product name" clearable @keyup.enter="fetchProducts" />
        </el-form-item>
        <el-form-item label="Price">
          <el-input-number v-model="searchParams.minPrice" :min="0" placeholder="Min" style="width: 100px" />
          <span style="margin: 0 10px;">-</span>
          <el-input-number v-model="searchParams.maxPrice" :min="0" placeholder="Max" style="width: 100px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchProducts">Search</el-button>
          <el-button @click="resetSearch">Reset</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-tabs v-model="activeCategory" class="category-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="All" name="All"></el-tab-pane>
      <el-tab-pane label="Fruits" name="Fruits"></el-tab-pane>
      <el-tab-pane label="Vegetables" name="Vegetables"></el-tab-pane>
      <el-tab-pane label="Meat & Poultry" name="Meat"></el-tab-pane>
      <el-tab-pane label="Dairy & Eggs" name="Dairy"></el-tab-pane>
      <el-tab-pane label="Grains" name="Grains"></el-tab-pane>
      <el-tab-pane label="Others" name="Others"></el-tab-pane>
    </el-tabs>

    <div class="product-grid" v-if="products.length > 0" v-loading="loading">
      <el-card v-for="product in products" :key="product.id" class="product-card" :body-style="{ padding: '0px' }" @click="goToProduct(product.id)" style="cursor: pointer;">
        <img :src="product.imageUrl" class="product-image" v-if="product.imageUrl"/>
        <div v-else class="image-placeholder">No Image</div>
        <div class="product-info">
          <h3>{{ product.name }}</h3>
          <el-tag size="small" type="info" class="cat-tag">{{ product.category || 'Others' }}</el-tag>
          <p class="desc">{{ product.description }}</p>
          <div class="bottom">
            <span class="price">￥{{ product.price }}</span>
            <span class="stock">Stock: {{ product.stock }}</span>
          </div>
          <!-- Show Add to Cart for consumers AND unauthenticated users (to prompt login) -->
          <el-button
            v-if="!userStore.token || userStore.userInfo.role === 'CONSUMER'"
            type="primary"
            style="width: 100%; margin-top: 10px;"
            @click.stop="addToCart(product.id)"
            :disabled="product.stock <= 0"
          >
            Add to Cart
          </el-button>
        </div>
      </el-card>
    </div>
    <el-empty description="No products found" v-else></el-empty>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/axios'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

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
    ElMessage.success('Added to cart')
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
