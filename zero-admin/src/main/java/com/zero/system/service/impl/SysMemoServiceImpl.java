package com.zero.system.service.impl;

import java.util.List;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import com.zero.common.exception.ServiceException;
import com.zero.common.constant.HttpStatus;
import com.zero.common.utils.SecurityUtils;
import com.zero.common.utils.StringUtils;
import com.zero.system.domain.SysMemo;
import com.zero.system.domain.SysMemoQuery;
import com.zero.system.domain.SysMemoRequest;
import com.zero.system.mapper.SysMemoMapper;
import com.zero.system.service.ISysMemoService;
import com.zero.system.service.MemoContentCipher;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17 21:20
 * 说明: 备忘录服务；普通账号按公开及本人范围访问，超级管理员可管理全部，正文加密且禁止记录敏感参数
 */
@Service
public class SysMemoServiceImpl implements ISysMemoService
{
    /** 备忘录数据访问。 */
    private final SysMemoMapper memoMapper;
    /** 正文加解密组件。 */
    private final MemoContentCipher contentCipher;
    /** 参数校验器，避免全局校验异常日志输出被拒绝的正文。 */
    private final Validator validator;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 注入备忘录数据访问、加密和校验组件
     * @param memoMapper 备忘录Mapper
     * @param contentCipher 独立正文加密组件
     * @param validator 标准参数校验器
     * @return
     * 返回值: 构造方法
     * 返回值说明: 初始化无状态业务服务
     */
    public SysMemoServiceImpl(SysMemoMapper memoMapper, MemoContentCipher contentCipher, Validator validator)
    {
        this.memoMapper = memoMapper;
        this.contentCipher = contentCipher;
        this.validator = validator;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 普通账号查询公开及本人记录，超级管理员查询全部；按创建时间和ID倒序，不读取正文
     * @param query 可选查询条件
     * @return
     * 返回值: List<SysMemo>
     * 返回值说明: 分页元数据，所属用户不可由客户端指定
     */
    @Override
    public List<SysMemo> selectMemoList(SysMemoQuery query)
    {
        if (query == null) query = new SysMemoQuery();
        query.setTitle(StringUtils.trim(query.getTitle()));
        query.setResourceUrl(StringUtils.trim(query.getResourceUrl()));
        query.setVisibility(StringUtils.trim(query.getVisibility()));
        var violations = validator.validate(query);
        if (!violations.isEmpty()) throw new ServiceException(violations.iterator().next().getMessage());
        Long userId = currentUserId();
        boolean admin = SecurityUtils.isAdmin(userId);
        List<SysMemo> list = memoMapper.selectMemoList(query, userId, admin);
        list.forEach(memo -> memo.setCanManage(admin || userId.equals(memo.getOwnerUserId())));
        return list;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 先按可见范围读取，再解密；不存在和不可见使用同一提示
     * @param memoId 备忘录ID，正整数
     * @return
     * 返回值: SysMemo
     * 返回值说明: 当前用户可见的完整详情
     */
    @Override
    public SysMemo selectMemoById(Long memoId)
    {
        checkId(memoId);
        Long userId = currentUserId();
        boolean admin = SecurityUtils.isAdmin(userId);
        SysMemo memo = memoMapper.selectVisibleMemo(memoId, userId, admin);
        if (memo == null) throw new ServiceException("备忘录不存在或无权查看");
        memo.setCanManage(admin || userId.equals(memo.getOwnerUserId()));
        memo.setMemoContent(contentCipher.decrypt(memo.getContentCipher(), memo.getOwnerUserId()));
        memo.setContentCipher(null);
        return memo;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 新增备忘录，身份和审计字段使用登录态，不接受客户端所属用户
     * @param request 新增参数，正文可空
     * @return
     * 返回值: int
     * 返回值说明: 成功插入条数
     */
    @Override
    public int insertMemo(SysMemoRequest request)
    {
        SysMemo memo = buildMemo(request, currentUserId());
        memo.setCreateBy(SecurityUtils.getUsername());
        return memoMapper.insertMemo(memo);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 创建人或超级管理员按版本原子更新，保留原创建人及正文加密绑定
     * @param request 修改参数，ID及版本必填
     * @return
     * 返回值: int
     * 返回值说明: 成功更新条数，冲突时要求重新读取
     */
    @Override
    public int updateMemo(SysMemoRequest request)
    {
        if (request == null) throw new ServiceException("备忘录参数不能为空");
        checkId(request.getMemoId());
        checkVersion(request.getVersion());
        Long userId = currentUserId();
        boolean admin = SecurityUtils.isAdmin(userId);
        SysMemo original = memoMapper.selectVisibleMemo(request.getMemoId(), userId, admin);
        if (original == null || (!admin && !userId.equals(original.getOwnerUserId())))
        {
            throw new ServiceException("备忘录不存在或无权修改");
        }
        // 加密绑定原创建人，超级管理员维护不会转移归属或破坏后续解密。
        SysMemo memo = buildMemo(request, original.getOwnerUserId());
        memo.setMemoId(request.getMemoId());
        memo.setVersion(request.getVersion());
        memo.setUpdateBy(SecurityUtils.getUsername());
        int rows = memoMapper.updateMemo(memo, userId, admin);
        if (rows == 0) throw new ServiceException("备忘录不存在、无权修改或已被修改，请刷新后重试");
        return rows;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 创建人或超级管理员按版本原子删除，普通账号不能删除他人记录
     * @param memoId 备忘录ID，正整数
     * @param version 确认删除时的版本，非负整数
     * @return
     * 返回值: int
     * 返回值说明: 成功删除条数，不符合条件时拒绝
     */
    @Override
    public int deleteMemo(Long memoId, Long version)
    {
        checkId(memoId);
        checkVersion(version);
        Long userId = currentUserId();
        int rows = memoMapper.deleteMemo(memoId, userId, version, SecurityUtils.isAdmin(userId));
        if (rows == 0) throw new ServiceException("备忘录不存在、无权删除或已被修改，请刷新后重试");
        return rows;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 校验参数并构造仅含密文的持久化对象，正文不trim避免改变密码
     * @param request 客户端允许提交的业务字段
     * @param ownerUserId 新增时取登录人，修改时取数据库原创建人，不接受客户端传入
     * @return
     * 返回值: SysMemo
     * 返回值说明: 保留服务端确定归属的加密实体，不复制客户端审计字段
     */
    private SysMemo buildMemo(SysMemoRequest request, Long ownerUserId)
    {
        if (request == null) throw new ServiceException("备忘录参数不能为空");
        var violations = validator.validate(request);
        if (!violations.isEmpty()) throw new ServiceException(violations.iterator().next().getMessage());
        SysMemo memo = new SysMemo();
        memo.setTitle(request.getTitle().trim());
        memo.setResourceUrl(StringUtils.trim(request.getResourceUrl()));
        memo.setRemark(request.getRemark());
        memo.setVisibility(request.getVisibility());
        memo.setOwnerUserId(ownerUserId);
        memo.setContentCipher(contentCipher.encrypt(request.getMemoContent(), memo.getOwnerUserId()));
        return memo;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 拒绝空或非正数主键
     * @param memoId 备忘录ID
     * @return
     * 返回值: void
     * 返回值说明: 无效ID返回固定业务错误
     */
    private void checkId(Long memoId)
    {
        if (memoId == null || memoId <= 0) throw new ServiceException("备忘录ID无效");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 拒绝缺失或无效的登录用户ID，避免不完整登录态读取公开数据或写入错误归属
     * @return
     * 返回值: Long
     * 返回值说明: 当前登录用户ID，无有效身份时返回401业务异常
     */
    private Long currentUserId()
    {
        Long userId = SecurityUtils.getUserId();
        if (userId == null || userId <= 0) throw new ServiceException("登录状态无效，请重新登录", HttpStatus.UNAUTHORIZED);
        return userId;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 写入必须携带读取版本，防止多个页面静默覆盖
     * @param version 读取时的版本，非负整数
     * @return
     * 返回值: void
     * 返回值说明: 版本缺失或非法时拒绝写入
     */
    private void checkVersion(Long version)
    {
        if (version == null || version < 0) throw new ServiceException("备忘录版本无效，请刷新后重试");
    }
}

