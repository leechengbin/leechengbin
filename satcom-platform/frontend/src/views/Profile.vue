<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="page-title">个人信息</div>
        <div class="page-sub">管理您的账号信息和安全设置</div>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
              <el-button v-if="!isEditing" type="primary" size="small" @click="startEdit">编辑资料</el-button>
            </div>
          </template>
          <el-form :model="form" label-width="100px" :disabled="!isEditing">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
            </el-form-item>
            <el-form-item label="显示名称">
              <el-input v-model="form.displayName" placeholder="请输入显示名称" />
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="form.roleCode" disabled style="width: 100%">
                <el-option label="管理员" value="ADMIN" />
                <el-option label="运维" value="OP" />
                <el-option label="财务" value="FIN" />
              </el-select>
            </el-form-item>
            <el-form-item label="账号状态">
              <el-tag :type="form.enabled ? 'success' : 'danger'">
                {{ form.enabled ? '启用' : '禁用' }}
              </el-tag>
            </el-form-item>
            <el-form-item label="创建时间">
              <el-input :value="formatDateTime(form.createdAt)" disabled />
            </el-form-item>
            <el-form-item v-if="isEditing">
              <el-button type="primary" @click="saveProfile">保存</el-button>
              <el-button @click="cancelEdit">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <span>修改密码</span>
            </div>
          </template>
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="avatar-card">
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <el-avatar :size="100" :src="avatarUrl">
                {{ userInitials }}
              </el-avatar>
            </div>
            <div class="avatar-info">
              <h3>{{ form.displayName || form.username }}</h3>
              <p class="role-tag">{{ getRoleName(form.roleCode) }}</p>
            </div>
          </div>
        </el-card>

        <el-card class="info-card">
          <template #header>
            <span>账号统计</span>
          </template>
          <div class="stat-item">
            <div class="stat-label">角色</div>
            <div class="stat-value">{{ getRoleName(form.roleCode) }}</div>
          </div>
          <el-divider />
          <div class="stat-item">
            <div class="stat-label">设备权限</div>
            <div class="stat-value">{{ equipmentCount }} 个</div>
          </div>
          <el-divider />
          <div class="stat-item">
            <div class="stat-label">最后更新</div>
            <div class="stat-value">{{ formatDateTime(form.updatedAt) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage, FormInstance, FormRules } from 'element-plus';
import http from '../api/http';

const isEditing = ref(false);
const passwordFormRef = ref<FormInstance>();
const equipmentCount = ref(0);

const form = ref({
  id: null as number | null,
  username: '',
  displayName: '',
  roleCode: '',
  enabled: true,
  createdAt: null as string | null,
  updatedAt: null as string | null
});

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

const avatarUrl = computed(() => {
  return `https://api.dicebear.com/7.x/initials/svg?seed=${form.value.username}`;
});

const userInitials = computed(() => {
  const name = form.value.displayName || form.value.username;
  return name.charAt(0).toUpperCase();
});

const getRoleName = (code: string) => {
  const roleMap: Record<string, string> = {
    'ADMIN': '管理员',
    'OP': '运维人员',
    'FIN': '财务人员'
  };
  return roleMap[code] || code;
};

const formatDateTime = (dateStr: string | null) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN');
};

const startEdit = () => {
  isEditing.value = true;
};

const cancelEdit = () => {
  isEditing.value = false;
  fetchProfile();
};

const fetchProfile = async () => {
  try {
    const res: any = await http.get('/users/current');
    if (res.success && res.data) {
      form.value = { ...form.value, ...res.data };
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    const username = localStorage.getItem('displayName') || 'admin';
    const role = localStorage.getItem('role') || 'ADMIN';
    form.value = {
      id: 1,
      username: username,
      displayName: username,
      roleCode: role,
      enabled: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
  }
};

const saveProfile = async () => {
  try {
    await http.put(`/users/${form.value.id}`, {
      displayName: form.value.displayName
    });
    localStorage.setItem('displayName', form.value.displayName);
    ElMessage.success('保存成功');
    isEditing.value = false;
  } catch (error) {
    console.error('保存失败:', error);
    ElMessage.error('保存失败');
  }
};

const changePassword = async () => {
  if (!passwordFormRef.value) return;
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await http.post('/users/change-password', {
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        });
        ElMessage.success('密码修改成功');
        passwordForm.value = {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        };
      } catch (error) {
        console.error('修改密码失败:', error);
        ElMessage.error('修改密码失败');
      }
    }
  });
};

onMounted(() => {
  fetchProfile();
});
</script>

<style scoped>
.page {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
}

.page-sub {
  color: #8c8c9a;
  margin-top: 6px;
  font-size: 13px;
}

.profile-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #26263a;
}

.avatar-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.avatar-section {
  text-align: center;
  padding: 20px;
}

.avatar-wrapper {
  margin-bottom: 16px;
}

.avatar-wrapper :deep(.el-avatar) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  font-size: 36px;
  font-weight: 600;
}

.avatar-info h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #26263a;
}

.role-tag {
  display: inline-block;
  padding: 4px 12px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 20px;
  font-size: 12px;
}

.info-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.stat-label {
  color: #8c8c9a;
  font-size: 14px;
}

.stat-value {
  color: #26263a;
  font-size: 14px;
  font-weight: 500;
}

:deep(.el-divider) {
  margin: 12px 0;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f5;
  background: #fafbfc;
  border-radius: 12px 12px 0 0;
}

:deep(.el-button) {
  border-radius: 8px;
}
</style>
