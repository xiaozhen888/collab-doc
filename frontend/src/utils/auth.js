//获取当前用户ID
export const getCurrentUserId = () => {
     return sessionStorage.getItem('userId')
}

//获取token
export const getToken = () => {
    return sessionStorage.getItem('token')
}

//是否已登录
export const isAuthenticated = () => {
    return !!getToken()
}

//登出
export const logout = () => {
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('userId')
    window.location.href = '/login'
}