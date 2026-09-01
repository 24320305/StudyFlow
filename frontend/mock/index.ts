// 本地 mock 服务端（Vite 插件）。
//
// 用途：后端尚未就绪时，让前端按《五人分工统一执行方案》的统一契约独立跑通，
//       注册 / 登录 / 计划 / 打卡 / 统计 / 上传 全部可用，数据保存在内存中。
// 切换真实后端：在 .env 中设置 VITE_USE_MOCK=false，前端即直连 http://localhost:8080。
//
// 统一响应结构：{ code, message, data, requestId }
// HTTP 语义：401 未登录/凭证无效，403 无权限/账号受限，404 不存在/不可见，
//            409 重复/非法状态流转，413 文件过大，415 类型不支持。
import { randomUUID } from 'node:crypto'
import type { IncomingMessage, ServerResponse } from 'node:http'
import type { Plugin } from 'vite'

// ---------- 内存数据表 ----------

interface MockUser {
  id: number
  email: string
  password: string
  nickname: string
  avatarUrl: string | null
  role: string
  status: string
}

interface MockPlan {
  id: number
  userId: number
  name: string
  startDate: string
  endDate: string
  dailyTarget: number
  status: string
}

interface MockCheckin {
  id: number
  userId: number
  planId: number
  checkDate: string
  durationMinutes: number
  completed: boolean
  note: string | null
  imageUrl: string | null
}

let userIdSeq = 1
let planIdSeq = 1
let checkinIdSeq = 1
let uploadSeq = 1

const users: MockUser[] = []
const plans: MockPlan[] = []
const checkins: MockCheckin[] = []

// ---------- 日期工具（本地时区，展示按 Asia/Shanghai） ----------

