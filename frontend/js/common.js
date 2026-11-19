/* 前后端分离前端公共脚本：Vue 3 + Axios */
(function () {
    'use strict';

    // 前端与后端分开部署时，默认访问 8081 的 Spring Boot 接口。
    // 如需修改，在引入本文件之前设置 window.LIBRARY_API_BASE。
    const pageHost = location.hostname || '127.0.0.1';
    const API_BASE = window.LIBRARY_API_BASE
        || (location.port === '8081'
            ? location.origin + '/api'
            : location.protocol + '//' + pageHost + ':8081/api');

    const http = axios.create({
        baseURL: API_BASE,
        timeout: 20000,
        withCredentials: true
    });

    http.interceptors.response.use(
        function (response) {
            const body = response.data;
            if (body && typeof body === 'object' && 'code' in body) {
                if (body.code === 0) {
                    return body;
                }
                const error = new Error(body.message || '请求失败');
                error.business = body;
                return Promise.reject(error);
            }
            return body;
        },
        function (error) {
            const status = error.response && error.response.status;
            const body = error.response && error.response.data;
            if (status === 401) {
                if (!/login\.html$/.test(location.pathname)) {
                    toast('登录已失效，请重新登录', 'warn');
                    setTimeout(function () { location.href = 'login.html'; }, 600);
                }
            } else if (status === 403) {
                toast((body && body.message) || '没有权限执行此操作', 'error');
            } else if (!error.response) {
                toast('无法连接后端服务，请确认 Spring Boot 已在 8081 端口启动', 'error');
            }
            return Promise.reject(error);
        }
    );

    function toast(message, type) {
        let box = document.getElementById('toast-box');
        if (!box) {
            box = document.createElement('div');
            box.id = 'toast-box';
            document.body.appendChild(box);
        }
        const item = document.createElement('div');
        item.className = 'toast ' + (type || 'success');
        item.textContent = message;
        box.appendChild(item);
        setTimeout(function () {
            item.classList.add('toast-out');
            setTimeout(function () { item.remove(); }, 260);
        }, 2800);
    }

    function errorMessage(error) {
        if (!error) return '操作失败';
        if (error.business && error.business.message) return error.business.message;
        if (error.response && error.response.data && error.response.data.message) {
            return error.response.data.message;
        }
        return error.message || '操作失败';
    }

    function fmt(value) {
        if (!value) return '-';
        return String(value).replace('T', ' ').slice(0, 19);
    }

    function fmtDate(value) {
        if (!value) return '-';
        return String(value).replace('T', ' ').slice(0, 10);
    }

    function parseTime(value) {
        if (!value) return NaN;
        return new Date(String(value).replace(' ', 'T')).getTime();
    }

    function isOverdue(loan) {
        if (!loan || loan.returnDate) return false;
        const due = parseTime(loan.dueDate);
        return !Number.isNaN(due) && due < Date.now();
    }

    function loanStatus(loan) {
        if (!loan) return '-';
        if (loan.returnDate || loan.status === '已还') return '已还';
        return isOverdue(loan) ? '已逾期' : '未还';
    }

    function statusClass(status) {
        const value = status || '';
        if (['正常', '已还', '已通过', '可借', '已完成'].includes(value)) return 'badge-success';
        if (['未还', '等待中'].includes(value)) return 'badge-info';
        if (['已逾期', '超期', '已驳回', '已废弃', '已过期', '已取消'].includes(value)) return 'badge-danger';
        if (value === '待审核') return 'badge-warn';
        return 'badge-muted';
    }

    function roleClass(role) {
        if (role === '管理员') return 'role-admin';
        if (role === '操作员') return 'role-operator';
        if (role === '教师') return 'role-teacher';
        return 'role-student';
    }

    function can(me, permission) {
        return !!(me && Array.isArray(me.permissions) && me.permissions.includes(permission));
    }

    const NAV = [
        { href: 'home.html', label: '首页', icon: '🏠' },
        { href: 'users.html', label: '用户管理', icon: '👥', permission: '用户信息查询' },
        { href: 'books.html', label: '图书管理', icon: '📚', any: ['查看所有书籍', '添加书籍', '编辑图书', '废弃图书', '图书副本编辑'] },
        { href: 'borrow.html', label: '借书', icon: '📖', permission: '借书' },
        { href: 'return.html', label: '还书', icon: '↩️', permission: '还书' },
        { href: 'my-loans.html', label: '我的借阅', icon: '📋', permission: '个人借阅查询' },
        { href: 'loan-query.html', label: '借阅查询', icon: '🔎', permission: '借阅记录查询' },
        { href: 'reservations.html', label: '预约管理', icon: '⏳', any: ['预约图书', '预约管理'] },
        { href: 'recommend.html', label: '推荐图书', icon: '💡', permission: '推荐图书' },
        { href: 'review.html', label: '审阅推荐', icon: '✅', permission: '审阅推荐图书' },
        { href: 'stats.html', label: '数据统计', icon: '📊', permission: '借阅数据统计' },
        { href: 'notifications.html', label: '消息通知', icon: '🔔' },
        { href: 'logs.html', label: '操作日志', icon: '🧾', permission: '操作日志查询' }
    ];

    function renderSidebar(me, active) {
        const sidebar = document.getElementById('sidebar');
        if (!sidebar) return;
        const items = NAV.filter(function (item) {
            if (item.any) return item.any.some(function (p) { return can(me, p); });
            if (item.permission) return can(me, item.permission);
            return true;
        });
        sidebar.innerHTML = items.map(function (item) {
            const isActive = item.label === active ? ' active' : '';
            return '<a class="nav-item' + isActive + '" href="' + item.href + '">'
                + '<span class="nav-icon">' + item.icon + '</span><span>' + item.label + '</span></a>';
        }).join('');
    }

    async function me() {
        const body = await http.get('/auth/me');
        return body.data;
    }

    async function requireMe() {
        const user = await me();
        if (!user) {
            location.href = 'login.html';
            throw new Error('未登录');
        }
        return user;
    }

    async function initPage(active) {
        const user = await requireMe();
        // 页面在 await initPage 之后才把 me 赋给 Vue，顶栏与侧栏的 v-if 此时刚具备渲染条件。
        // 因此等 Vue 完成一次 DOM 更新后再注入导航，避免 #sidebar 尚不存在导致侧栏空白。
        let tries = 0;
        const applyChrome = function () {
            const sidebar = document.getElementById('sidebar');
            if (sidebar) {
                renderSidebar(user, active);
                refreshUnreadCount();
                return;
            }
            if (tries++ < 50) setTimeout(applyChrome, 20);
        };
        setTimeout(applyChrome, 0);
        return user;
    }

    async function refreshUnreadCount() {
        const badge = document.getElementById('notif-count');
        if (!badge) return;
        try {
            const body = await http.get('/notifications/unread-count');
            const count = body.data || 0;
            badge.textContent = count > 99 ? '99+' : String(count);
            badge.style.display = count > 0 ? 'inline-flex' : 'none';
        } catch (e) {
            badge.style.display = 'none';
        }
    }

    async function logout() {
        try { await http.post('/auth/logout'); } catch (e) { /* 忽略退出失败 */ }
        location.href = 'login.html';
    }

    function get(url, params) {
        return http.get(url, { params: params || {} });
    }

    function post(url, data) {
        return http.post(url, data || {});
    }

    function put(url, data) {
        return http.put(url, data || {});
    }

    function del(url) {
        return http.delete(url);
    }

    function upload(url, formData) {
        return http.post(url, formData, { headers: { 'Content-Type': 'multipart/form-data' } });
    }

    function today() {
        const d = new Date();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return d.getFullYear() + '-' + m + '-' + day;
    }

    function fileUrl(url) {
        if (!url) return '';
        if (/^https?:\/\//i.test(url)) return url;
        const origin = API_BASE.replace(/\/api\/?$/, '');
        return origin + (url.charAt(0) === '/' ? url : '/' + url);
    }

    function monthStart() {
        const d = new Date();
        return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-01';
    }

    const FEATURES = {
        '用户信息查询': { icon: '👥', title: '用户查询', desc: '按姓名、手机号或专业查找用户', href: 'users.html' },
        '查看所有书籍': { icon: '📚', title: '馆藏图书', desc: '浏览全部图书与库存', href: 'books.html' },
        '添加书籍': { icon: '➕', title: '添加图书', desc: '录入新书并生成副本', href: 'books.html?action=add' },
        '编辑图书': { icon: '✏️', title: '编辑图书', desc: '维护图书资料', href: 'books.html' },
        '废弃图书': { icon: '🗑️', title: '废弃图书', desc: '处理不再流通的图书', href: 'books.html' },
        '图书副本编辑': { icon: '🧩', title: '副本管理', desc: '增删馆藏副本', href: 'books.html' },
        '借书': { icon: '📖', title: '借书', desc: '从可借图书中选择借阅', href: 'borrow.html' },
        '还书': { icon: '↩️', title: '还书', desc: '归还当前借阅的副本', href: 'return.html' },
        '个人借阅查询': { icon: '📋', title: '我的借阅', desc: '查看自己的借阅历史与状态', href: 'my-loans.html' },
        '借阅记录查询': { icon: '🔎', title: '借阅查询', desc: '按读者、书名、日期或逾期筛选', href: 'loan-query.html' },
        '借阅数据统计': { icon: '📊', title: '数据统计', desc: '按图书类型和专业统计借阅', href: 'stats.html' },
        '推荐图书': { icon: '💡', title: '推荐图书', desc: '推荐馆藏缺少的图书', href: 'recommend.html' },
        '审阅推荐图书': { icon: '✅', title: '审阅推荐', desc: '审核教师提交的图书推荐', href: 'review.html' },
        '预约图书': { icon: '⏳', title: '预约图书', desc: '库存为零时预约，可借后通知', href: 'reservations.html' },
        '预约管理': { icon: '⏳', title: '预约管理', desc: '查看和处理全部预约', href: 'reservations.html' },
        '操作日志查询': { icon: '🧾', title: '操作日志', desc: '查看 AOP 记录的接口操作日志', href: 'logs.html' }
    };

    function features(me) {
        const seen = {};
        const result = [];
        (me && me.permissions ? me.permissions : []).forEach(function (permission) {
            const feature = FEATURES[permission];
            if (feature && !seen[feature.href]) {
                seen[feature.href] = true;
                result.push(feature);
            }
        });
        return result;
    }

    window.Lib = {
        API_BASE: API_BASE,
        http: http,
        get: get,
        post: post,
        put: put,
        del: del,
        upload: upload,
        toast: toast,
        errorMessage: errorMessage,
        fmt: fmt,
        fmtDate: fmtDate,
        parseTime: parseTime,
        isOverdue: isOverdue,
        loanStatus: loanStatus,
        statusClass: statusClass,
        roleClass: roleClass,
        can: can,
        me: me,
        requireMe: requireMe,
        initPage: initPage,
        refreshUnreadCount: refreshUnreadCount,
        logout: logout,
        today: today,
        monthStart: monthStart,
        fileUrl: fileUrl,
        features: features
    };
})();