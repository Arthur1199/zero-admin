package com.zero.system.service;

import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;
import com.zero.common.annotation.Log;
import com.zero.common.annotation.RequiresPermissions;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.exception.ServiceException;
import com.zero.system.domain.*;
import com.zero.system.mapper.SysMemoMapper;
import com.zero.system.service.impl.SysMemoServiceImpl;
import com.zero.web.controller.systemTools.SysMemoController;
import jakarta.validation.Validation;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17 21:20
 * 说明: 备忘录加密、用户隔离、并发版本、日志及MyBatis绑定回归；不连接数据库、Redis或Nacos
 */
class SysMemoTest
{
    /** 测试专用固定密钥，仅用于隔离测试。 */
    private final String testKey = Base64.getEncoder().encodeToString(new byte[32]);
    /** Mapper替身，不访问数据库。 */
    private final SysMemoMapper mapper = mock(SysMemoMapper.class);
    /** 实际加密组件，测试随机数及完整性。 */
    private final MemoContentCipher cipher = new MemoContentCipher(testKey);
    /** 标准参数校验器工厂，测试后释放。 */
    private final jakarta.validation.ValidatorFactory validation = Validation.buildDefaultValidatorFactory();
    /** 待测服务。 */
    private final SysMemoServiceImpl service = new SysMemoServiceImpl(mapper, cipher, validation.getValidator());

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 建立超级管理员登录态，供全量访问及普通账号切换测试使用
     * @return
     * 返回值: void
     * 返回值说明: 测试用户ID为1
     */
    @BeforeEach
    void login()
    {
        loginAs(1L);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 切换真实登录主体，管理员判断仅以项目用户ID规则为准
     * @param userId 当前测试用户ID，1为超级管理员，其它为普通账号
     * @return
     * 返回值: void
     * 返回值说明: 替换线程内登录态，不连接外部服务
     */
    private void loginAs(Long userId)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setUserName("memo-test");
        LoginUser login = new LoginUser();
        login.setUserId(userId);
        login.setUser(user);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login, null, List.of()));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 释放线程登录上下文及校验器
     * @return
     * 返回值: void
     * 返回值说明: 不污染其它测试
     */
    @AfterEach
    void cleanup()
    {
        SecurityContextHolder.clearContext();
        validation.close();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 同一正文每次密文不同，保留换行空格；篡改、跨用户及错误密钥均拒绝
     * @return
     * 返回值: void
     * 返回值说明: 验证AES-GCM完整性和随机数
     */
    @Test
    void cipherPreservesTextAndRejectsTampering()
    {
        String text = "  账号：demo\n密码： test-password  \n<literal>&";
        String encrypted = cipher.encrypt(text, 1L);
        assertThat(encrypted).startsWith("v1:").doesNotContain("demo", "test-password");
        assertThat(cipher.encrypt(text, 1L)).isNotEqualTo(encrypted);
        assertThat(cipher.decrypt(encrypted, 1L)).isEqualTo(text);
        assertThatThrownBy(() -> cipher.decrypt(encrypted, 2L)).isInstanceOf(ServiceException.class);
        byte[] payload = Base64.getDecoder().decode(encrypted.substring(3));
        payload[payload.length - 1] ^= 1;
        assertThatThrownBy(() -> cipher.decrypt("v1:" + Base64.getEncoder().encodeToString(payload), 1L))
                .isInstanceOf(ServiceException.class);
        byte[] differentKey = new byte[32];
        differentKey[0] = 1;
        assertThatThrownBy(() -> new MemoContentCipher(Base64.getEncoder().encodeToString(differentKey)).decrypt(encrypted, 1L))
                .isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> cipher.decrypt("plain text", 1L)).isInstanceOf(ServiceException.class);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 密钥缺失或长度非法时不允许保存正文，空正文也不能绕过配置检查
     * @return
     * 返回值: void
     * 返回值说明: 不产生默认密钥或明文回退
     */
    @Test
    void missingKeyFailsClosed()
    {
        for (String key : List.of("", "bad-key", Base64.getEncoder().encodeToString(new byte[16])))
        {
            var invalid = new MemoContentCipher(key);
            assertThatThrownBy(() -> invalid.encrypt("", 1L)).isInstanceOf(ServiceException.class);
            assertThatThrownBy(() -> invalid.decrypt(null, 1L)).isInstanceOf(ServiceException.class);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 新增只写密文并使用登录人，不把正文写进实体JSON
     * @return
     * 返回值: void
     * 返回值说明: 校验入库和序列化边界
     */
    @Test
    void createsEncryptedContentWithAuthenticatedOwner() throws Exception
    {
        var request = request();
        when(mapper.insertMemo(any())).thenReturn(1);
        assertThat(service.insertMemo(request)).isEqualTo(1);
        var capture = ArgumentCaptor.forClass(SysMemo.class);
        verify(mapper).insertMemo(capture.capture());
        SysMemo memo = capture.getValue();
        assertThat(memo.getOwnerUserId()).isEqualTo(1L);
        assertThat(memo.getCreateBy()).isEqualTo("memo-test");
        assertThat(memo.getMemoContent()).isNull();
        assertThat(cipher.decrypt(memo.getContentCipher(), 1L)).isEqualTo(request.getMemoContent());
        assertThat(JsonMapper.builder().build().writeValueAsString(memo)).doesNotContain("contentCipher", "test-password");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 超级管理员可读取他人私有正文，仍使用原创建用户ID解密
     * @return
     * 返回值: void
     * 返回值说明: 不存在和不可见统一处理
     */
    @Test
    void adminReadsPrivateDetailUsingOriginalOwner()
    {
        assertThatThrownBy(() -> service.selectMemoById(9L)).isInstanceOf(ServiceException.class)
                .hasMessage("备忘录不存在或无权查看");
        verify(mapper).selectVisibleMemo(9L, 1L, true);
        SysMemo visible = new SysMemo();
        visible.setMemoId(10L);
        visible.setOwnerUserId(2L);
        visible.setContentCipher(cipher.encrypt("public test", 2L));
        visible.setVisibility("0");
        when(mapper.selectVisibleMemo(10L, 1L, true)).thenReturn(visible);
        assertThat(service.selectMemoById(10L).getMemoContent()).isEqualTo("public test");
        assertThat(visible.getContentCipher()).isNull();
        assertThat(visible.isCanManage()).isTrue();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 普通账号修改删除只携带当前用户ID，零行影响统一拒绝且要求版本
     * @return
     * 返回值: void
     * 返回值说明: 普通账号仍只允许维护本人记录
     */
    @Test
    void writesRequireOwnerAndCurrentVersion()
    {
        loginAs(3L);
        var request = request();
        request.setMemoId(9L);
        assertThatThrownBy(() -> service.updateMemo(request)).isInstanceOf(ServiceException.class);
        verifyNoInteractions(mapper);
        request.setVersion(2L);
        SysMemo original = new SysMemo();
        original.setOwnerUserId(3L);
        when(mapper.selectVisibleMemo(9L, 3L, false)).thenReturn(original);
        assertThatThrownBy(() -> service.updateMemo(request)).isInstanceOf(ServiceException.class);
        var capture = ArgumentCaptor.forClass(SysMemo.class);
        verify(mapper).updateMemo(capture.capture(), eq(3L), eq(false));
        assertThat(capture.getValue().getOwnerUserId()).isEqualTo(3L);
        assertThat(capture.getValue().getVersion()).isEqualTo(2L);
        assertThatThrownBy(() -> service.deleteMemo(9L, 2L)).isInstanceOf(ServiceException.class);
        verify(mapper).deleteMemo(9L, 3L, 2L, false);
        when(mapper.deleteMemo(9L, 3L, 2L, false)).thenReturn(1);
        assertThat(service.deleteMemo(9L, 2L)).isEqualTo(1);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 超级管理员修改他人私有记录后，原创建人仍可解密，删除携带管理员标志
     * @return
     * 返回值: void
     * 返回值说明: 不改变归属及创建审计字段，更新人使用实际登录账号
     */
    @Test
    void adminMaintainsOthersPrivateMemoWithoutChangingEncryptionOwner()
    {
        SysMemo original = new SysMemo();
        original.setOwnerUserId(2L);
        original.setVisibility("0");
        when(mapper.selectVisibleMemo(9L, 1L, true)).thenReturn(original);
        when(mapper.updateMemo(any(), eq(1L), eq(true))).thenReturn(1);
        var request = request();
        request.setMemoId(9L);
        request.setVersion(3L);
        assertThat(service.updateMemo(request)).isEqualTo(1);
        var capture = ArgumentCaptor.forClass(SysMemo.class);
        verify(mapper).updateMemo(capture.capture(), eq(1L), eq(true));
        var updated = capture.getValue();
        assertThat(updated.getOwnerUserId()).isEqualTo(2L);
        assertThat(updated.getCreateBy()).isNull();
        assertThat(updated.getUpdateBy()).isEqualTo("memo-test");
        assertThat(cipher.decrypt(updated.getContentCipher(), 2L)).isEqualTo(request.getMemoContent());
        assertThatThrownBy(() -> cipher.decrypt(updated.getContentCipher(), 1L)).isInstanceOf(ServiceException.class);
        when(mapper.deleteMemo(9L, 1L, 3L, true)).thenReturn(1);
        assertThat(service.deleteMemo(9L, 3L)).isEqualTo(1);
        when(mapper.updateMemo(any(), eq(1L), eq(true))).thenReturn(0);
        assertThatThrownBy(() -> service.updateMemo(request)).isInstanceOf(ServiceException.class)
                .hasMessageContaining("请刷新后重试");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 普通账号不能修改他人公开记录，也不能读取他人私有记录
     * @return
     * 返回值: void
     * 返回值说明: 不进入更新SQL，详情不泄露记录是否存在
     */
    @Test
    void ordinaryUserCannotEditOthersPublicOrReadPrivate()
    {
        loginAs(3L);
        SysMemo other = new SysMemo();
        other.setOwnerUserId(2L);
        other.setVisibility("1");
        when(mapper.selectVisibleMemo(9L, 3L, false)).thenReturn(other);
        var request = request();
        request.setMemoId(9L);
        request.setVersion(0L);
        assertThatThrownBy(() -> service.updateMemo(request)).isInstanceOf(ServiceException.class)
                .hasMessage("备忘录不存在或无权修改");
        verify(mapper, never()).updateMemo(any(), anyLong(), anyBoolean());
        assertThatThrownBy(() -> service.selectMemoById(10L)).isInstanceOf(ServiceException.class)
                .hasMessage("备忘录不存在或无权查看");
        verify(mapper).selectVisibleMemo(10L, 3L, false);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 列表按登录态生成范围及操作资格，不以页面传参或角色名称确定超级管理员
     * @return
     * 返回值: void
     * 返回值说明: 超级管理员可维护全部，普通账号只能维护本人
     */
    @Test
    void listComputesManageEligibilityFromAuthenticatedUser()
    {
        SysMemo own = new SysMemo();
        own.setOwnerUserId(3L);
        SysMemo other = new SysMemo();
        other.setOwnerUserId(2L);
        when(mapper.selectMemoList(any(), eq(1L), eq(true))).thenReturn(List.of(own, other));
        assertThat(service.selectMemoList(new SysMemoQuery())).allMatch(SysMemo::isCanManage);
        loginAs(3L);
        when(mapper.selectMemoList(any(), eq(3L), eq(false))).thenReturn(List.of(own, other));
        var result = service.selectMemoList(new SysMemoQuery());
        assertThat(result.get(0).isCanManage()).isTrue();
        assertThat(result.get(1).isCanManage()).isFalse();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 不完整登录主体不能查询或写入，避免空用户ID退化为公开访问
     * @return
     * 返回值: void
     * 返回值说明: 无有效用户ID时返回401且不访问数据库
     */
    @Test
    void invalidAuthenticatedUserFailsClosed()
    {
        for (Long id : java.util.Arrays.asList(null, 0L, -1L))
        {
            loginAs(id);
            assertThatThrownBy(() -> service.selectMemoList(new SysMemoQuery()))
                    .isInstanceOfSatisfying(ServiceException.class, error -> assertThat(error.getCode()).isEqualTo(401));
            assertThatThrownBy(() -> service.insertMemo(request())).isInstanceOf(ServiceException.class);
        }
        verifyNoInteractions(mapper);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 查询条件有长度和可见范围约束，空白范围归一化但非法范围不退化为全部
     * @return
     * 返回值: void
     * 返回值说明: 非法输入不访问Mapper，合法输入保留既有可见范围
     */
    @Test
    void queryInputIsBoundedAndNormalized()
    {
        var query = new SysMemoQuery();
        query.setTitle("x".repeat(201));
        assertThatThrownBy(() -> service.selectMemoList(query)).isInstanceOf(ServiceException.class);
        query.setTitle(null);
        query.setResourceUrl("x".repeat(2001));
        assertThatThrownBy(() -> service.selectMemoList(query)).isInstanceOf(ServiceException.class);
        query.setResourceUrl(null);
        query.setVisibility("invalid");
        assertThatThrownBy(() -> service.selectMemoList(query)).isInstanceOf(ServiceException.class);
        verifyNoInteractions(mapper);
        query.setVisibility(" 1 ");
        when(mapper.selectMemoList(any(), eq(1L), eq(true))).thenReturn(List.of());
        service.selectMemoList(query);
        assertThat(query.getVisibility()).isEqualTo("1");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 页面参数不能绕过分页上限或覆盖固定排序，异常后也不能残留分页线程上下文
     * @return
     * 返回值: void
     * 返回值说明: pageSize限制为1至500，401保留原状态码且不缓存响应
     */
    @Test
    void mvcBoundsPaginationAndPreservesAuthenticationFailure() throws Exception
    {
        ISysMemoService mockService = mock(ISysMemoService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new SysMemoController(mockService)).build();
        for (int size : new int[] { 0, -5, 10, 100000 })
        {
            int expected = Math.max(1, Math.min(500, size));
            doAnswer(invocation -> {
                var page = com.github.pagehelper.PageHelper.getLocalPage();
                assertThat(page.getPageNum()).isEqualTo(1);
                assertThat(page.getPageSize()).isEqualTo(expected);
                assertThat(page.getOrderBy()).isNullOrEmpty();
                return List.of();
            }).when(mockService).selectMemoList(any());
            mvc.perform(get("/systemTools/memo/list").param("pageSize", String.valueOf(size))
                    .param("pageNum", "-1").param("orderByColumn", "title").param("isAsc", "asc"))
                    .andExpect(jsonPath("$.code").value(200));
            assertThat(com.github.pagehelper.PageHelper.getLocalPage()).isNull();
        }
        doThrow(new ServiceException("登录状态无效，请重新登录", 401)).when(mockService).selectMemoList(any());
        mvc.perform(get("/systemTools/memo/list"))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(header().string("Cache-Control", "no-store"));
        assertThat(com.github.pagehelper.PageHelper.getLocalPage()).isNull();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 校验错误只返回固定提示，不能把非法正文放入异常
     * @return
     * 返回值: void
     * 返回值说明: 非法范围、空标题及超长正文均拒绝
     */
    @Test
    void validationNeverEchoesContent()
    {
        var request = request();
        request.setVisibility("public");
        assertThatThrownBy(() -> service.insertMemo(request)).isInstanceOf(ServiceException.class)
                .hasMessageNotContaining("test-password");
        request.setVisibility("0");
        request.setTitle(" ");
        assertThatThrownBy(() -> service.insertMemo(request)).isInstanceOf(ServiceException.class);
        request.setTitle("test");
        request.setMemoContent("test-password".repeat(10000));
        assertThatThrownBy(() -> service.insertMemo(request)).isInstanceOf(ServiceException.class)
                .hasMessage("备忘录内容不能超过100000个字符");
        verifyNoInteractions(mapper);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 真实解析Mapper XML验证所有可见范围、创建人及版本条件，不执行SQL
     * @return
     * 返回值: void
     * 返回值说明: 列表不选正文，写入不修改所属用户
     */
    @Test
    void mapperBindsVisibilityOwnerAndVersion() throws Exception
    {
        Configuration config = new Configuration();
        String resource = "mapper/system/SysMemoMapper.xml";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, config, resource, config.getSqlFragments()).parse();
        }
        var query = new SysMemoQuery();
        query.setTitle("demo");
        query.setVisibility("0");
        String prefix = SysMemoMapper.class.getName() + ".";
        var list = config.getMappedStatement(prefix + "selectMemoList").getBoundSql(Map.of("query", query, "userId", 3L, "admin", false));
        assertThat(list.getSql().replaceAll("\\s+", " ")).contains("(visibility = '1' or owner_user_id = ?)", "and visibility = ?", "order by create_time desc, memo_id desc")
                .doesNotContain("memo_content");
        var detail = config.getMappedStatement(prefix + "selectVisibleMemo").getBoundSql(Map.of("memoId", 9L, "userId", 3L, "admin", false));
        assertThat(detail.getSql().replaceAll("\\s+", " ")).contains("where memo_id = ? and (visibility = '1' or owner_user_id = ?)");
        var update = config.getMappedStatement(prefix + "updateMemo").getBoundSql(Map.of("memo", new SysMemo(), "userId", 3L, "admin", false));
        assertThat(update.getSql().replaceAll("\\s+", " ")).contains("where memo_id = ? and version = ? and owner_user_id = ?", "version = version + 1");
        var delete = config.getMappedStatement(prefix + "deleteMemo").getBoundSql(Map.of("memoId", 9L, "userId", 3L, "version", 0L, "admin", false));
        assertThat(delete.getSql().replaceAll("\\s+", " ")).contains("where memo_id = ? and version = ? and owner_user_id = ?");
        // 超级管理员只取消用户范围，筛选条件和并发版本仍保留。
        var adminList = config.getMappedStatement(prefix + "selectMemoList").getBoundSql(Map.of("query", query, "userId", 1L, "admin", true));
        assertThat(adminList.getSql()).contains("visibility = ?").doesNotContain("owner_user_id = ?");
        var adminDetail = config.getMappedStatement(prefix + "selectVisibleMemo").getBoundSql(Map.of("memoId", 9L, "userId", 1L, "admin", true));
        assertThat(adminDetail.getSql()).contains("where memo_id = ?").doesNotContain("owner_user_id = ?");
        var adminUpdate = config.getMappedStatement(prefix + "updateMemo").getBoundSql(Map.of("memo", new SysMemo(), "userId", 1L, "admin", true));
        assertThat(adminUpdate.getSql()).contains("version = ?").doesNotContain("owner_user_id = ?");
        var adminDelete = config.getMappedStatement(prefix + "deleteMemo").getBoundSql(Map.of("memoId", 9L, "userId", 1L, "version", 0L, "admin", true));
        assertThat(adminDelete.getSql()).contains("version = ?").doesNotContain("owner_user_id = ?");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 接口写操作同时要求菜单和按钮权限，审计日志不保存正文或响应
     * @return
     * 返回值: void
     * 返回值说明: 权限与SQL菜单标识一致
     */
    @Test
    void controllerPermissionsAndAuditExcludeSecrets() throws Exception
    {
        for (String action : List.of("add", "edit", "remove"))
        {
            var method = action.equals("remove")
                    ? SysMemoController.class.getMethod(action, Long.class, Long.class)
                    : SysMemoController.class.getMethod(action, SysMemoRequest.class);
            var log = method.getAnnotation(Log.class);
            assertThat(log.isSaveRequestData()).isFalse();
            assertThat(log.isSaveResponseData()).isFalse();
            var permissions = method.getAnnotation(RequiresPermissions.class).value();
            assertThat(permissions).containsExactly("systemTools:memo:list", "systemTools:memo:" + action);
        }
        assertThat(SysMemoController.class.getMethod("getInfo", Long.class)
                .getAnnotation(RequiresPermissions.class).value()).containsExactly("systemTools:memo:list");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 执行真实权限切面，菜单与写按钮必须同时具备，不能仅靠页面隐藏
     * @return
     * 返回值: void
     * 返回值说明: 权限不足不调用业务服务，权限齐备才进入写入入口
     */
    @Test
    void realPermissionAspectRequiresMenuAndWritePermission()
    {
        loginAs(3L);
        var login = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var mockService = mock(ISysMemoService.class);
        var aspect = new com.zero.framework.aspectj.RequiresPermissionsAspect();
        org.springframework.test.util.ReflectionTestUtils.setField(aspect, "permissionService",
                new com.zero.framework.web.service.PermissionService());
        var factory = new org.springframework.aop.aspectj.annotation.AspectJProxyFactory(new SysMemoController(mockService));
        factory.addAspect(aspect);
        SysMemoController proxy = factory.getProxy();
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(new org.springframework.mock.web.MockHttpServletRequest()));
        try
        {
            login.setPermissions(java.util.Set.of("systemTools:memo:edit"));
            assertThatThrownBy(() -> proxy.edit(request())).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
            login.setPermissions(java.util.Set.of("systemTools:memo:list"));
            proxy.getInfo(7L);
            assertThatThrownBy(() -> proxy.edit(request())).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
            assertThatThrownBy(() -> proxy.remove(7L, 0L)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
            verify(mockService, never()).updateMemo(any());
            verify(mockService, never()).deleteMemo(anyLong(), anyLong());
            login.setPermissions(java.util.Set.of("systemTools:memo:list", "systemTools:memo:edit"));
            when(mockService.updateMemo(any())).thenReturn(1);
            assertThat(proxy.edit(request()).get("code")).isEqualTo(200);
            verify(mockService).updateMemo(any());
        }
        finally
        {
            org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 实际MVC响应禁止缓存，非法JSON错误不回显正文
     * @return
     * 返回值: void
     * 返回值说明: 不启动外部服务，不执行写入
     */
    @Test
    void mvcDoesNotCacheOrEchoMalformedContent() throws Exception
    {
        ISysMemoService mockService = mock(ISysMemoService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new SysMemoController(mockService)).build();
        when(mockService.selectMemoList(any())).thenReturn(List.of());
        mvc.perform(get("/systemTools/memo/list")).andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.rows").isArray());
        mvc.perform(post("/systemTools/memo").contentType("application/json")
                .content("{\"memoContent\":\"test-password\",\"version\":\"bad\"}"))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("test-password"))));
        verify(mockService, never()).insertMemo(any());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 构造纯测试内容，禁止使用真实账号或凭证
     * @return
     * 返回值: SysMemoRequest
     * 返回值说明: 默认私有的合法新增请求
     */
    private SysMemoRequest request()
    {
        var request = new SysMemoRequest();
        request.setTitle("Git仓库");
        request.setMemoContent("  test-password\n保留换行  ");
        return request;
    }
}

