import { request } from './client'
import type {
  CommunityComment,
  CommunityPost,
  FollowState,
  LikeState,
  Page,
  PostVisibility,
} from './types'

export interface PostListParams {
  keyword?: string
  page?: number
  pageSize?: number
}

export interface PublishPostPayload {
  checkInId: number
  content: string
  visibility: PostVisibility
}

export interface UpdatePostPayload {
  content?: string
  visibility?: PostVisibility
}

export interface CreateCommentPayload {
  content: string
}

export function listPosts(params?: PostListParams): Promise<Page<CommunityPost>> {
  return request.get('/posts', { params })
}

export function searchPosts(params: Required<Pick<PostListParams, 'keyword'>> & PostListParams): Promise<Page<CommunityPost>> {
  return request.get('/posts/search', { params })
}

export function listMyPosts(params?: Pick<PostListParams, 'page' | 'pageSize'>): Promise<Page<CommunityPost>> {
  return request.get('/posts/mine', { params })
}

export function getPost(id: number): Promise<CommunityPost> {
  return request.get(`/posts/${id}`)
}

export function publishPost(payload: PublishPostPayload): Promise<CommunityPost> {
  return request.post('/posts', payload)
}

export function updatePost(id: number, payload: UpdatePostPayload): Promise<CommunityPost> {
  return request.patch(`/posts/${id}`, payload)
}

export function deletePost(id: number): Promise<void> {
  return request.delete(`/posts/${id}`)
}

export function likePost(id: number): Promise<LikeState> {
  return request.post(`/posts/${id}/likes`)
}

export function unlikePost(id: number): Promise<LikeState> {
  return request.delete(`/posts/${id}/likes`)
}

export function listComments(postId: number): Promise<CommunityComment[]> {
  return request.get(`/posts/${postId}/comments`)
}

export function addComment(postId: number, payload: CreateCommentPayload): Promise<CommunityComment> {
  return request.post(`/posts/${postId}/comments`, payload)
}

export function deleteComment(id: number): Promise<void> {
  return request.delete(`/comments/${id}`)
}

export function followUser(id: number): Promise<FollowState> {
  return request.post(`/users/${id}/follow`)
}

export function unfollowUser(id: number): Promise<FollowState> {
  return request.delete(`/users/${id}/follow`)
}
