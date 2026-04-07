<template>
  <div class="home">
    <h2>Marketplace</h2>

    <el-tabs v-model="activeCategory" class="category-tabs">
      <el-tab-pane label="All" name="All"></el-tab-pane>
      <el-tab-pane label="Fruits" name="Fruits"></el-tab-pane>
      <el-tab-pane label="Vegetables" name="Vegetables"></el-tab-pane>
      <el-tab-pane label="Meat & Poultry" name="Meat"></el-tab-pane>
      <el-tab-pane label="Dairy & Eggs" name="Dairy"></el-tab-pane>
      <el-tab-pane label="Grains" name="Grains"></el-tab-pane>
      <el-tab-pane label="Others" name="Others"></el-tab-pane>
    </el-tabs>

    <div class="product-grid" v-if="filteredProducts.length > 0">
      <el-card v-for="product in filteredProducts" :key="product.id" class="product-card" :body-style="{ padding: '0px' }">
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
            @click="addToCart(product.id)"
            :disabled="product.stock <= 0"
          >
            Add to Cart
          </el-button>
        </div>
      </el-card>
    </div>
    <el-empty description="No products found in this category" v-else></el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/axios'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const products = ref([])
const activeCategory = ref('All')

const fetchProducts = async () => {
  // Use public endpoint
  products.value = await request.get('/api/products/public')
}

const filteredProducts = computed(() => {
  if (activeCategory.value === 'All') return products.value
  return products.value.filter(p => (p.category || 'Others') === activeCategory.value)
})

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
