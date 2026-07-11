import { useUserStore } from '../stores/user'
import { hasAnyPermission } from '../utils/permission'

export default {
  mounted(element, binding) {
    const permissions = Array.isArray(binding.value) ? binding.value : [binding.value]
    const userStore = useUserStore()
    if (!hasAnyPermission(userStore.userInfo, permissions)) {
      element.remove()
    }
  },
}