function toDateStr(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function todayStr(): string {
  return toDateStr(new Date())
}

function addDays(dateStr: string, n: number): string {
  const d = new Date(`${dateStr}T00:00:00`)
  d.setDate(d.getDate() + n)
  return toDateStr(d)
}

function daysInclusive(a: string, b: string): number {
  const d1 = new Date(`${a}T00:00:00`).getTime()
  const d2 = new Date(`${b}T00:00:00`).getTime()
  return Math.floor((d2 - d1) / 86400000) + 1
}

// ---------- 种子数据（便于演示与截图） ----------

function seed(): void {
  const today = todayStr()
  const demo = {
    id: userIdSeq++,
    email: 'demo@studyflow.com',
    password: '123456',
    nickname: '演示同学',
    avatarUrl: null,
    role: 'USER',
    status: 'NORMAL',
  }
  users.push(demo)

  // 一个进行中的计划（含最近 5 天连续打卡）
  const activePlan: MockPlan = {
    id: planIdSeq++,
    userId: demo.id,
    name: '考研英语冲刺',
    startDate: addDays(today, -6),
    endDate: addDays(today, 20),
    dailyTarget: 120,
    status: 'ACTIVE',
  }
  plans.push(activePlan)

  const durations = [110, 120, 95, 120, 100]
  for (let i = 0; i < 5; i++) {
    checkins.push({
      id: checkinIdSeq++,
      userId: demo.id,
      planId: activePlan.id,
      checkDate: addDays(today, -i),
      durationMinutes: durations[i],
      completed: true,
      note: i === 0 ? '今天背了 60 个单词' : null,
      imageUrl: null,
    })
  }

  // 一个已完成的计划
  const donePlan: MockPlan = {
    id: planIdSeq++,
    userId: demo.id,
    name: '数据结构刷题',
    startDate: addDays(today, -12),
    endDate: addDays(today, -3),
    dailyTarget: 60,
    status: 'COMPLETED',
  }
  plans.push(donePlan)
  checkins.push({
    id: checkinIdSeq++,
    userId: demo.id,
    planId: donePlan.id,
    checkDate: addDays(today, -10),
    durationMinutes: 75,
    completed: true,
    note: '二叉树专题',
    imageUrl: null,
  })
}

seed()

// ---------- 响应工具 ----------

function send(res: ServerResponse, status: number, body: unknown): void {
  res.statusCode = status
  res.setHeader('Content-Type', 'application/json; charset=utf-8')
  res.end(JSON.stringify(body))
}

function ok(res: ServerResponse, data: unknown, status = 200): void {
  send(res, status, {
    code: 'OK',
    message: 'success',
    data,
    requestId: randomUUID(),
  })
}

function fail(res: ServerResponse, status: number, message: string): void {
  send(res, status, {
    code: 'ERROR',
    message,
    data: null,
    requestId: randomUUID(),
  })
}

// ---------- 请求工具 ----------

function readBody(req: IncomingMessage): Promise<Record<string, unknown>> {
  return new Promise((resolve) => {
    let raw = ''
    req.on('data', (chunk) => {
      raw += chunk
    })
    req.on('end', () => {
      if (!raw) {
        resolve({})
        return
      }
      try {
        resolve(JSON.parse(raw) as Record<string, unknown>)
      } catch {
        resolve({})
      }
    })
  })
}

function currentUser(req: IncomingMessage): MockUser | null {
  const auth = req.headers.authorization ?? ''
  const m = /^Bearer mock-token-(\d+)$/.exec(auth)
  if (!m) return null
  const id = Number(m[1])
  return users.find((u) => u.id === id) ?? null
}

function publicUser(u: MockUser): Record<string, unknown> {
  return {
    id: u.id,
    email: u.email,
    nickname: u.nickname,
    avatarUrl: u.avatarUrl,
    role: u.role,
    status: u.status,
  }
}

function publicPlan(p: MockPlan): Record<string, unknown> {
  return { ...p }
}

function publicCheckin(c: MockCheckin): Record<string, unknown> {
  return { ...c }
}

function toNum(v: string | null | undefined): number | null {
  if (v === null || v === undefined || v === '') return null
  const n = Number(v)
  return Number.isNaN(n) ? null : n
}

// ---------- 路由处理 ----------

async function handle(req: IncomingMessage, res: ServerResponse, pathWithQuery: string): Promise<void> {
  const method = req.method ?? 'GET'
  const [pathname, queryStr] = pathWithQuery.split('?')
  const seg = pathname.split('/').filter(Boolean)
  const query = new URLSearchParams(queryStr ?? '')

  // ---- 认证 ----
  if (seg[0] === 'auth' && seg[1] === 'register' && method === 'POST') {
    const body = await readBody(req)
    const email = String(body.email ?? '').trim()
    const password = String(body.password ?? '')
    const nickname = String(body.nickname ?? '').trim()
    if (!email || !password) return fail(res, 400, '邮箱和密码不能为空')
    if (!nickname) return fail(res, 400, '昵称不能为空')
    if (users.some((u) => u.email === email)) return fail(res, 409, '邮箱已注册')
    const u: MockUser = {
      id: userIdSeq++,
      email,
      password,
      nickname,
      avatarUrl: null,
      role: 'USER', // 注册接口只创建 USER，忽略请求体中的 role
      status: 'NORMAL',
    }
    users.push(u)
    return ok(res, { token: `mock-token-${u.id}`, user: publicUser(u) }, 201)
  }

  if (seg[0] === 'auth' && seg[1] === 'login' && method === 'POST') {
    const body = await readBody(req)
    const email = String(body.email ?? '').trim()
    const password = String(body.password ?? '')
    const u = users.find((x) => x.email === email)
    if (!u || u.password !== password) return fail(res, 401, '邮箱或密码错误')
    if (u.status === 'DISABLED') return fail(res, 403, '账号已被禁用')
    return ok(res, { token: `mock-token-${u.id}`, user: publicUser(u) })
  }

  if (seg[0] === 'auth' && seg[1] === 'logout' && method === 'POST') {
    return ok(res, null)
  }

  if (seg[0] === 'me') {
    const u = currentUser(req)
    if (!u) return fail(res, 401, '未登录或凭证无效')
    if (method === 'GET') return ok(res, publicUser(u))
    if (method === 'PATCH') {
      const body = await readBody(req)
      if (typeof body.nickname === 'string' && body.nickname.trim()) {
        u.nickname = body.nickname.trim()
      }
      if ('avatarUrl' in body) {
        u.avatarUrl = body.avatarUrl === null ? null : String(body.avatarUrl)
      }
      return ok(res, publicUser(u))
    }
    return fail(res, 405, '方法不支持')
  }

  // ---- 上传 ----
  if (seg[0] === 'uploads' && method === 'POST') {
    const u = currentUser(req)
    if (!u) return fail(res, 401, '未登录或凭证无效')
    // mock 不解析 multipart 文件内容，仅返回稳定的 fileId 与占位 url。
    const fileId = `mock-upload-${uploadSeq++}`
    return ok(res, { fileId, url: `/mock/uploads/${fileId}` }, 201)
  }
  if (seg[0] === 'uploads' && seg[1] && method === 'DELETE') {
    const u = currentUser(req)
    if (!u) return fail(res, 401, '未登录或凭证无效')
    return ok(res, null)
  }

  // ---- 统计 ----
  if (seg[0] === 'statistics' && method === 'GET') {
    const u = currentUser(req)
    if (!u) return fail(res, 401, '未登录或凭证无效')
    const planId = toNum(query.get('planId'))
    const startDate = query.get('startDate') ?? null
    const endDate = query.get('endDate') ?? null
    const today = todayStr()

    let scope: MockCheckin[] = checkins.filter((c) => c.userId === u.id)
    if (planId !== null) {
      scope = scope.filter((c) => c.planId === planId)
    }
    if (startDate) scope = scope.filter((c) => c.checkDate >= startDate)
    if (endDate) scope = scope.filter((c) => c.checkDate <= endDate)

    const totalDurationMinutes = scope.reduce((s, c) => s + c.durationMinutes, 0)
    const completedScope = scope.filter((c) => c.completed)

    let completedDays = completedScope.length
    let totalDays = 0
    let completionRate: number | null = null
    let streakDays = 0

    if (planId !== null) {
      const plan = plans.find((p) => p.id === planId && p.userId === u.id)
      if (!plan) return fail(res, 404, '计划不存在或不可见')
      const lo = startDate && startDate > plan.startDate ? startDate : plan.startDate
      const hiCandidates = [endDate, plan.endDate, today].filter((x): x is string => !!x)
      const hi = hiCandidates.sort()[0] ?? today
      totalDays = daysInclusive(lo, hi)
      if (totalDays > 0) {
        completionRate = Math.round((completedDays / totalDays) * 10000) / 100
      }
      // 连续天数：从统计区间末尾向前连续 completed=true 的 checkDate
      const completedDates = new Set(completedScope.map((c) => c.checkDate))
      let cursor = hi
      while (completedDates.has(cursor)) {
        streakDays += 1
        cursor = addDays(cursor, -1)
      }
    }

    return ok(res, {
      totalDurationMinutes,
      completionRate,
      streakDays,
      completedDays,
      totalDays,
      checkinCount: scope.length,
    })
  }

  // ---- 计划 ----
  if (seg[0] === 'plans' && !seg[1]) {
    const u = currentUser(req)
    if (!u) return fail(res, 401, '未登录或凭证无效')

    if (method === 'GET') {
      const page = toNum(query.get('page')) ?? 1
      const pageSize = toNum(query.get('pageSize')) ?? 10
      const status = query.get('status')
      let list = plans.filter((p) => p.userId === u.id)
      if (status) list = list.filter((p) => p.status === status)
      list = [...list].sort((a, b) => b.id - a.id)
      const total = list.length
      const start = (page - 1) * pageSize
      const items = list.slice(start, start + pageSize).map(publicPlan)
      return ok(res, { items, page, pageSize, total })
    }

    if (method === 'POST') {
      const body = await readBody(req)
      const name = String(body.name ?? '').trim()
      const startDate = String(body.startDate ?? '')
      const endDate = String(body.endDate ?? '')
      const dailyTarget = Number(body.dailyTarget ?? 0)
      if (!name) return fail(res, 400, '计划名称不能为空')
      if (!startDate || !endDate) return fail(res, 400, '开始和结束日期不能为空')
      if (endDate < startDate) return fail(res, 400, '结束日期不得早于开始日期')
      if (!(dailyTarget > 0)) return fail(res, 400, '每日目标必须是正整数分钟')
      const p: MockPlan = {
        id: planIdSeq++,
        userId: u.id,
        name,
        startDate,
        endDate,
        dailyTarget,
        status: body.status === 'PAUSED' ? 'PAUSED' : 'ACTIVE',
      }
      plans.push(p)
      return ok(res, publicPlan(p), 201)
    }
    return fail(res, 405, '方法不支持')
  }

  if (seg[0] === 'plans' && seg[1] && !seg[2]) {
    const u = currentUser(req)
    if (!u) return fail(res, 401, '未登录或凭证无效')
    const id = toNum(seg[1])
    if (id === null) return fail(res, 400, '非法计划 id')
    const plan = plans.find((p) => p.id === id && p.userId === u.id)
    if (!plan) return fail(res, 404, '计划不存在或不可见')

    if (method === 'GET') return ok(res, publicPlan(plan))
    if (method === 'PATCH') {
      const body = await readBody(req)
      if (typeof body.name === 'string' && body.name.trim()) plan.name = body.name.trim()
      if (typeof body.startDate === 'string' && body.startDate) plan.startDate = body.startDate
      if (typeof body.endDate === 'string' && body.endDate) plan.endDate = body.endDate
      if (typeof body.dailyTarget === 'number' && body.dailyTarget > 0) {
        plan.dailyTarget = body.dailyTarget
      }
      if (typeof body.status === 'string' && ['ACTIVE', 'PAUSED', 'COMPLETED'].includes(body.status)) {
        plan.status = body.status
      }
      if (plan.endDate < plan.startDate) return fail(res, 400, '结束日期不得早于开始日期')
      return ok(res, publicPlan(plan))
    }
    if (method === 'DELETE') {
      const idx = plans.indexOf(plan)
      plans.splice(idx, 1)
      for (let i = checkins.length - 1; i >= 0; i--) {
        if (checkins[i].planId === plan.id) checkins.splice(i, 1)
      }
      return ok(res, null)
    }
    return fail(res, 405, '方法不支持')
  }

  // ---- 打卡 ----
  if (seg[0] === 'plans' && seg[1] && seg[2] === 'check-ins') {
    const u = currentUser(req)
    if (!u) return fail(res, 401, '未登录或凭证无效')
    const id = toNum(seg[1])
    if (id === null) return fail(res, 400, '非法计划 id')
    const plan = plans.find((p) => p.id === id && p.userId === u.id)
    if (!plan) return fail(res, 404, '计划不存在或不可见')

    if (method === 'GET') {
      const startDate = query.get('startDate')
      const endDate = query.get('endDate')
      let list = checkins.filter((c) => c.planId === plan.id)
      if (startDate) list = list.filter((c) => c.checkDate >= startDate)
      if (endDate) list = list.filter((c) => c.checkDate <= endDate)
      list = [...list].sort((a, b) => a.checkDate.localeCompare(b.checkDate))
      return ok(res, list.map(publicCheckin))
    }

    // PUT /plans/{id}/check-ins/{date}
    if (method === 'PUT' && seg[3]) {
      const date = seg[3]
      if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) return fail(res, 400, '日期格式应为 YYYY-MM-DD')
      if (plan.status !== 'ACTIVE') return fail(res, 409, '只有进行中的计划才能打卡')
      if (date < plan.startDate || date > plan.endDate) {
        return fail(res, 409, '打卡日期不在计划有效期内')
      }
      const body = await readBody(req)
      const durationMinutes = Number(body.durationMinutes ?? 0)
      const completed = Boolean(body.completed)
      if (!(durationMinutes >= 0)) return fail(res, 400, '学习时长不能为负数')
      const note = body.note == null ? null : String(body.note)
      const imageUrl = body.imageUrl == null ? null : String(body.imageUrl)

      const existing = checkins.find((c) => c.planId === plan.id && c.checkDate === date)
      if (existing) {
        existing.durationMinutes = durationMinutes
        existing.completed = completed
        existing.note = note
        existing.imageUrl = imageUrl
        return ok(res, publicCheckin(existing))
      }
      const c: MockCheckin = {
        id: checkinIdSeq++,
        userId: u.id,
        planId: plan.id,
        checkDate: date,
        durationMinutes,
        completed,
        note,
        imageUrl,
      }
      checkins.push(c)
      return ok(res, publicCheckin(c), 201)
    }
    return fail(res, 405, '方法不支持')
  }

  return fail(res, 404, '接口不存在')
}

// ---------- Vite 插件 ----------

interface MockPluginOptions {
  enabled: boolean
}

export default function mockPlugin(options: MockPluginOptions): Plugin {
  return {
    name: 'studyflow-mock',
    configureServer(server) {
      if (!options.enabled) return
      server.middlewares.use((req, res, next) => {
        const url = req.url ?? ''
        if (!url.startsWith('/api')) {
          next()
          return
        }
        void handle(
          req as unknown as IncomingMessage,
          res as unknown as ServerResponse,
          url.slice('/api'.length),
        ).catch(() => {
          if (!res.writableEnded) fail(res as unknown as ServerResponse, 500, '服务端异常')
        })
      })
    },
  }
}
