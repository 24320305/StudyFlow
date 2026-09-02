// 全项目唯一的状态枚举与中文文案映射。
// 规则（执行方案 5.3）：前端状态标签、枚举中文文案集中在本文件，
// 组件中不得硬编码第二套状态映射。

export const PLAN_STATUS_LABEL: Record<string, string> = {
  ACTIVE: '进行中',
  PAUSED: '已暂停',
  COMPLETED: '已完成',
}

/** Element Plus el-tag 的 type */
export const PLAN_STATUS_TAG: Record<string, 'success' | 'warning' | 'info'> = {
  ACTIVE: 'success',
  PAUSED: 'warning',
  COMPLETED: 'info',
}

export const USER_ROLE_LABEL: Record<string, string> = {
  USER: '用户',
  ADMIN: '管理员',
}

export const USER_STATUS_LABEL: Record<string, string> = {
  NORMAL: '正常',
  RESTRICTED: '受限',
  DISABLED: '已禁用',
}

// —— 社区 / 后台状态（由其他成员维护对应页面，此处仅统一文案，避免各写一套）——

export const POST_VISIBILITY_LABEL: Record<string, string> = {
  PUBLIC: '公开',
  PRIVATE: '私密',
}

export const POST_STATUS_LABEL: Record<string, string> = {
  PENDING: '待审核',
  VISIBLE: '可见',
  HIDDEN: '已隐藏',
  DELETED: '已删除',
}

export const COMMENT_STATUS_LABEL: Record<string, string> = {
  VISIBLE: '可见',
  HIDDEN: '已隐藏',
  DELETED: '已删除',
}

export const REPORT_STATUS_LABEL: Record<string, string> = {
  OPEN: '待处理',
  PROCESSING: '处理中',
  RESOLVED: '已解决',
  REJECTED: '已驳回',
}

/** 默认分页大小 */
export const DEFAULT_PAGE_SIZE = 10
