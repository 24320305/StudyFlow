import { request } from './client'
import type { UploadResult } from './types'

/** 上传文件（multipart/form-data 的 file 字段），返回 fileId 与 url */
export function uploadImage(file: File): Promise<UploadResult> {
  const form = new FormData()
  form.append('file', file)
  return request.post('/uploads', form)
}

/** 删除上传文件 */
export function deleteUpload(fileId: string): Promise<void> {
  return request.delete(`/uploads/${fileId}`)
}
