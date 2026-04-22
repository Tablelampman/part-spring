<template>
  <div class="cart">
    <h2>{{ $t('cart.title') }}</h2>
    <el-table :data="cartItems" style="width: 100%" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column :label="$t('cart.product')">
        <template #default="scope">
          <div style="display: flex; align-items: center;">
            <img :src="scope.row.product.imageUrl" style="width: 50px; height: 50px; object-fit: cover; margin-right: 10px;"/>
            <span>{{ scope.row.product.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="$t('product.price')" width="120">
        <template #default="scope">
          ￥{{ scope.row.product.price }}
        </template>
      </el-table-column>
      <el-table-column prop="quantity" :label="$t('cart.quantity')" width="150" />
      <el-table-column :label="$t('cart.subtotal')" width="120">
        <template #default="scope">
          ￥{{ (scope.row.product.price * scope.row.quantity).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.actions')" width="100">
        <template #default="scope">
          <el-button size="small" type="danger" @click="removeItem(scope.row.id)">{{ $t('cart.remove') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="checkout-bar" v-if="cartItems.length > 0">
      <div class="total">
        {{ $t('cart.total') }}: <span class="price">￥{{ totalAmount.toFixed(2) }}</span>
      </div>
      <el-button type="primary" size="large" @click="dialogVisible = true" :disabled="selectedItems.length === 0">
        {{ $t('cart.checkout', { count: selectedItems.length }) }}
      </el-button>
    </div>

    <el-dialog v-model="dialogVisible" :title="$t('cart.checkout', {count: selectedItems.length})" width="30%">
      <el-form>
        <el-form-item :label="$t('cart.shippingAddress')">
          <el-input type="textarea" v-model="shippingAddress" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleCheckout">{{ $t('common.confirm') }}</el-button>
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
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
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
  ElMessage.success(t('cart.removed'))
  fetchCart()
}

const handleCheckout = async () => {
  if (!shippingAddress.value) {
    ElMessage.warning(t('cart.emptyWarning'))
    return
  }
  const itemIds = selectedItems.value.map(i => i.id)
  await request.post('/api/orders/checkout', {
    cartItemIds: itemIds,
    shippingAddress: shippingAddress.value
  })
  ElMessage.success(t('cart.orderSuccess'))
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
