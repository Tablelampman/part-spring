<template>
  <div class="cart">
    <h2>My Shopping Cart</h2>
    <el-table :data="cartItems" style="width: 100%" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column label="Product">
        <template #default="scope">
          <div style="display: flex; align-items: center;">
            <img :src="scope.row.product.imageUrl" style="width: 50px; height: 50px; object-fit: cover; margin-right: 10px;"/>
            <span>{{ scope.row.product.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="Price" width="120">
        <template #default="scope">
          ￥{{ scope.row.product.price }}
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="Quantity" width="150" />
      <el-table-column label="Subtotal" width="120">
        <template #default="scope">
          ￥{{ (scope.row.product.price * scope.row.quantity).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="Action" width="100">
        <template #default="scope">
          <el-button size="small" type="danger" @click="removeItem(scope.row.id)">Remove</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="checkout-bar" v-if="cartItems.length > 0">
      <div class="total">
        Total: <span class="price">￥{{ totalAmount.toFixed(2) }}</span>
      </div>
      <el-button type="primary" size="large" @click="dialogVisible = true" :disabled="selectedItems.length === 0">
        Checkout ({{ selectedItems.length }} items)
      </el-button>
    </div>

    <el-dialog v-model="dialogVisible" title="Checkout" width="30%">
      <el-form>
        <el-form-item label="Shipping Address">
          <el-input type="textarea" v-model="shippingAddress" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="handleCheckout">Confirm Order</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/api/axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartItems = ref([])
const selectedItems = ref([])
const dialogVisible = ref(false)
const shippingAddress = ref('')

const fetchCart = async () => {
  cartItems.value = await request.get('/api/cart')
}

const handleSelectionChange = (val) => {
  selectedItems.value = val
}

const totalAmount = computed(() => {
  return selectedItems.value.reduce((sum, item) => {
    return sum + (item.product.price * item.quantity)
  }, 0)
})

const removeItem = async (id) => {
  await request.delete(`/api/cart/${id}`)
  ElMessage.success('Removed from cart')
  fetchCart()
}

const handleCheckout = async () => {
  if (!shippingAddress.value) {
    ElMessage.warning('Please enter a shipping address')
    return
  }
  const itemIds = selectedItems.value.map(i => i.id)
  await request.post('/api/orders/checkout', {
    cartItemIds: itemIds,
    shippingAddress: shippingAddress.value
  })
  ElMessage.success('Order created successfully')
  dialogVisible.value = false
  router.push('/orders')
}

onMounted(() => {
  fetchCart()
})
</script>

<style scoped>
.checkout-bar {
  margin-top: 20px;
  padding: 20px;
  background-color: #fff;
  border: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
}
.price {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}
</style>
