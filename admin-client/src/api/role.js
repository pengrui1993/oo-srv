import request from '@/utils/request'

export function getRoutes() {
  return request({
    url: '/vue-element-admin/routes',
    method: 'get'
  })
}

export function getRoles() {
  return request({
    url: '/vue-element-admin/roles',
    method: 'get'
  })
}

export function addRole(data) {
  return request({
    url: '/vue-element-admin/role/add',
    method: 'post',
    data
  })
}

export function updateRole(id, data) {
  const v = {}
  v.data = data
  v.role = id
  data = v
  return request({
    url: `/vue-element-admin/role/update`,
    method: 'put',
    data
  })
}

export function deleteRole(id) {
  const data = {}
  data.role = id
  return request({
    url: `/vue-element-admin/role/delete`,
    method: 'delete',
    data
  })
}
